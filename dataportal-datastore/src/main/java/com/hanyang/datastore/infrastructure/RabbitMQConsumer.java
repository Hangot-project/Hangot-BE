package com.hanyang.datastore.infrastructure;

import com.hanyang.datastore.service.DataParsingService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final DataParsingService dataParsingService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();
        String datasetId = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            dataParsingService.createDataTable(datasetId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("데이터 파싱 처리 실패", e);
            handleProcessingError(message, channel, e);
        }
    }

    private void handleProcessingError(Message message, Channel channel, Exception e) {
        long tag = message.getMessageProperties().getDeliveryTag();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        int retryCount = (Integer) headers.getOrDefault("x-retry-count", 0);

        try {
            int MAX_RETRY_COUNT = 3;
            if (retryCount < MAX_RETRY_COUNT) {
                headers.put("x-retry-count", retryCount + 1);
                Message retryMessage = new Message(message.getBody(), message.getMessageProperties());
                rabbitTemplate.send(
                        message.getMessageProperties().getReceivedExchange(),
                        message.getMessageProperties().getReceivedRoutingKey(),
                        retryMessage
                );
                channel.basicAck(tag, false);
            }
            else {
                log.error("데이터 파싱 최대 재시도 초과",e);
            }
        } catch (IOException io) {
            log.error("Nack 또는 재전송 실패", io);
        }
    }
}