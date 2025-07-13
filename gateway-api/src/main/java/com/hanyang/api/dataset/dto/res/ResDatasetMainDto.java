package com.hanyang.api.dataset.dto.res;

import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.dataset.domain.DatasetTheme;
import lombok.Data;

import java.util.List;

@Data
public class ResDatasetMainDto {

    private List<SimpleDatasetMain> dataset;

    public ResDatasetMainDto(List<Dataset> dataset) {
        this.dataset = dataset.stream().map(SimpleDatasetMain::new).toList();
    }

    @Data
    public static class SimpleDatasetMain{
        private Long datasetId;
        private String title;
        private String type;
        private String organization;
        private List<String> themeList;
        private Integer scrap;

        public SimpleDatasetMain(Dataset dataset) {
            this.datasetId = dataset.getDatasetId();
            this.title = dataset.getTitle();
            this.type = dataset.getType();
            this.organization = dataset.getOrganization();
            this.themeList = dataset.getDatasetThemeList().stream().map(DatasetTheme::getTheme).toList();
            this.scrap = dataset.getScrap();

        }
    }
}
