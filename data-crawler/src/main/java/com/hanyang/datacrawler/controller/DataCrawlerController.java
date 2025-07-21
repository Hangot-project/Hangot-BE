package com.hanyang.datacrawler.controller;

import com.hanyang.datacrawler.service.DataCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class DataCrawlerController {

    private final DataCrawlerService dataCrawlerService;

    @PostMapping("/crawl")
    public ResponseEntity<String> crawlData(
            @RequestParam(defaultValue = "data.go.kr") String siteName,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "10") int maxPages) {

        try {
            dataCrawlerService.crawlDatasets(siteName, pageSize, maxPages);
            return ResponseEntity.ok(null);
        } catch (Exception error) {
            log.error("{} 크롤링 중 오류 발생: {}", siteName, error.getMessage(), error);
            return ResponseEntity.internalServerError()
                    .body(siteName + " 크롤링 중 오류 발생: " + error.getMessage());
        }
    }

}