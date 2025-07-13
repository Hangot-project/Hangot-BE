package com.hanyang.api.dataset.dto.res;

import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.dataset.domain.DatasetTheme;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResDatasetDetailDto {

    private Long datasetId;
    private String title;
    private String description;
    private String organization;
    private List<String> theme;
    private LocalDate createdDate;
    private LocalDate updateDate;
    private Integer view;
    private Integer scrap;
    private Integer download;
    private String resourceName;
    private String resourceUrl;
    private String type;
    private String license;

    public ResDatasetDetailDto(Dataset dataset,Integer scrapCount) {
        this.datasetId = dataset.getDatasetId();
        this.title = dataset.getTitle();
        this.description = dataset.getDescription();
        this.organization = dataset.getOrganization();
        this.theme = dataset.getDatasetThemeList().stream().map(DatasetTheme::getTheme).toList();
        this.createdDate = dataset.getCreatedDate();
        this.updateDate = dataset.getUpdatedDate();
        this.view = dataset.getView();
        this.download = dataset.getDownload();
        this.license = dataset.getLicense();
        this.resourceName = dataset.getResourceName();
        this.resourceUrl = dataset.getResourceUrl();
        this.type = dataset.getType();
        this.scrap = scrapCount;
    }
}
