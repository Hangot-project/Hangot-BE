package com.hanyang.datacrawler.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class RabbitMQPublisher {
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String datasetId) {
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setHeader("x-retry-count", 0);

        Message message = new Message(datasetId.getBytes(StandardCharsets.UTF_8), props);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}
