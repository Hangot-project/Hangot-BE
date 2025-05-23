package com.hanyang.dataportal.resource.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String requestId;
    private Long datasetId;
    private String timestamp;

    public MessageDto(Long datasetId) {
        this.requestId = UUID.randomUUID().toString();
        this.datasetId = datasetId;
        this.timestamp =  Instant.now().toString();
    }
}
