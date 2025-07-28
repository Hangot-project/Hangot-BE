package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.dto.DatasetWithTag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DataCrawler {

    String getSiteDomain();
    List<String> getSourceUrlList(int pageNo, int pageSize, LocalDate startDate, LocalDate endDate);
    Optional<DatasetWithTag> crawlSingleDataset(String sourceUrl);
    void crawlDataset(List<String> sourceUrls);
}