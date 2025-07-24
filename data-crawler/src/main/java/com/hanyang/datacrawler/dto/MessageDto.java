package com.hanyang.datacrawler.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {
    String datasetId;
    String sourceUrl;
}