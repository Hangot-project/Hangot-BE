package com.hanyang.datastore.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(String rawMessage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MessageDto message = objectMapper.readValue(rawMessage, MessageDto.class);
        log.info("receive message: {}", message.toString());
    }
}
