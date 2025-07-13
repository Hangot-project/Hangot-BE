package com.hanyang.api.resource.dto;

import com.hanyang.api.dataset.domain.Dataset;
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
