package com.hanyang.datacrawler.exception;

import java.time.LocalDate;

public class CrawlStopException extends RuntimeException {
    
    public CrawlStopException(String datasetTitle, LocalDate updatedDate, LocalDate cutoffDate) {
        super(String.format("데이터셋 '%s'의 수정일(%s)이 기준일(%s)보다 오래되어 크롤링 중단", 
                datasetTitle, updatedDate, cutoffDate));
    }
}