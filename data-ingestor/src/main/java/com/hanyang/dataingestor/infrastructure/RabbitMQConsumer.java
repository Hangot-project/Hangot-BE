package com.hanyang.dataingestor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.dataingestor.dto.MessageDto;
import com.hanyang.dataingestor.service.DataParsingService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final DataParsingService dataParsingService;
    private final S3StorageManager s3StorageManager;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        
        try {
            MessageDto messageDto = objectMapper.readValue(messageBody, MessageDto.class);
            log.info("메세지 수령: {}", messageDto.getDatasetId());

            dataParsingService.createDataTable(messageDto.getDatasetId());
            s3StorageManager.deleteDatasetFiles(messageDto.getDatasetId());

            //메세지 처리 완료
            channel.basicAck(tag, false);
            log.info("메세지 처리 완료: {}", messageDto.getDatasetId());
        } catch (Exception e) {
            log.error("데이터 처리 실패: {} - {}", messageBody, e.getMessage());
            handleProcessingError(message, channel);
        }
    }

    private void handleProcessingError(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            MessageDto messageDto = objectMapper.readValue(messageBody, MessageDto.class);
            sendToDLQ(message, messageDto, "data-processing-error", null);

        } catch (Exception parseEx) {
            sendToDLQ(message, null, "json-parsing-error", parseEx);
        }
        
        try {
            channel.basicAck(tag, false);
        } catch (IOException ackEx) {
            log.error("메시지 ACK 실패: {}", ackEx.getMessage());
        }
    }
    
    private void sendToDLQ(Message originalMessage, MessageDto messageDto, String failureType, Exception error) {
        try {
            MessageProperties dlqProps = new MessageProperties();
            dlqProps.setContentType("application/json");

            dlqProps.getHeaders().put("x-failure-type", failureType);
            dlqProps.getHeaders().put("x-failure-time", java.time.LocalDateTime.now().toString());
            dlqProps.getHeaders().put("x-original-queue", originalMessage.getMessageProperties().getConsumerQueue());

            if (messageDto != null) {
                dlqProps.getHeaders().put("x-dataset-id", messageDto.getDatasetId());
                dlqProps.getHeaders().put("x-resource-url", messageDto.getResourceUrl());
                dlqProps.getHeaders().put("x-source-url", messageDto.getSourceUrl());
            }

            if (error != null) {
                dlqProps.getHeaders().put("x-error-message", error.getMessage());
                dlqProps.getHeaders().put("x-error-class", error.getClass().getSimpleName());
                // 스택트레이스 일부만 포함 (너무 길어질 수 있어서)
                String stackTrace = getShortStackTrace(error);
                dlqProps.getHeaders().put("x-stack-trace", stackTrace);
            }
            
            Message dlqMessage = new Message(originalMessage.getBody(), dlqProps);
            rabbitTemplate.send(exchangeName + ".dlx", routingKey + ".dlq", dlqMessage);
            
            String datasetId = messageDto != null ? messageDto.getDatasetId() : "unknown";
            log.error("메시지 DLQ로 전송 완료: {} (원인: {})", datasetId, failureType);
            
        } catch (Exception e) {
            log.error("DLQ 전송 실패: {}", e.getMessage());
        }
    }
    
    private String getShortStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        
        StackTraceElement[] traces = e.getStackTrace();
        int limit = Math.min(3, traces.length);
        for (int i = 0; i < limit; i++) {
            sb.append("  at ").append(traces[i].toString()).append("\n");
        }
        
        return sb.toString();
    }
}
