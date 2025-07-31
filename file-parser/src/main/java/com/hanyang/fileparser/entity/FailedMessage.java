package com.hanyang.fileparser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "failed_messages")
public class FailedMessage {
    
    @Id
    private String id;
    
    private String messageBody;
    
    private String failureReason;
    
    private String status;

    private String notes;
    
    private LocalDateTime failedAt;
}