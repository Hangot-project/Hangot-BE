package com.hanyang.fileparser.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.fileparser.core.exception.ParsingException;
import com.hanyang.fileparser.core.exception.ResourceNotFoundException;
import com.hanyang.fileparser.dto.MessageDto;
import com.hanyang.fileparser.service.DataIngestionService;
import com.hanyang.fileparser.service.DatasetCountService;
import com.hanyang.fileparser.service.FailedMessageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionConsumer {

    private final DataIngestionService dataIngestionService;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final FailedMessageService failedMessageService;
    private final DatasetCountService datasetCountService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @RabbitListener(queues = {"${rabbitmq.queue.name}", "${rabbitmq.queue.name}.retry"}, ackMode = "MANUAL")
    public void handleMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("메세지 수령: : {}", messageBody);

        MessageDto messageDto = null;
        try {
            messageDto = objectMapper.readValue(messageBody, MessageDto.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패, 메시지 버림: {}", messageBody);
            channel.basicAck(deliveryTag, false);
            return;
        }

        datasetCountService.incrementProcessingCount(messageDto.getDatasetId());
        
        try{
            dataIngestionService.createDataTable(messageDto);
            log.info("메세지 처리 완료: {}", messageBody);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.info("지원하지 않는 형식이거나 파일 링크가 없는 경우: {}", e.getMessage());
        } catch (DataAccessResourceFailureException e) {
            sendToRetryQueue(message, e);
        } catch (ParsingException e) {
            log.info("파싱 중 발생한 에러: {}", e.getMessage(),e);
            failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(e));
        } catch (Throwable e) {
            log.error("시스템 에러 발생 {}", e.getMessage());
            failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(e));
        } finally {
            channel.basicAck(deliveryTag, false);
            datasetCountService.removeKey(messageDto.getDatasetId());
        }
    }

    private String getFullStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    private void sendToRetryQueue(Message message, Exception error) {
        int retryCount = getRetryCount(message);
        
        if (retryCount < 3) {
            try {
                rabbitTemplate.send(exchangeName + ".retry", routingKey + ".retry", message);
                log.warn("재시도 큐 전송 - MongoDB 연결 에러 ({}번째 재시도, 5분 후 재처리): {}", retryCount + 1, error.getMessage());
            } catch (Exception e) {
                log.error("재시도 큐 전송 실패: {}", e.getMessage());
                String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
                failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(e));
            }
        } else {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(error));
            log.error("최대 재시도 횟수 초과 - MongoDB에 저장: {}", error.getMessage());
        }
    }
    
    private int getRetryCount(Message message) {
        List<Map<String, Object>> xDeathHeader = (List<Map<String, Object>>) message.getMessageProperties().getHeaders().get("x-death");
        
        if (xDeathHeader != null && !xDeathHeader.isEmpty()) {
            Object count = xDeathHeader.get(0).get("count");
            return count != null ? ((Number) count).intValue() : 0;
        }
        return 0;
    }
}
