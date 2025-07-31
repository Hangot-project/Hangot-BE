package com.hanyang.fileparser.service;

import com.hanyang.fileparser.entity.FailedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
                .failedAt(LocalDateTime.now())
                .build();
            
            mongoTemplate.save(failedMessage);
    }
}