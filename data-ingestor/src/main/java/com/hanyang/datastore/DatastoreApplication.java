package com.hanyang.datastore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DatastoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(DatastoreApplication.class, args);
	}

}
