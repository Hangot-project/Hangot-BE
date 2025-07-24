package com.hanyang.dataingestor.infrastructure;

import com.hanyang.dataingestor.core.exception.ResourceNotFoundException;
import com.hanyang.dataingestor.dto.GroupType;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MongoManager {
    private final MongoTemplate mongoTemplate;
    @Value("${datastore.batch.size:1000}")
    private int batchSize;
    private static final String ID_FIELD = "_id";


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

    public Optional<Map<String, Object>> findById(String collectionName, Object id) {
        Document doc = mongoTemplate.findById(id, Document.class, collectionName);
        return Optional.ofNullable(doc);
    }

    public List<Document> findAll(String collectionName) {
        Query query = new Query().limit(100);
        return mongoTemplate.find(query, Document.class, collectionName);
    }

    public List<Document> groupByAxis(String collectionName, String axis, GroupType type) {
        Optional<Map<String, Object>> row = findById(collectionName, 1);
        if (row.isEmpty()) {
            throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");
        }

        if (!row.get().containsKey(axis)) {
            throw new IllegalArgumentException("지정된 축 '" + axis + "'가 데이터에 존재하지 않습니다");
        }

        List<String> keys = row.get().entrySet().stream()
                .filter(e -> e.getKey().equals(axis) || (!e.getKey().equals("_id") && isNumericColumn(e.getValue())))
                .map(Map.Entry::getKey)
                .toList();

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("숫자 컬럼이 없어 그룹핑할 수 없습니다");
        }

        AggregationOperation groupStage = buildGroupStage(axis, keys, type);

        Aggregation aggregation = Aggregation.newAggregation(groupStage);
        return mongoTemplate.aggregate(aggregation, collectionName, Document.class).getMappedResults();
    }


    private boolean isNumericColumn(Object value) {
        return value instanceof Number;
    }

    private AggregationOperation buildGroupStage(String axis, List<String> keys, GroupType type) {
        return context -> {
            Document groupDoc = new Document("_id", "$" + axis);
            for (String key : keys) {
                if(key.equals(axis)) continue;
                groupDoc.put(key, new Document(
                        switch (type) {
                            case SUM -> "$sum";
                            case AVG -> "$avg";
                        },
                        "$" + key
                ));
            }
            return new Document("$group", groupDoc);
        };
    }

    public void processBatchData(String datasetId, String[] columns, List<List<String>> rows) {
        List<Map<String, Object>> buffer = new ArrayList<>();
        int rowCount = 0;

        for (List<String> row : rows) {
            rowCount++;
            Map<String, Object> document = createDocument(row, columns, rowCount);

            if (!document.isEmpty()) {
                buffer.add(document);

                if (buffer.size() >= batchSize) {
                    insertDocuments(datasetId, buffer);
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            insertDocuments(datasetId, buffer);
        }

    }

    private Map<String, Object> createDocument(List<String> row, String[] columns, int rowId) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put(ID_FIELD, rowId);

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
