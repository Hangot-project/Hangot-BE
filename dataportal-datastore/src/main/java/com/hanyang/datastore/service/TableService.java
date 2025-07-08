package com.hanyang.datastore.service;

import com.hanyang.datastore.core.exception.ResourceNotFoundException;
import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.dto.ResChartTableDto;
import com.hanyang.datastore.dto.GroupType;
import com.hanyang.datastore.infrastructure.MongoManager;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {
    
    private static final int FIRST_ROW_ID = 1;
    private static final String MONGO_ID_FIELD = "_id";
    private static final double DEFAULT_NUMERIC_VALUE = 0.0;
    private static final String DEFAULT_STRING_VALUE = "";
    private static final String DATASET_NOT_FOUND_MESSAGE = "해당 데이터셋이 없거나 파일이 존재하지 않습니다";
    
    private final MongoManager mongoManager;

    public ResChartDto getAggregationLabel(String datasetId, String axis, GroupType type) {
        List<Document> resultList = mongoManager.groupByAxis(datasetId, axis, type);

        List<String> dataNames = resultList.get(0).keySet().stream()
                .filter(key -> !key.equals(axis))
                .collect(Collectors.toList());

        List<String> xLabels = resultList.stream()
                .map(doc -> String.valueOf(doc.get(axis)))
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
        Optional<Map<String, Object>> row = mongoManager.findById(datasetId, FIRST_ROW_ID);
        validateDatasetExists(row);
        return row.get().keySet();
    }

    public ResChartTableDto getChartTable(String datasetId) {
        List<Document> resultList = mongoManager.findAll(datasetId);
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

    private void validateDatasetExists(Optional<Map<String, Object>> row) {
        if (row.isEmpty()) {
            throw new ResourceNotFoundException(DATASET_NOT_FOUND_MESSAGE);
        }
    }
}
