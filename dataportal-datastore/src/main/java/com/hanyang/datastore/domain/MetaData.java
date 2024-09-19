package com.hanyang.datastore.domain;

import com.hanyang.datastore.dto.DatasetMetaDataDto;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Embedded
    private List<TableData> dataList = new ArrayList<>();

    public MetaData(DatasetMetaDataDto datasetMetaDataDto) {
        this.datasetId = datasetMetaDataDto.getDatasetId();
        this.title = datasetMetaDataDto.getTitle();
    }

    public void setDataListClean() {
        this.dataList = new ArrayList<>();
    }

    public void setDataList(List<TableData> dataList) {
        this.dataList = dataList;
    }

    public void updateDataset(DatasetMetaDataDto datasetMetaDataDto) {
        this.datasetId = datasetMetaDataDto.getDatasetId();
        this.title = datasetMetaDataDto.getTitle();
    }


}
