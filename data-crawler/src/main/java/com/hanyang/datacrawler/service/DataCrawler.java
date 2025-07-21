package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;

import java.time.LocalDate;
import java.util.Optional;

public interface DataCrawler {

    String getSiteName();
    void crawlDatasetsPage(int pageNo, int pageSize,LocalDate targetDate);
    Optional<Dataset> crawlSingleDataset(String datasetUrl, LocalDate targetDate);
    void downloadFile(Dataset dataset);
}