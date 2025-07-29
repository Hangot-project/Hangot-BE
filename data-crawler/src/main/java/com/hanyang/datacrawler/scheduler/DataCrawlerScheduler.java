package com.hanyang.datacrawler.scheduler;

import com.hanyang.datacrawler.service.DataCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "crawler.scheduler.enabled", havingValue = "true")
public class DataCrawlerScheduler {

    private final DataCrawlerService dataCrawlerService;

    @Scheduled(cron = "0 00 2 * * ?")
    public void scheduledDataGoKRCrawl() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("공공 데이터 크롤링 시작 - 목표 날짜: {}", yesterday);
        dataCrawlerService.crawlDatasets("https://www.data.go.kr", 40, yesterday,yesterday);
        log.info("공공 데이터 크롤링 완료");
    }

    @Scheduled(cron = "0 00 3 * * ?")
    public void scheduledSeoulDataCrawl() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("서울 열린데이터 광장 크롤링 시작 - 목표 날짜: {}", yesterday);
        dataCrawlerService.crawlDatasets("https://data.seoul.go.kr", 10, yesterday,yesterday);
        log.info("서울 열린데이터 광장 크롤링 완료");
    }
}