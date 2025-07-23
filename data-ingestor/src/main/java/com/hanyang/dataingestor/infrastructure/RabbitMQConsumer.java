package com.hanyang.dataingestor.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final DataParsingService dataParsingService;
    private final S3StorageManager s3StorageManager;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final MongoManager mongoManager;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("메세지 수령: : {}", messageBody);

        MessageDto messageDto;
        try {
            messageDto = objectMapper.readValue(messageBody, MessageDto.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패, 메시지 버림: {}", messageBody, e);
            try {
                channel.basicAck(tag, false);
            } catch (Exception ackException) {
                log.error("메시지 ACK 실패: {}", ackException.getMessage());
            }
            return;
        }

        try{
            dataParsingService.createDataTable(messageDto.getDatasetId());
            s3StorageManager.deleteDatasetFiles(messageDto.getDatasetId());
            channel.basicAck(tag, false);
            log.info("메세지 처리 완료: {}", messageBody);
        } catch (Exception e) {
            mongoManager.dropIfExists(messageDto.getDatasetId());
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
            log.error("메시지 DLQ로 전송 완료: {} (원인: {})", message, error.getClass().getSimpleName());

        } catch (Exception e) {
            log.error("DLQ 전송 실패: {}", e.getMessage());
        }
    }
}
