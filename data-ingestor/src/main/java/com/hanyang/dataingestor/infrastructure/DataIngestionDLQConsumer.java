package com.hanyang.dataingestor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.dataingestor.dto.MessageDto;
import com.hanyang.dataingestor.service.DataIngestionService;
import com.hanyang.dataingestor.service.FailedMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionDLQConsumer {

    private final ObjectMapper objectMapper;
    private final DataIngestionService dataIngestionService;
    private final FailedMessageService failedMessageService;
    private final S3StorageManager s3StorageManager;

    @RabbitListener(queues = "${rabbitmq.queue.name}.dlq", concurrency = "1")
    public void handleDeadLetterMessage(Message message) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        
        int deathCount = getDeathCount(headers);
        
        try {
            MessageDto messageDto = objectMapper.readValue(messageBody, MessageDto.class);
            
            if (deathCount <= 1) {
                // 첫 번째 DLQ 도착 - 한 번 더 처리 시도
                log.warn("DLQ 처리 시작 ({}회차): {}", deathCount, messageBody);
                dataIngestionService.createDataTable(messageDto.getDatasetId());
                s3StorageManager.deleteFiles(messageDto.getDatasetId());
                log.warn("DLQ 처리 성공 ({}회차): {}", deathCount, messageBody);
            } else {
                // 두 번째 이상 DLQ 도착 - 최종 실패로 MongoDB에 저장
                log.error("DLQ 최종 실패 ({}회차): {}", deathCount, messageBody);
                saveToFailedMessages(messageBody, headers, message, "DLQ에서 처리 실패");
            }
            
        } catch (Exception e) {
            // 처리 실패 시 항상 MongoDB에 저장하고 메시지 소비 완료
            log.error("DLQ 처리 실패 ({}회차): {}", deathCount, messageBody, e);
            saveToFailedMessages(messageBody, headers, message, e.getMessage());
        }
    }
    
    private int getDeathCount(Map<String, Object> headers) {
        Object deathHeader = headers.get("x-death");
        if (deathHeader instanceof List) {
            return ((List<?>) deathHeader).size();
        }
        return 0;
    }
    
    private void saveToFailedMessages(String messageBody, Map<String, Object> headers, 
                                      Message message, String failureReason) {
        try {
            failedMessageService.saveFailedMessage(
                messageBody,
                headers,
                message.getMessageProperties().getReceivedRoutingKey(),
                message.getMessageProperties().getReceivedExchange(),
                failureReason
            );
        } catch (Exception e) {
            log.error("실패 메시지 저장 중 오류: {}", messageBody, e);
        }
    }
}