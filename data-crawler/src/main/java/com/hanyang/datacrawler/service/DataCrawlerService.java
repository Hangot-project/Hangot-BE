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

    public void crawlDatasets(String siteName, int pageSize, int maxPages) {
        log.info("{} 사이트 데이터 크롤링 시작 - 페이지 크기: {}, 최대 페이지: {}", siteName, pageSize, maxPages);

        try {
            DataCrawler crawler = crawlerFactory.getCrawler(siteName);
            int totalPages = Math.min(maxPages, crawler.getMaxPages());
            
            for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
                log.info("{} {}페이지 크롤링 시작", siteName, pageNo);
                
                List<Dataset> datasets = crawler.crawlDatasetsPage(pageNo, pageSize);
                
                if (datasets == null || datasets.isEmpty()) {
                    break;
                }
                log.info("{} 페이지에서 {}개 데이터셋 파싱 완료", siteName, datasets.size());
            }
        } catch (Exception e) {
            log.error("{} 크롤러 초기화 실패: {}", siteName, e.getMessage(), e);
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }
    }

    public void crawlDatasetsForPreviousDay(String siteName, int pageSize) {
        LocalDate previousDay = LocalDate.now().minusDays(1);
        log.info("{} 사이트 전날({}) 데이터 크롤링 시작 - 페이지 크기: {}, 모든 페이지 크롤링", 
                siteName, previousDay, pageSize);

        try {
            DataCrawler crawler = crawlerFactory.getCrawler(siteName);
            int maxPages = crawler.getMaxPages();
            
            for (int pageNo = 1; pageNo <= maxPages; pageNo++) {
                log.info("{} {}페이지 전날 데이터 크롤링 시작", siteName, pageNo);
                
                try {
                    List<Dataset> datasets = crawler.crawlDatasetsPage(pageNo, pageSize);
                    
                    if (datasets == null || datasets.isEmpty()) {
                        log.info("{} {}페이지에서 더 이상 데이터가 없어 크롤링 종료", siteName, pageNo);
                        break;
                    }
                    log.info("{} 페이지에서 {}개 전날 데이터셋 파싱 완료", siteName, datasets.size());
                } catch (CrawlStopException e) {
                    log.info("전날보다 오래된 데이터를 만나 크롤링 중단: {}", e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("{} 전날 데이터 크롤러 초기화 실패: {}", siteName, e.getMessage(), e);
            throw new RuntimeException("전날 데이터 크롤링 실패: " + e.getMessage(), e);
        }
    }


}