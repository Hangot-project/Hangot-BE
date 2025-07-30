package com.hanyang.fileparser.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanyang.fileparser.core.exception.ParsingException;
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
            return;
        }

        try {
            log.warn("DLQ 처리 시작: {}", messageBody);
            dataIngestionService.createDataTable(messageDto);
            log.warn("DLQ 처리 성공: {}", messageBody);

        } catch (ParsingException e) {
            // 처리 실패 시 항상 MongoDB에 저장하고 메시지 소비 완료
            log.error("DLQ 처리 실패  {}", messageBody, e);
            saveToFailedMessages(messageBody, getFullStackTrace(e));
        }
    }

    
    private void saveToFailedMessages(String messageBody, String failureReason) {
        try {
            failedMessageService.saveFailedMessage(
                messageBody,
                failureReason
            );
        } catch (Exception e) {
            log.error("실패 메시지 저장 중 오류: {}", messageBody, e);
        }
    }

    private String getFullStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}