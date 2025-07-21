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
    public void scheduledDailyCrawl() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("데이터 크롤링 시작 - 목표 날짜: {}", yesterday);
        dataCrawlerService.crawlDatasets("data.go.kr", 10, yesterday);
        log.info("데이터 크롤링 완료");
    }
}