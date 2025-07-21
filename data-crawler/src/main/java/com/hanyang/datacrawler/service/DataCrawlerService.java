package com.hanyang.datacrawler.service;

import com.hanyang.datacrawler.exception.CrawlStopException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCrawlerService {

    private final CrawlerFactory crawlerFactory;

    public void crawlDatasets(String siteName, int pageSize, LocalDate startDate, LocalDate endDate) {
        String dateInfo = startDate + " ~ " + endDate + " 기간";
        
        log.info("{} {} 크롤링 시작", siteName, dateInfo);

        try {
            DataCrawler crawler = crawlerFactory.getCrawler(siteName);
            
            for (int pageNo = 1; ; pageNo++) {
                try {
                    crawler.crawlDatasetsPage(pageNo, pageSize, startDate, endDate);
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