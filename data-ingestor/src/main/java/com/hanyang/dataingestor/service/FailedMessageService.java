package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.entity.FailedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedMessageService {

    private final MongoTemplate mongoTemplate;

    public void saveFailedMessage(String messageBody, String failureReason) {
        
        Query query = new Query(Criteria.where("messageBody").is(messageBody));
        FailedMessage existingMessage = mongoTemplate.findOne(query, FailedMessage.class);
        
        if (existingMessage != null) {
            Update update = new Update()
                    .set("lastFailedAt", LocalDateTime.now())
                    .inc("retryCount", 1)
                    .set("failureReason", failureReason);
            
            mongoTemplate.updateFirst(query, update, FailedMessage.class);
            log.info("기존 실패 메시지 업데이트: {}", existingMessage.getId());
        } else {
            // 새 실패 메시지 저장
            FailedMessage failedMessage = FailedMessage.builder()
                    .messageBody(messageBody)
                    .failureReason(failureReason)
                    .status("FAILED")
                    .build();
            
            mongoTemplate.save(failedMessage);
        }
    }

}