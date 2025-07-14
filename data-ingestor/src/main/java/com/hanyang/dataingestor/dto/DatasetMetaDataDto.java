package com.hanyang.dataingestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DatasetMetaDataDto {
    @JsonProperty("datasetId")
    private String datasetId;
    @JsonProperty("title")
    private String title;

}
