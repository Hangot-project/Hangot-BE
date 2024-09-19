package com.hanyang.dataportal.dataset.dto.res;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.DatasetTheme;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class ResDatasetListDto {

    private Integer totalPage;
    private Long totalElement;
    private List<SimpleDataset> data;

    public ResDatasetListDto(Page<Dataset> datasets) {
        this.totalPage = datasets.getTotalPages();
        this.totalElement = datasets.getTotalElements();
        this.data = datasets.getContent().stream().map(SimpleDataset::new).toList();
    }

    @Data
    public static class SimpleDataset{
        private Long datasetId;
        private String title;
        private String description;
        private Organization organization;
        private Integer view;
        private Type type;
        private List<Theme> themeList;
        private Integer scrap;

        public SimpleDataset(Dataset dataset) {
            this.datasetId = dataset.getDatasetId();
            this.title = dataset.getTitle();
            this.description = dataset.getDescription();
            this.view = dataset.getView();
            this.organization = dataset.getOrganization();
            if(dataset.getResource()!=null) {
                this.type = dataset.getResource().getType();
            }
            if(dataset.getDatasetThemeList()!=null) {
                this.themeList = dataset.getDatasetThemeList().stream().map(DatasetTheme::getTheme).toList();
            }
            this.scrap = dataset.getScrapList().size();
        }
    }
}
