package com.hanyang.dataingestor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.dataingestor.dto.MessageDto;
import com.hanyang.dataingestor.service.DataParsingService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterQueueConsumer {

    private final ObjectMapper objectMapper;
    private final DataParsingService dataParsingService;
    private final S3StorageManager s3StorageManager;

    @RabbitListener(queues = "${rabbitmq.queue.name}.dlq", concurrency = "1")
    public void handleDeadLetterMessage(Message message, Channel channel) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        long tag = message.getMessageProperties().getDeliveryTag();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            MessageDto messageDto = objectMapper.readValue(messageBody, MessageDto.class);
            
            log.warn("=== DLQ 처리 시작 ===");
            log.warn("데이터셋 ID: {}", messageDto.getDatasetId());
            log.warn("실패 시간: {}", headers.get("x-failure-time"));
            log.warn("에러 메시지: {}", headers.get("x-error-message"));

            dataParsingService.createDataTable(messageDto.getDatasetId(),messageDto.getResourceUrl());
            s3StorageManager.deleteDatasetFiles(messageDto.getDatasetId());
            channel.basicAck(tag, false);
            
            LocalDateTime endTime = LocalDateTime.now();
            log.warn("DLQ 처리 성공: {} (소요시간: {}초)", messageDto.getDatasetId(), 
                    java.time.Duration.between(startTime, endTime).getSeconds());
            log.warn("=== DLQ 처리 완료 ===");
            
        } catch (Exception e) {
            try {
                channel.basicNack(tag, false, false);
                LocalDateTime endTime = LocalDateTime.now();
                
                log.error("=== DLQ 최종 실패 ===");
                log.error("실패 시간: {}", endTime);
                log.error("소요시간: {}초", java.time.Duration.between(startTime, endTime).getSeconds());
                log.error("실패 메시지: {}", messageBody);
                log.error("실패 사유: {}", e.getMessage());
                log.error("스택 트레이스: ", e);
                log.error("메시지 헤더: {}", headers);
                log.error("=== 메시지 완전 폐기 ===");
                
            } catch (Exception ackEx) {
                log.error("메시지 ACK/NACK 실패: {}", ackEx.getMessage());
            }
        }
    }
}