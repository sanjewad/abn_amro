package com.mytask.abn;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AbnApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbnApplication.class, args);
	}

}
