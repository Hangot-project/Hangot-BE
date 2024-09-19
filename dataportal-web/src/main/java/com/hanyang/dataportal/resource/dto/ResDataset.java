package com.hanyang.dataportal.resource.dto;

import com.hanyang.dataportal.dataset.domain.Dataset;
import lombok.Data;

@Data
public class ResDataset {
    private Long datasetId;
    private String title;

    public ResDataset(Dataset dataset) {
        this.datasetId = dataset.getDatasetId();
        this.title = dataset.getTitle();
    }
}
