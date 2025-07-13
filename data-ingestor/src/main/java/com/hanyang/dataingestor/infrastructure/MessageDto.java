package com.hanyang.dataingestor.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String requestId;
    private Long datasetId;
    private String timestamp;
}
