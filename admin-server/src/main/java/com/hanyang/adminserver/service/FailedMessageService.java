package com.hanyang.adminserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.adminserver.entity.FailedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FailedMessageService {

    private final MongoTemplate mongoTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${file-parser.url:http://localhost:8081}")
    private String dataIngestorUrl;

    public List<FailedMessage> getFailedMessages(int page, int size) {
        Query query = new Query()
                .skip((long) page * size)
                .limit(size);
        
        return mongoTemplate.find(query, FailedMessage.class);
    }

    public void retryProcessing(String messageId) throws JsonProcessingException {
        FailedMessage failedMessage = getFailedMessageById(messageId);
        if (failedMessage == null) {
            throw new RuntimeException("실패 메시지를 찾을 수 없습니다: " + messageId);
        }
        
        processMessageDirectly(failedMessage);

        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update()
                .set("status", "SUCCESS")
                .set("notes", "성공");

        mongoTemplate.updateFirst(query, update, FailedMessage.class);
    }
    
    private void processMessageDirectly(FailedMessage failedMessage) throws JsonProcessingException {
        Map messageMap = objectMapper.readValue(failedMessage.getMessageBody(), Map.class);

        String url = dataIngestorUrl + "/api/file/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity<>(messageMap, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            String errorMessage = response.getBody();
            throw new RuntimeException(errorMessage != null ? errorMessage : "데이터 처리 API 호출 실패: " + response.getStatusCode());
        }
    }

    public void markAsIgnored(String messageId) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update()
                .set("status", "IGNORE");

        mongoTemplate.updateFirst(query, update, FailedMessage.class);
    }

    public void deleteFailedMessage(String messageId) {
        Query query = new Query(Criteria.where("id").is(messageId));
        mongoTemplate.remove(query, FailedMessage.class);
    }

    public FailedMessage getFailedMessageById(String messageId) {
        return mongoTemplate.findById(messageId, FailedMessage.class);
    }

    public void updateNotes(String messageId, String notes) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update().set("notes", notes);
        mongoTemplate.updateFirst(query, update, FailedMessage.class);
    }
}