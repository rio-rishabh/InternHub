package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedin.jobSearch.linkedin_job_finder.Config.ApiKeysConfig;
import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary implementation of JobScraperService that uses the Jooble Jobs API.
 * Jooble offers a free API with a generous request limit.
 */
@Service
@Primary
public class JoobleApiJobScraperService implements JobScraperService {
    private static final Logger logger = LoggerFactory.getLogger(JoobleApiJobScraperService.class);

    // Jooble API URL
    private static final String JOOBLE_API_URL = "https://jooble.org/api/";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApiKeysConfig apiKeysConfig;

    @Autowired
    public JoobleApiJobScraperService(ApiKeysConfig apiKeysConfig) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKeysConfig = apiKeysConfig;

        logger.info("JoobleApiJobScraperService initialized as primary job scraper");
    }

    @Override
    public List<JobListing> scrapeJobs(String searchQuery) {
        List<JobListing> jobs = new ArrayList<>();

        try {
            String apiKey = apiKeysConfig.getJoobleApiKey();
            if (apiKey == null || apiKey.isEmpty() || "YOUR_JOOBLE_API_KEY".equals(apiKey)) {
                logger.warn("Jooble API key not configured. Please set jooble.api.key in application.properties");
                return jobs;
            }

            logger.info("Fetching jobs from Jooble API for query: {}", searchQuery);

            // Create request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("keywords", searchQuery);
            requestPayload.put("page", 1);

            // Optional parameters
            // requestPayload.put("location", "New York"); // Add location if needed
            // requestPayload.put("radius", 25); // Search radius in miles
            // requestPayload.put("salary", 50000); // Minimum salary

            // Convert payload to JSON
            String requestJson = objectMapper.writeValueAsString(requestPayload);

            // Create the request entity
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(
                    JOOBLE_API_URL + apiKey,
                    request,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                processJoobleResponse(response.getBody(), jobs);
                logger.info("Successfully fetched {} jobs from Jooble API", jobs.size());
            } else {
                logger.warn("Jooble API returned non-success status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error fetching jobs from Jooble API: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void processJoobleResponse(String responseBody, List<JobListing> jobs) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode jobsNode = rootNode.get("jobs");

        if (jobsNode != null && jobsNode.isArray()) {
            for (JsonNode jobNode : jobsNode) {
                JobListing job = new JobListing();

                // Extract job details
                if (jobNode.has("title")) {
                    job.setTitle(jobNode.get("title").asText());
                }

                if (jobNode.has("company")) {
                    job.setCompany(jobNode.get("company").asText());
                }

                if (jobNode.has("location")) {
                    job.setLocation(jobNode.get("location").asText());
                }

                if (jobNode.has("link")) {
                    job.setJobUrl(jobNode.get("link").asText());
                }

                if (jobNode.has("updated")) {
                    job.setPostedDate(formatJoobleDate(jobNode.get("updated").asText()));
                }

                if (jobNode.has("type")) {
                    job.setJobType(jobNode.get("type").asText());
                }

                if (jobNode.has("salary")) {
                    job.setSalary(jobNode.get("salary").asText());
                }

                if (jobNode.has("snippet")) {
                    String description = jobNode.get("snippet").asText();
                    if (description.length() > 200) {
                        description = description.substring(0, 197) + "...";
                    }
                    job.setDescription(description);
                }

                job.setSource("Jooble");

                // Add job to list if it has at least a title
                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } else {
            logger.warn("Jooble API response does not contain a 'jobs' array");
        }
    }

    private String formatJoobleDate(String joobleDate) {
        // Jooble sometimes returns dates like "2 days ago" or ISO format
        // This could be enhanced to parse and format consistently
        return joobleDate;
    }
}