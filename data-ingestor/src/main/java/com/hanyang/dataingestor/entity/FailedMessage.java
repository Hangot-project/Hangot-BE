package com.hanyang.dataingestor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "failed_messages")
public class FailedMessage {
    
    @Id
    private String id;
    
    private String messageBody;
    
    private Map<String, Object> headers;
    
    private String routingKey;
    
    private String exchange;
    
    private String failureReason;
    
    private int retryCount;
    
    private LocalDateTime firstFailedAt;
    
    private LocalDateTime lastFailedAt;
    
    private String status;
    
    private String processedBy;
    
    private LocalDateTime processedAt;
    
    private String notes;
}