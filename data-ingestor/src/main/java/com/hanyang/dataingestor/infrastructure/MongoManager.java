package com.hanyang.dataingestor.infrastructure;

import com.hanyang.dataingestor.core.exception.ResourceNotFoundException;
import com.hanyang.dataingestor.dto.GroupType;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public Optional<Map<String, Object>> findById(String collectionName, Object id) {
        Document doc = mongoTemplate.findById(id, Document.class, collectionName);
        return Optional.ofNullable(doc);
    }

    public List<Document> findAll(String collectionName) {
        Query query = new Query().limit(100);
        return mongoTemplate.find(query, Document.class, collectionName);
    }

    public List<Document> groupByAxis(String collectionName, String axis, GroupType type) {
        Optional<Map<String,Object>> row = findById(collectionName, 1);
        if(row.isEmpty()) {
            throw new ResourceNotFoundException("해당 데이터셋이 없거나 파일이 존재하지 않습니다");
        }

        GroupOperation groupOp = Aggregation.group(axis);
        ProjectionOperation projectOp = Aggregation.project()
                .and("_id").as(axis)
                .andExclude("_id");

        for(Map.Entry<String,Object> column: row.get().entrySet()) {
            if(isNumericColumn(column.getValue()) && !column.getKey().equals(axis)){
                String key = column.getKey();
                groupOp = applyGroupOperation(groupOp, key, type);
                projectOp = projectOp.and(key).as(key);
            }
        }
        
        Aggregation aggregation = Aggregation.newAggregation(groupOp, projectOp);
        return mongoTemplate.aggregate(aggregation, collectionName, Document.class).getMappedResults();
    }
    
    private boolean isNumericColumn(Object value) {
        return value instanceof Number;
    }
    
    private GroupOperation applyGroupOperation(GroupOperation groupOp, String key, GroupType type) {
        if (type == GroupType.SUM) {
            return groupOp.sum(key).as(key);
        } else if (type == GroupType.AVG) {
            return groupOp.avg(key).as(key);
        } else {
            throw new IllegalArgumentException("지원하지 않는 집계 타입: " + type);
        }
    }

}
