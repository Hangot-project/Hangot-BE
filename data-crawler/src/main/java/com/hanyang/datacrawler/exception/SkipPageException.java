package com.hanyang.datacrawler.exception;

public class SkipPageException extends RuntimeException {

    public SkipPageException(int pageNo) {
        super(String.format("페이지 {} 스킵: 날짜 범위 밖 데이터만 존재", pageNo));
    }
}

