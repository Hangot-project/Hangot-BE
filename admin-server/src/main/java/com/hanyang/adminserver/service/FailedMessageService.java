package com.hanyang.adminserver.service;

import com.hanyang.adminserver.entity.FailedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedMessageService {

    private final MongoTemplate mongoTemplate;

    public List<FailedMessage> getFailedMessages(int page, int size) {
        Query query = new Query()
                .skip((long) page * size)
                .limit(size);
        
        return mongoTemplate.find(query, FailedMessage.class);
    }

    public void markAsProcessed(String messageId, String processedBy, String notes) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update()
                .set("status", "PROCESSED")
                .set("processedBy", processedBy)
                .set("processedAt", LocalDateTime.now())
                .set("notes", notes);
        
        mongoTemplate.updateFirst(query, update, FailedMessage.class);
    }

    public void markAsIgnored(String messageId, String processedBy, String notes) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update()
                .set("status", "IGNORED")
                .set("processedBy", processedBy)
                .set("processedAt", LocalDateTime.now())
                .set("notes", notes);
        
        mongoTemplate.updateFirst(query, update, FailedMessage.class);
    }

    public void deleteFailedMessage(String messageId) {
        Query query = new Query(Criteria.where("id").is(messageId));
        mongoTemplate.remove(query, FailedMessage.class);
    }

    public long getFailedMessageCount() {
        return mongoTemplate.count(new Query(), FailedMessage.class);
    }

    public List<FailedMessage> getFailedMessagesByStatus(String status) {
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.find(query, FailedMessage.class);
    }

    public FailedMessage getFailedMessageById(String messageId) {
        return mongoTemplate.findById(messageId, FailedMessage.class);
    }
}