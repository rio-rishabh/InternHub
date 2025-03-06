package com.linkedin.jobSearch.linkedin_job_finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.linkedin.jobSearch")
public class LinkedinJobFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinkedinJobFinderApplication.class, args);
	}
}
