package com.hanyang.dataingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DataIngestorApplication {
	public static void main(String[] args) {
		SpringApplication.run(DataIngestorApplication.class, args);
	}

}
