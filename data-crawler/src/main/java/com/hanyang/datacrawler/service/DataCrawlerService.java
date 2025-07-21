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

    public void crawlDatasets(String siteName, int pageSize) {
        LocalDate today = LocalDate.now();
        log.info("{} 사이트 크롤링 시작 (오늘 데이터 제외)", siteName);

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
                    log.info("{} 크롤링 중단: 오늘 데이터 도달", siteName);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("{} 크롤링 실패: {}", siteName, e.getMessage());
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }
        
        log.info("{} 크롤링 완료", siteName);
    }

    public void crawlDatasetsForPreviousDay(String siteName, int pageSize) {
        LocalDate previousDay = LocalDate.now().minusDays(1);
        log.info("{} 사이트 전날({}) 데이터 크롤링 시작", siteName, previousDay);

        try {
            DataCrawler crawler = crawlerFactory.getCrawler(siteName);
            
            for (int pageNo = 1; ; pageNo++) {
                
                try {
                    List<Dataset> datasets = crawler.crawlDatasetsPage(pageNo, pageSize);
                    
                    if (datasets == null || datasets.isEmpty()) {
                        log.debug("{} {}페이지에서 데이터 없음 - 크롤링 종료", siteName, pageNo);
                        break;
                    }
                    log.debug("{} 페이지 {}: {}개 전날 데이터셋 파싱", siteName, pageNo, datasets.size());
                } catch (CrawlStopException e) {
                    log.info("{} 전날 크롤링 중단: 전날보다 오래된 데이터 도달", siteName);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("{} 전낡 데이터 크롤링 실패: {}", siteName, e.getMessage());
            throw new RuntimeException("전낡 데이터 크롤링 실패: " + e.getMessage(), e);
        }
        
        log.info("{} 전날 데이터 크롤링 완료", siteName);
    }


}