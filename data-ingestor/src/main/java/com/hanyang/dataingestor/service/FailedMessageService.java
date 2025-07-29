package com.hanyang.dataingestor.service;

import com.hanyang.dataingestor.entity.FailedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailedMessageService {

    private final MongoTemplate mongoTemplate;

    public void saveFailedMessage(String messageBody, String failureReason) {
        FailedMessage failedMessage = FailedMessage.builder()
                .messageBody(messageBody)
                .failureReason(failureReason)
                .status("FAILED")
                .build();
            
            mongoTemplate.save(failedMessage);
    }
}