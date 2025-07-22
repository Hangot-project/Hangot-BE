package com.hanyang.dataingestor.infrastructure;

import com.hanyang.dataingestor.service.DataParsingService;
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
    private final S3StorageManager s3StorageManager;
    
    @Value("${rabbitmq.retry.max-count:3}")
    private int maxRetryCount;
    
    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(Message message, Channel channel) {
        log.info("메세지 수령: {}", message.getBody());
        long tag = message.getMessageProperties().getDeliveryTag();
        String datasetId = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            dataParsingService.createDataTable(datasetId);
            s3StorageManager.deleteDatasetFiles(datasetId);
            channel.basicAck(tag, false);
            log.info("데이터 처리 및 S3 파일 삭제 완료: {}", datasetId);
            
        } catch (Exception e) {
            log.error("데이터 처리 실패: {} - {}", datasetId, e.getMessage());
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
                log.warn("메시지 재시도: {}/{}", retryCount + 1, maxRetryCount);
            } else {
                channel.basicNack(tag, false, false); // DLQ로 전송
                log.error("최대 재시도 초과: {} ({}회)", new String(message.getBody(), StandardCharsets.UTF_8), retryCount);
            }
        } catch (IOException io) {
            log.error("메시지 처리 실패: {}", io.getMessage());
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
