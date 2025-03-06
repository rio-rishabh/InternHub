package com.linkedin.jobSearch.linkedin_job_finder.controller;

import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import com.linkedin.jobSearch.linkedin_job_finder.service.FallbackScraperService;
import com.linkedin.jobSearch.linkedin_job_finder.service.JobScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for job search API endpoints
 */
@RestController
@RequestMapping("/api")
public class JobSearchController {

    private static final Logger logger = LoggerFactory.getLogger(JobSearchController.class);

    private final JobScraperService primaryScraper;
    private final FallbackScraperService fallbackScraper;

    @Autowired
    public JobSearchController(
            JobScraperService primaryScraper,  // Will use the @Primary bean
            FallbackScraperService fallbackScraper) {
        this.primaryScraper = primaryScraper;
        this.fallbackScraper = fallbackScraper;
        logger.info("JobSearchController initialized with: {} and FallbackScraperService",
                primaryScraper.getClass().getSimpleName());
    }

    /**
     * Endpoint to search for jobs based on keywords
     *
     * @param keywords The job search keywords
     * @param limit Optional limit to number of results (default 20)
     * @return List of job listings matching the search criteria
     */
    @GetMapping("/jobs/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam String keywords,
            @RequestParam(required = false, defaultValue = "20") int limit) {

        try {
            logger.info("Received job search request for keywords: {}", keywords);
            long startTime = System.currentTimeMillis();

            // First try the primary scraper
            List<JobListing> jobs = primaryScraper.scrapeJobs(keywords);

            // If no results, use the fallback
            if (jobs.isEmpty()) {
                logger.info("No results from primary scraper, using fallback for: {}", keywords);
                jobs = fallbackScraper.scrapeJobs(keywords);
            }

            // Apply limit if needed
            if (jobs.size() > limit) {
                jobs = jobs.subList(0, limit);
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Found {} jobs for keywords '{}' in {}ms", jobs.size(), keywords, duration);

            Map<String, Object> response = new HashMap<>();
            response.put("jobs", jobs);
            response.put("count", jobs.size());
            response.put("keywords", keywords);
            response.put("executionTimeMs", duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing search request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Error processing request",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Health check endpoint
     *
     * @return Status information about the API
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Job search API is running");
        status.put("primaryImplementation", primaryScraper.getClass().getSimpleName());
        status.put("fallbackImplementation", fallbackScraper.getClass().getSimpleName());
        status.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }
}