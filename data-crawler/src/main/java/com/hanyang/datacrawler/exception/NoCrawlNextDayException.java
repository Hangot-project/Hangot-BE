package com.hanyang.datacrawler.exception;

import java.time.LocalDate;

public class NoCrawlNextDayException extends RuntimeException {
    
    public NoCrawlNextDayException(String datasetTitle, LocalDate updatedDate, LocalDate cutoffDate) {
        super(String.format("데이터셋 '%s'의 수정일(%s)이 기준일(%s)보다 늦어 크롤링에서 제외됨", 
                datasetTitle, updatedDate, cutoffDate));
    }
}