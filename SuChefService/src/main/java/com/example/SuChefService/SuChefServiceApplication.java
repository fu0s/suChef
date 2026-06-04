package com.example.SuChefService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the SuChef Service.
 * This class bootstraps the Spring Boot application.
 */
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class SuChefServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuChefServiceApplication.class, args);
	}

}
