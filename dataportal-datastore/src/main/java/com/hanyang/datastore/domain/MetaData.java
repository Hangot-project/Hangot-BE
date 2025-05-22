package com.hanyang.datastore.domain;

import com.hanyang.datastore.dto.DatasetMetaDataDto;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetaData {
    @Id
    private String datasetId;
    private String title;

    @Setter
    @Embedded
    private List<TableData> dataList = new ArrayList<>();

    public MetaData(DatasetMetaDataDto datasetMetaDataDto) {
        this.datasetId = datasetMetaDataDto.getDatasetId();
        this.title = datasetMetaDataDto.getTitle();
    }

    public void setDataListClean() {
        this.dataList = new ArrayList<>();
    }

    public void updateDataset(DatasetMetaDataDto datasetMetaDataDto) {
        this.datasetId = datasetMetaDataDto.getDatasetId();
        this.title = datasetMetaDataDto.getTitle();
    }


}
