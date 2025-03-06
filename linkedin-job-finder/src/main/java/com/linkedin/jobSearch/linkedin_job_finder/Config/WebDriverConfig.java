package com.linkedin.jobSearch.linkedin_job_finder.Config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class to set up WebDriverManager
 * This ensures WebDriverManager is initialized when the application starts
 */
@Configuration
public class WebDriverConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);

    @PostConstruct
    public void setup() {
        try {
            logger.info("Setting up WebDriverManager...");
            WebDriverManager.chromedriver().setup();
            logger.info("WebDriverManager initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriverManager: {}", e.getMessage(), e);
        }
    }
}