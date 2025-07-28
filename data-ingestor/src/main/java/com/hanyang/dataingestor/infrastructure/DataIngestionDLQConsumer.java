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
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionDLQConsumer {

    private final ObjectMapper objectMapper;
    private final DataIngestionService dataIngestionService;
    private final FailedMessageService failedMessageService;

    @RabbitListener(queues = "${rabbitmq.queue.name}.dlq", concurrency = "1")
    public void handleDeadLetterMessage(Message message) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        try {
            MessageDto messageDto = objectMapper.readValue(messageBody, MessageDto.class);
            
            log.warn("DLQ 처리 시작: {}", messageBody);
            dataIngestionService.createDataTable(messageDto);
            log.warn("DLQ 처리 성공: {}", messageBody);

        } catch (Exception e) {
            // 처리 실패 시 항상 MongoDB에 저장하고 메시지 소비 완료
            log.error("DLQ 처리 실패  {}", messageBody, e);
            saveToFailedMessages(messageBody, headers, message, e.getMessage());
        }
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