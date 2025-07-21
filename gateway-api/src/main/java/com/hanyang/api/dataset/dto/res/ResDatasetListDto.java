package com.hanyang.api.dataset.dto.res;

import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.dataset.domain.Tag;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
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
        private String organization;
        private Integer view;
        private String type;
        private List<String> tagList;
        private Integer scrap;
        private LocalDate createDate;

        public SimpleDataset(Dataset dataset) {
            this.datasetId = dataset.getDatasetId();
            this.title = dataset.getTitle();
            this.description = dataset.getDescription();
            this.view = dataset.getView();
            this.organization = dataset.getOrganization();
            this.createDate = dataset.getCreatedDate();
            this.type = dataset.getType();
            if(dataset.getTagList()!=null) {
                this.tagList = dataset.getTagList().stream().map(Tag::getTag).toList();
            }
            this.scrap = dataset.getScrap();
        }
    }
}
