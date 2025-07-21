package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.domain.Dataset;
import com.hanyang.datacrawler.exception.CrawlStopException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCrawlerService {

    private final CrawlerFactory crawlerFactory;

    public void crawlDatasets(String siteName, int pageSize, LocalDate targetDate) {
        String dateInfo = targetDate != null ? targetDate + " 날짜" : "사이트";
        
        log.info("{} {} 크롤링 시작", siteName, dateInfo);

        try {
            DataCrawler crawler = crawlerFactory.getCrawler(siteName);
            
            for (int pageNo = 1; ; pageNo++) {
                
                try {
                    List<Dataset> datasets = crawler.crawlDatasetsPage(pageNo, pageSize);
                    
                    if (datasets == null || datasets.isEmpty()) {
                        log.debug("{} {}페이지에서 데이터 없음 - 크롤링 종료", siteName, pageNo);
                        break;
                    }
                    log.debug("{} 페이지 {}: {}개 데이터셋 파싱", siteName, pageNo, datasets.size());
                } catch (CrawlStopException e) {
                    log.info("{} 크롤링 중단: 목표 날짜 이전 데이터 도달", siteName);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("{} 크롤링 실패: {}", siteName, e.getMessage());
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }
        
        log.info("{} {} 크롤링 완료", siteName, dateInfo);
    }

}