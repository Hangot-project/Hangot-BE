package com.hanyang.dataingestor.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MongoManager {
    private final MongoTemplate mongoTemplate;

    public void createCollection(String collectionName) {
        dropIfExists(collectionName);
        mongoTemplate.createCollection(collectionName);
    }

    public void dropIfExists(String collectionName) {
        boolean isExists = mongoTemplate.collectionExists(collectionName);
        if (isExists) mongoTemplate.dropCollection(collectionName);
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
            document.put(columns[j], value);
        }

        return document;
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
            return cellValue.trim();
        }
    }

}
