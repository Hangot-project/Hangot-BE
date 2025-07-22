package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.dto.DatasetWithTag;

import java.time.LocalDate;
import java.util.Optional;

public interface DataCrawler {

    String getSiteName();
    void crawlDatasetsPage(int pageNo, int pageSize, LocalDate startDate, LocalDate endDate);
    Optional<DatasetWithTag> crawlSingleDataset(String datasetUrl, LocalDate startDate, LocalDate endDate);
    void extractResourceURL(Dataset dataset);
}