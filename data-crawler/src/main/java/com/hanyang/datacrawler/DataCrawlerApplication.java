package com.hanyang.datacrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataCrawlerApplication.class, args);
	}

}
