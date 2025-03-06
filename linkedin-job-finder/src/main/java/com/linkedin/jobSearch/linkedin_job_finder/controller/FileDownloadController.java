package com.linkedin.jobSearch.linkedin_job_finder.controller;

import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import com.linkedin.jobSearch.linkedin_job_finder.service.ExcelExportService;
import com.linkedin.jobSearch.linkedin_job_finder.service.JobScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for downloading job search results as Excel
 */
@RestController
@RequestMapping("/api/download")
public class FileDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

    private final JobScraperService jobScraperService;
    private final ExcelExportService excelExportService;

    @Autowired
    public FileDownloadController(JobScraperService jobScraperService, ExcelExportService excelExportService) {
        this.jobScraperService = jobScraperService;
        this.excelExportService = excelExportService;
        logger.info("FileDownloadController initialized with: {} and {}",
                jobScraperService.getClass().getSimpleName(),
                excelExportService.getClass().getSimpleName());
    }

    /**
     * Endpoint to download job search results as Excel file
     *
     * @param keywords The job search keywords
     * @param limit Optional limit to number of results (default 100)
     * @return Excel file as a downloadable attachment
     */
    @GetMapping("/excel")
    public ResponseEntity<?> downloadExcel(
            @RequestParam String keywords,
            @RequestParam(required = false, defaultValue = "100") int limit) {

        try {
            logger.info("Received Excel download request for keywords: {}", keywords);
            long startTime = System.currentTimeMillis();

            // Scrape the job listings
            List<JobListing> jobs = jobScraperService.scrapeJobs(keywords);

            // Apply limit if needed
            if (jobs.size() > limit) {
                jobs = jobs.subList(0, limit);
            }

            // Export to Excel
            byte[] excelContent = excelExportService.exportToExcel(jobs);

            // Generate a filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String encodedKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String filename = "JobSearch_" + encodedKeywords + "_" + timestamp + ".xlsx";

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Generated Excel with {} jobs for keywords '{}' in {}ms", jobs.size(), keywords, duration);

            return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.error("Error generating Excel file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error generating Excel file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing download request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error processing request: " + e.getMessage());
        }
    }
}