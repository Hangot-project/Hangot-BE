package com.hanyang.datastore.service;

import com.hanyang.datastore.core.exception.ResourceNotFoundException;
import com.hanyang.datastore.dto.ResChartDto;
import com.hanyang.datastore.dto.ResChartTableDto;
import com.hanyang.datastore.infrastructure.GroupType;
import com.hanyang.datastore.infrastructure.MongoManager;
import com.hanyang.datastore.infrastructure.S3StorageManager;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TableService {
    private final S3StorageManager s3StorageManager;
    private final MongoManager mongoManager;

    public void createDataTable(String datasetId) {

        InputStream file = s3StorageManager.getFile(datasetId);
        ExcelSheetHandler excelSheetHandler = ExcelSheetHandler.readExcel(file);

        mongoManager.createCollection(datasetId);

        List<String> headers = excelSheetHandler.getHeader();
        String[] columns = headers.toArray(new String[0]);

        int BATCH_SIZE = 1000;
        int rowCount = 0;

        List<List<String>> rows = excelSheetHandler.getRows();
        List<Map<String, Object>> buffer = new ArrayList<>();

        for (List<String> row : rows) {
            rowCount++;
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("_id", rowCount);
            for (int j = 0; j < row.size(); j++) {
                String cellValue = row.get(j);
                Object value;
                try {
                    value = Double.parseDouble(cellValue);
                } catch (NumberFormatException e) {
                    value = cellValue;
                }
                if (j < columns.length) {
                    document.put(columns[j], value);
                }
            }

            if (!document.isEmpty()) buffer.add(document);
            if (buffer.size() == BATCH_SIZE) {
                mongoManager.insertDocuments(datasetId, buffer);
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) mongoManager.insertDocuments(datasetId, buffer);
    }

    public ResChartDto getAggregationLabel(String datasetId, String axis, GroupType type) {
        List<Document> resultList = mongoManager.groupByAxis(datasetId,axis,type);

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
                            return (val instanceof Number) ? ((Number) val).doubleValue() : 0.0;
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

    public Set<String> getAxis(String datasetId){
        Optional<Map<String,Object>> row = mongoManager.findById(datasetId,1);
        if(row.isEmpty()) throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");
        return row.get().keySet();
    }

    public ResChartTableDto getChartTable(String datasetId) {
        List<Document> resultList = mongoManager.findAll(datasetId);
        if (resultList.isEmpty()) throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");

        List<String> labelList = resultList.get(0).keySet().stream()
                .filter(key -> !key.equals("_id"))
                .collect(Collectors.toList());

        List<List<String>> dataList = resultList.stream()
                .map(doc -> labelList.stream()
                        .map(key -> {
                            Object val = doc.get(key);
                            return val != null ? val.toString() : "";
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        return new ResChartTableDto(labelList, dataList);
    }
}


