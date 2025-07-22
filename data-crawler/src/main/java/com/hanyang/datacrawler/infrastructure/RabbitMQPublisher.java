package com.hanyang.datacrawler.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.datacrawler.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
@Slf4j
public class RabbitMQPublisher {
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(MessageDto messageDto) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageDto);
            
            MessageProperties props = new MessageProperties();
            props.setContentType("application/json");

            Message message = new Message(jsonMessage.getBytes(StandardCharsets.UTF_8), props);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            
            log.info("메시지 전송 완료: datasetId={}, resourceUrl={}, sourceUrl={}", messageDto.getDatasetId(), messageDto.getResourceUrl(), messageDto.getSourceUrl());
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패로 메세지 전송 실패: {}", messageDto, e);
        }
    }
}
