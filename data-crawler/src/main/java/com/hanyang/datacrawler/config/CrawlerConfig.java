package com.hanyang.datacrawler.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerConfig {
    
    public static final int MAX_PAGES = 10;
    public static final int CONCURRENT_DATASET_LIMIT = 5;
}