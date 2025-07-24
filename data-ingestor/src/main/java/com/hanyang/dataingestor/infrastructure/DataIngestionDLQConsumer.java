package com.hanyang.dataingestor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.dataingestor.dto.MessageDto;
import com.hanyang.dataingestor.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionDLQConsumer {

    private final ObjectMapper objectMapper;
    private final DataIngestionService dataIngestionService;
    private final S3StorageManager s3StorageManager;

    @RabbitListener(queues = "${rabbitmq.queue.name}.dlq", concurrency = "1")
    public void handleDeadLetterMessage(Message message) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        try {
            MessageDto messageDto = objectMapper.readValue(messageBody, MessageDto.class);
            
            log.warn("DLQ 처리 시작: {} ",messageBody);
            dataIngestionService.createDataTable(messageDto.getDatasetId());
          //  s3StorageManager.deleteFiles(messageDto.getDatasetId());
            log.warn("DLQ 처리 완료: {} ",messageBody);
            
        } catch (Exception e) {
            log.error("DLQ 최종 실패: {} ",messageBody,e);
        }
    }
}