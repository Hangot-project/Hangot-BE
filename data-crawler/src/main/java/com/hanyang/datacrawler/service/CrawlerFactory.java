package com.hanyang.datacrawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlerFactory {
    
    private final List<DataCrawler> crawlers;
    private Map<String, DataCrawler> crawlerMap;

    public DataCrawler getCrawler(String siteName) {
        if (crawlerMap == null) {
            initializeCrawlerMap();
        }
        
        DataCrawler crawler = crawlerMap.get(siteName);
        if (crawler == null) {
            throw new IllegalArgumentException("지원하지 않는 사이트입니다: " + siteName);
        }
        
        log.info("크롤러 선택됨: {} -> {}", siteName, crawler.getClass().getSimpleName());
        return crawler;
    }


    private void initializeCrawlerMap() {
        crawlerMap = crawlers.stream()
                .collect(Collectors.toMap(
                        DataCrawler::getSiteDomain,
                        Function.identity()
                ));
        
        log.info("크롤러 팩토리 초기화 완료 - 지원 사이트: {}", crawlerMap.keySet());
    }
}