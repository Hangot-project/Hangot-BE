package com.hanyang.fileparser.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MongoManager {
    private final MongoTemplate mongoTemplate;

    public void dropCollection(String collectionName) {
        mongoTemplate.dropCollection(collectionName);
    }

    public void insertDocuments(String collectionName, List<?> objects) {
        if (objects == null || objects.isEmpty()) return;
        mongoTemplate.insert(objects, collectionName);
    }

    public void insertDataRows(String datasetId, String[] columns, List<List<String>> rows) {
        List<Map<String, Object>> documents = new ArrayList<>();

        for (List<String> row : rows) {
            Map<String, Object> document = createDocument(row, columns);

            if (!document.isEmpty()) {
                documents.add(document);
            }
        }

        if (!documents.isEmpty()) {
            insertDocuments(datasetId, documents);
        }
    }

    private Map<String, Object> createDocument(List<String> row, String[] columns) {
        Map<String, Object> document = new LinkedHashMap<>();

        for (int j = 0; j < row.size() && j < columns.length; j++) {
            String cellValue = row.get(j);
            Object value = parseValue(cellValue);
            String sanitizedColumnName = replaceDot(columns[j]);
            document.put(sanitizedColumnName, value);
        }

        return document;
    }


    private String replaceDot(String columnName) {
        if (columnName == null) {
            return null;
        }
        return columnName.replace(".", "\u00b7");
    }

    private Object parseValue(String cellValue) {
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return "";
        }

        try {
            // 정수인지 확인
            if (!cellValue.contains(".")) {
                return Long.parseLong(cellValue);
            }
            // 실수인지 확인
            return Double.parseDouble(cellValue);
        } catch (NumberFormatException e) {
            return  replaceDot(cellValue).trim();
        }
    }

}
