package com.hanyang.api.chart.service;

import com.hanyang.api.chart.dto.GroupType;
import com.hanyang.api.chart.dto.ResChartDto;
import com.hanyang.api.chart.dto.ResChartTableDto;
import com.hanyang.api.chart.dto.ResPieChartDto;
import com.hanyang.api.chart.infrastructure.MongoManager;
import com.hanyang.api.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {
    
    private static final String MONGO_ID_FIELD = "_id";
    private static final double DEFAULT_NUMERIC_VALUE = 0.0;
    private static final String DEFAULT_STRING_VALUE = "";
    private static final String DATASET_NOT_FOUND_MESSAGE = "해당 데이터셋이 없거나 파일이 존재하지 않습니다";
    
    private final MongoManager mongoManager;

    public ResChartDto getAggregationLabel(String datasetId, String axis, GroupType type) {
        Map<String, Object> row = validateDatasetAndAxis(datasetId, axis);
        
        List<String> keys = row.entrySet().stream()
                .filter(e -> e.getKey().equals(axis) || (!e.getKey().equals(MONGO_ID_FIELD) && isNumericColumn(e.getValue())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("숫자 컬럼이 없어 그룹핑할 수 없습니다");
        }
        
        List<Document> resultList = mongoManager.groupByAxis(datasetId, axis, type, keys);

        List<String> dataNames = resultList.get(0).keySet().stream()
                .filter(key -> !key.equals(axis) && !key.equals(MONGO_ID_FIELD))
                .collect(Collectors.toList());

        List<String> xLabels = resultList.stream()
                .map(doc -> String.valueOf(doc.get(MONGO_ID_FIELD)))
                .toList();

        List<List<Double>> dataList = dataNames.stream()
                .map(field -> resultList.stream()
                        .map(doc -> {
                            Object val = doc.get(field);
                            return (val instanceof Number) ? ((Number) val).doubleValue() : DEFAULT_NUMERIC_VALUE;
                        })
                        .toList()
                )
                .toList();

        return ResChartDto.builder()
                .x_axis_name(axis)
                .x_label(xLabels)
                .dataName(dataNames)
                .dataList(dataList)
                .build();
    }

    public Set<String> getAxis(String datasetId) {
        Optional<Map<String, Object>> row = mongoManager.findOne(datasetId);
        validateDatasetExists(row);
        return row.get().keySet().stream()
                .filter(key -> !key.equals(MONGO_ID_FIELD))
                .collect(Collectors.toSet());
    }

    public ResChartTableDto getChartTable(String datasetId) {
        List<Document> resultList = mongoManager.findLimit100(datasetId);
        if (resultList.isEmpty()) {
            throw new ResourceNotFoundException(DATASET_NOT_FOUND_MESSAGE);
        }

        List<String> labelList = resultList.get(0).keySet().stream()
                .filter(key -> !key.equals(MONGO_ID_FIELD))
                .collect(Collectors.toList());

        List<List<String>> dataList = resultList.stream()
                .map(doc -> labelList.stream()
                        .map(key -> {
                            Object val = doc.get(key);
                            return val != null ? val.toString() : DEFAULT_STRING_VALUE;
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        return ResChartTableDto.builder()
                .label(labelList)
                .dataList(dataList)
                .build();
    }

    public ResPieChartDto getPieChart(String datasetId, String axis) {
        validateDatasetAndAxis(datasetId, axis);
        List<Document> aggregatedData = mongoManager.groupBxAxisCount(datasetId, axis);

        List<String> labels = aggregatedData.stream()
                .map(doc -> {
                    Object val = doc.get("_id");
                    return val != null ? val.toString() : DEFAULT_STRING_VALUE;
                })
                .collect(Collectors.toList());

        List<Long> count = aggregatedData.stream()
                .map(doc -> {
                    Object val = doc.get("count");
                    return val instanceof Number ? ((Number) val).longValue() : 0L;
                })
                .collect(Collectors.toList());

        return ResPieChartDto.builder()
                .axis_name(axis)
                .labels(labels)
                .count(count)
                .build();
    }

    private Map<String, Object> validateDatasetAndAxis(String datasetId, String axis) {
        Optional<Map<String, Object>> row = mongoManager.findOne(datasetId);
        if (row.isEmpty()) {
            throw new ResourceNotFoundException(DATASET_NOT_FOUND_MESSAGE);
        }

        if (!row.get().containsKey(axis)) {
            throw new IllegalArgumentException("지정된 축 '" + axis + "'가 데이터에 존재하지 않습니다");
        }

        return row.get();
    }

    private boolean isNumericColumn(Object value) {
        return value instanceof Number;
    }

    private void validateDatasetExists(Optional<Map<String, Object>> row) {
        if (row.isEmpty()) {
            throw new ResourceNotFoundException(DATASET_NOT_FOUND_MESSAGE);
        }
    }
}