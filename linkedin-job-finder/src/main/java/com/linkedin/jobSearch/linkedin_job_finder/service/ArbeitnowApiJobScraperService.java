package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of JobScraperService that uses the free Arbeitnow API.
 * This API requires no authentication and provides job listings with no rate limits.
 */
@Service
public class ArbeitnowApiJobScraperService implements JobScraperService {
    private static final Logger logger = LoggerFactory.getLogger(ArbeitnowApiJobScraperService.class);

    // Arbeitnow API URL - no API key required!
    private static final String ARBEITNOW_API_URL = "https://arbeitnow.com/api/job-board-api";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ArbeitnowApiJobScraperService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<JobListing> scrapeJobs(String searchQuery) {
        List<JobListing> jobs = new ArrayList<>();

        try {
            logger.info("Fetching jobs from Arbeitnow API");

            // Build URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ARBEITNOW_API_URL)
                    .queryParam("search", searchQuery)
                    .queryParam("page", 1)
                    .queryParam("limit", 20);

            // Make the API call
            ResponseEntity<String> response = restTemplate.getForEntity(
                    builder.toUriString(),
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                processArbeitnowResponse(response.getBody(), jobs);
                logger.info("Successfully fetched {} jobs from Arbeitnow API", jobs.size());
            } else {
                logger.warn("Arbeitnow API returned non-success status: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error fetching jobs from Arbeitnow API: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private void processArbeitnowResponse(String responseBody, List<JobListing> jobs) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode != null && dataNode.isArray()) {
            for (JsonNode jobNode : dataNode) {
                JobListing job = new JobListing();

                // Extract job details
                if (jobNode.has("title")) {
                    job.setTitle(jobNode.get("title").asText());
                }

                if (jobNode.has("company_name")) {
                    job.setCompany(jobNode.get("company_name").asText());
                }

                if (jobNode.has("location")) {
                    job.setLocation(jobNode.get("location").asText());
                }

                if (jobNode.has("url")) {
                    job.setJobUrl(jobNode.get("url").asText());
                }

                if (jobNode.has("created_at")) {
                    job.setPostedDate(formatDate(jobNode.get("created_at").asText()));
                }

                // Job type extraction (remote, full-time, etc.)
                if (jobNode.has("remote") && jobNode.get("remote").asBoolean()) {
                    job.setJobType("Remote");
                } else if (jobNode.has("tags") && jobNode.get("tags").isArray()) {
                    for (JsonNode tag : jobNode.get("tags")) {
                        if (tag.asText().contains("full-time") || tag.asText().contains("part-time")) {
                            job.setJobType(tag.asText());
                            break;
                        }
                    }
                }

                // Description
                if (jobNode.has("description")) {
                    String description = jobNode.get("description").asText();
                    if (description.length() > 200) {
                        description = description.substring(0, 197) + "...";
                    }
                    job.setDescription(description);
                }

                job.setSource("Arbeitnow");

                // Add job to list if it has at least a title
                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } else {
            logger.warn("Arbeitnow API response does not contain a 'data' array");
        }
    }

    private String formatDate(String dateStr) {
        // Convert ISO date to a more readable format if needed
        // For now, just return the original date
        return dateStr;
    }
}