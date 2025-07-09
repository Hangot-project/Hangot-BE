package com.hanyang.dataportal.dataset.dto.res;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.DatasetTheme;
import com.hanyang.dataportal.dataset.domain.vo.License;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResDatasetDetailDto {

    private Long datasetId;
    private String title;
    private String description;
    private Organization organization;
    private List<Theme> theme;
    private LocalDate createdDate;
    private LocalDate updateDate;
    private Integer view;
    private Integer scrap;
    private Integer download;
    private String resourceName;
    private String resourceUrl;
    private Type type;
    private License license;

    public ResDatasetDetailDto(Dataset dataset,Integer scrapCount) {
        this.datasetId = dataset.getDatasetId();
        this.title = dataset.getTitle();
        this.description = dataset.getDescription();
        this.organization = dataset.getOrganization();
        this.theme = dataset.getDatasetThemeList().stream().map(DatasetTheme::getTheme).toList();
        this.createdDate = dataset.getCreatedDate();
        this.updateDate = dataset.getUpdateDate();
        this.view = dataset.getView();
        this.download = dataset.getDownload();
        this.license = dataset.getLicense();
        if(dataset.getResource() != null){
            this.resourceName = dataset.getResource().getResourceName();
            this.resourceUrl = dataset.getResource().getResourceUrl();
            this.type = dataset.getResource().getType();
        }
        this.scrap = scrapCount;
    }
}
