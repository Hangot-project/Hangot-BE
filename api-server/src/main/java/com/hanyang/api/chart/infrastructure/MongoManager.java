package com.hanyang.api.chart.infrastructure;

import com.hanyang.api.chart.dto.GroupType;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
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

    public Optional<Map<String, Object>> findOne(String collectionName) {
        Document doc = mongoTemplate.findOne(new Query(), Document.class, collectionName);
        return Optional.ofNullable(doc);
    }

    public List<Document> findLimit100(String collectionName) {
        Query query = new Query().limit(100);
        return mongoTemplate.find(query, Document.class, collectionName);
    }

    public List<Document> groupByAxis(String collectionName, String axis, GroupType type, List<String> keys) {
        AggregationOperation groupStage = buildGroupStage(axis, keys, type);
        Aggregation aggregation = Aggregation.newAggregation(groupStage);
        return mongoTemplate.aggregate(aggregation, collectionName, Document.class).getMappedResults();
    }


    public List<Document> groupBxAxisCount(String collectionName, String axis) {
        AggregationOperation groupStage = context -> new Document("$group", 
            new Document("_id", "$" + axis)
                .append("count", new Document("$sum", 1))
        );

        Aggregation aggregation = Aggregation.newAggregation(groupStage);
        return mongoTemplate.aggregate(aggregation, collectionName, Document.class).getMappedResults();
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
}