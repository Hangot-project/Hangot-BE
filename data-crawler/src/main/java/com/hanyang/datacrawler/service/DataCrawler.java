package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;

import java.util.List;
import java.util.Optional;

public interface DataCrawler {

    String getSiteName();
    List<Dataset> crawlDatasetsPage(int pageNo, int pageSize);
    Optional<Dataset> crawlSingleDataset(String datasetUrl);
    void downloadFile(Dataset dataset);

    default int getMaxPages() {
        return 10;
    }
}