package com.hanyang.datacrawler.scheduler;

import com.hanyang.datacrawler.service.DataCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "crawler.scheduler.enabled", havingValue = "true")
public class DataCrawlerScheduler {

    private final DataCrawlerService dataCrawlerService;

    @Scheduled(cron = "0 00 2 * * ?")
    public void scheduledDailyCrawl() {
        log.info("데이터 크롤링 시작...");
        dataCrawlerService.crawlDatasetsForPreviousDay("data.go.kr", 10);
        log.info("데이터 크롤링 완료");
    }
}