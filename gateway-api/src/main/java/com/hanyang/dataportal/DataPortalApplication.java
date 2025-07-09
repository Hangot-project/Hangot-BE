package com.hanyang.dataportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DataPortalApplication {
	public static void main(String[] args) {
		SpringApplication.run(DataPortalApplication.class, args);
	}
}
