package com.hanyang.fileparser.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.fileparser.core.exception.ParsingException;
import com.hanyang.fileparser.core.exception.ResourceNotFoundException;
import com.hanyang.fileparser.dto.MessageDto;
import com.hanyang.fileparser.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
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
            log.error("JSON 파싱 실패, 메시지 버림: {}", messageBody);
            return;
        }

        try{
            dataIngestionService.createDataTable(messageDto);
            log.info("메세지 처리 완료: {}", messageBody);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            // 데이터 시각화 지원하지 않거나,파일 다운로드를 지원하지 않음
            log.info("지원하지 않는 형식이나거나 파일 링크가 없는 경우: {}", e.getMessage());
        } catch (ParsingException e) {
            sendToDLQ(message, e);
        } catch (Exception e) {
            //예상하지 못한 에러
            log.error("예상 하지 못한 에러로 예외처리가 필요: {}", e.getMessage());
            sendToDLQ(message, e);
        }
    }
    
    private void sendToDLQ(Message message, Exception error) {
        try {
            rabbitTemplate.send(exchangeName + ".dlx", routingKey + ".dlq", message);
            log.error("DLQ 전송 - 파싱 에러: {}", error.getMessage(), error);
        } catch (Exception e) {
            log.error("DLQ 전송 실패: {}", e.getMessage());
        }
    }
}
