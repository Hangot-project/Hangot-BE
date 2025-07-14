package com.hanyang.api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResTitleDescDto {
    private Long datasetId;
    private String title;
    private String description;
}
