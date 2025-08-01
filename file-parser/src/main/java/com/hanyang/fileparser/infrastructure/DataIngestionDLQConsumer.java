package com.hanyang.fileparser.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.fileparser.dto.MessageDto;
import com.hanyang.fileparser.service.DataIngestionService;
import com.hanyang.fileparser.service.FailedMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionDLQConsumer {

    private final ObjectMapper objectMapper;
    private final DataIngestionService dataIngestionService;
    private final FailedMessageService failedMessageService;

    @RabbitListener(queues = "${rabbitmq.queue.name}.dlq", concurrency = "1")
    public void handleDeadLetterMessage(Message message) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

        MessageDto messageDto;
        try {
            messageDto = objectMapper.readValue(messageBody, MessageDto.class);
        } catch (JsonProcessingException e) {
            log.error("DLQ JSON 파싱 실패, 메시지 버림: {}", messageBody);
            return;
        }

        try {
            log.warn("DLQ 처리 시작 : {}", messageBody);
            dataIngestionService.createDataTable(messageDto);
            log.warn("DLQ 처리 성공: {}", messageBody);

        } catch (Exception e) {
            failedMessageService.saveFailedMessage(messageBody, getFullStackTrace(e));
        }
    }

    private String getFullStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}