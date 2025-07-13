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

    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    public void scheduledCrawl() {
        log.info("예약된 전체 데이터 크롤링 시작...");
        dataCrawlerService.crawlDatasets("data.go.kr",10,50);
        log.info("예약된 데이터 크롤링 완료");
    }

    @Scheduled(fixedDelay = 3600000)
    public void healthCheck() {
        log.debug("Data crawler scheduler is running");
    }
}