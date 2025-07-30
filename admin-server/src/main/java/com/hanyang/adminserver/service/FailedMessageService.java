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

    public void retryProcessing(String messageId, String processedBy, String notes) {
        FailedMessage failedMessage = getFailedMessageById(messageId);
        if (failedMessage == null) {
            throw new RuntimeException("Failed message not found: " + messageId);
        }
        
        try {
            // 실제 처리 로직 호출 (data-ingestor API 호출)
            processMessageDirectly(failedMessage);
            
            // 성공 시 PROCESSED로 상태 변경
            Query query = new Query(Criteria.where("id").is(messageId));
            Update update = new Update()
                    .set("status", "PROCESSED")
                    .set("processedBy", processedBy)
                    .set("processedAt", LocalDateTime.now())
                    .set("notes", notes);
            
            mongoTemplate.updateFirst(query, update, FailedMessage.class);
            log.info("Successfully reprocessed message: {}", messageId);
            
        } catch (Exception e) {
            log.error("Failed to reprocess message: {}", messageId, e);
            
            // 재처리 실패 시 실패 정보 업데이트
            Query query = new Query(Criteria.where("id").is(messageId));
            Update update = new Update()
                    .set("lastRetryAt", LocalDateTime.now())
                    .set("retryFailureReason", e.getMessage())
                    .inc("retryCount", 1);
            
            mongoTemplate.updateFirst(query, update, FailedMessage.class);
            throw new RuntimeException("Retry processing failed: " + e.getMessage(), e);
        }
    }
    
    private void processMessageDirectly(FailedMessage failedMessage) {
        // TODO: data-ingestor의 처리 로직을 직접 호출하거나 HTTP API 호출
        // 예시: RestTemplate을 사용해서 data-ingestor API 호출
        log.info("Processing message directly: {}", failedMessage.getMessageBody());
        
        // 실제 구현에서는 여기서 data-ingestor API를 호출하거나
        // 처리 로직을 직접 실행해야 합니다
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

    public List<FailedMessage> getAllFailedMessages() {
        return mongoTemplate.findAll(FailedMessage.class);
    }
}