package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCrawlerService {

    private final CrawlerFactory crawlerFactory;

    public void crawlDatasets(String siteName, int pageSize, int maxPages) {
        log.info("{} 사이트 데이터 크롤링 시작 - 페이지 크기: {}, 최대 페이지: {}", siteName, pageSize, maxPages);

        try {
            DataCrawler crawler = crawlerFactory.getCrawler(siteName);
            int totalPages = Math.min(maxPages, crawler.getMaxPages());
            
            for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
                log.info("{} {}페이지 크롤링 시작", siteName, pageNo);
                
                List<Dataset> datasets = crawler.crawlDatasetsPage(pageNo, pageSize);
                
                if (datasets == null || datasets.isEmpty()) {
                    log.info("{} {}페이지에서 더 이상 데이터가 없어 크롤링 종료", siteName, pageNo);
                    break;
                }
                log.info("{} 페이지에서 {}개 데이터셋 파싱 완료", siteName, datasets.size());
            }
        } catch (Exception e) {
            log.error("{} 크롤러 초기화 실패: {}", siteName, e.getMessage(), e);
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }
    }


}