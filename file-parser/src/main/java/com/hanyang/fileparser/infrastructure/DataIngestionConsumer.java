package com.hanyang.fileparser.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.fileparser.core.exception.ParsingException;
import com.hanyang.fileparser.core.exception.ResourceNotFoundException;
import com.hanyang.fileparser.dto.MessageDto;
import com.hanyang.fileparser.service.DataIngestionService;
import com.hanyang.fileparser.service.FailedMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionConsumer {

    private final DataIngestionService dataIngestionService;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final FailedMessageService failedMessageService;

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
            log.error("JSON 파싱 실패, 메시지 버림: {}", messageBody);
            return;
        }

        try{
            dataIngestionService.createDataTable(messageDto);
            log.info("메세지 처리 완료: {}", messageBody);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            // 데이터 시각화 지원하지 않거나,파일 다운로드를 지원하지 않음
            log.info("지원하지 않는 형식이거나 파일 링크가 없는 경우: {}", e.getMessage());
        } catch (ParsingException e) {
            log.info("파싱 중 발생한 에러: {}", e.getMessage(),e);
            failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(e));
        } catch (DataAccessException e) {
            sendToDelayQueue(message, e);
        } catch (Exception e) {
            //예상하지 못한 에러
            log.error("예상 하지 못한 에러로 예외처리가 필요: {}", e.getMessage());
            failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(e));
        }
    }

    private String getFullStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    private void sendToDelayQueue(Message message, Exception error) {
        try {
            rabbitTemplate.send(exchangeName + ".delay", routingKey + ".delay", message);
            log.warn("지연 큐 전송 - MongoDB 연결 에러 (1분 후 DLQ로 전송): {}", error.getMessage());
        } catch (Exception e) {
            log.error("지연 큐 전송 실패: {}", e.getMessage());
        }
    }
}
