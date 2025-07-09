package com.hanyang.datastore.infrastructure;

import com.hanyang.datastore.service.DataParsingService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${rabbitmq.retry.max-count:3}")
    private int maxRetryCount;
    
    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(Message message, Channel channel) {
        long tag = message.getMessageProperties().getDeliveryTag();
        String datasetId = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            log.info("데이터 파싱 시작: datasetId={}", datasetId);
            dataParsingService.createDataTable(datasetId);
            channel.basicAck(tag, false);
            log.info("데이터 파싱 완료: datasetId={}", datasetId);
            
        } catch (Exception e) {
            log.error("데이터 파싱 처리 실패: datasetId={}", datasetId, e);
            handleProcessingError(message, channel, e);
        }
    }

    private void handleProcessingError(Message message, Channel channel, Exception e) {
        long tag = message.getMessageProperties().getDeliveryTag();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        
        int retryCount = (Integer) headers.getOrDefault(RETRY_COUNT_HEADER, 0);

        try {
            if (retryCount < maxRetryCount) {
                retryMessage(message, headers, retryCount);
                channel.basicAck(tag, false);
                log.warn("메시지 재시도: retryCount={}/{}", retryCount + 1, maxRetryCount);
            } else {
                channel.basicNack(tag, false, false); // DLQ로 전송
                log.error("데이터 파싱 최대 재시도 초과: retryCount={}", retryCount, e);
            }
        } catch (IOException io) {
            log.error("메시지 처리 실패", io);
        }
    }

    private void retryMessage(Message message, Map<String, Object> headers, int currentRetryCount) {
        headers.put(RETRY_COUNT_HEADER, currentRetryCount + 1);
        Message retryMessage = new Message(message.getBody(), message.getMessageProperties());
        
        rabbitTemplate.send(
                message.getMessageProperties().getReceivedExchange(),
                message.getMessageProperties().getReceivedRoutingKey(),
                retryMessage
        );
    }
}
