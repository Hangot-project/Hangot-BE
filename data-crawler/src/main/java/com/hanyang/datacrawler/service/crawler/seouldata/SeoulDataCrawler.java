package com.hanyang.datacrawler.service.crawler.seouldata;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.service.DataCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 서울열린데이터광장(data.seoul.go.kr) 크롤러 구현체
 * TODO: 실제 구현 필요
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeoulDataCrawler implements DataCrawler {

    @Override
    public String getSiteName() {
        return "seoul.go.kr";
    }

    @Override
    public List<Dataset> crawlDatasetsPage(int pageNo, int pageSize) {
        return List.of();
    }

    @Override
    public Optional<Dataset> crawlSingleDataset(String datasetUrl) {
        return null;
    }

    @Override
    public void downloadFile(Dataset dataset) {}

}