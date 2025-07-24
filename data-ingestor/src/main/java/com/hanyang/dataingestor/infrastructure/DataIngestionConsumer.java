package com.hanyang.dataingestor.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.dataingestor.dto.MessageDto;
import com.hanyang.dataingestor.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionConsumer {

    private final DataIngestionService dataIngestionService;
    private final S3StorageManager s3StorageManager;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(Message message){
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("메세지 수령: : {}", messageBody);

        MessageDto messageDto;
        try {
            messageDto = objectMapper.readValue(messageBody, MessageDto.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패, 메시지 버림: {}", messageBody, e);
            return;
        }

        try{
            dataIngestionService.createDataTable(messageDto.getDatasetId());
            s3StorageManager.deleteFiles(messageDto.getDatasetId());
            log.info("메세지 처리 완료: {}", messageBody);
        } catch (Exception e) {
            sendToDLQ(message, e);
        }
    }
    
    private void sendToDLQ(Message message, Exception error) {
        try {
            MessageProperties dlqProps = new MessageProperties();
            dlqProps.setContentType("application/json");
            dlqProps.getHeaders().put("x-error-message", error.getMessage());
            dlqProps.getHeaders().put("x-failure-type", error.getClass().getSimpleName());

            rabbitTemplate.send(exchangeName + ".dlx", routingKey + ".dlq", message);
        } catch (Exception e) {
            log.error("DLQ 전송 실패: {}", e.getMessage());
        }
    }
}
