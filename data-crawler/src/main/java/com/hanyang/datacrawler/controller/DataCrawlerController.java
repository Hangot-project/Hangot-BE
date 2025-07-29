package com.hanyang.datacrawler.controller;

import com.hanyang.datacrawler.service.DataCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class DataCrawlerController {

    private final DataCrawlerService dataCrawlerService;

    @PostMapping("/crawl")
    public ResponseEntity<String> crawlData(
            @RequestParam(defaultValue = "https://data.seoul.go.kr") String siteName,
            @RequestParam(defaultValue = "40") int pageSize,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            dataCrawlerService.crawlDatasets(siteName, pageSize, startDate, endDate);
            return ResponseEntity.ok(null);
        } catch (Exception error) {
            log.error("{} 크롤링 중 오류 발생: {}", siteName, error.getMessage(), error);
            return ResponseEntity.internalServerError()
                    .body(siteName + " 크롤링 중 오류 발생: " + error.getMessage());
        }
    }

}