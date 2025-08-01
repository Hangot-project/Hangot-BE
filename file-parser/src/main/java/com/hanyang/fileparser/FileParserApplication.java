package com.hanyang.fileparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FileParserApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileParserApplication.class, args);
	}

}
