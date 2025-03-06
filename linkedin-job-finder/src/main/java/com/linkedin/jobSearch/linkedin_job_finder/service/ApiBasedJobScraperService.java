package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.linkedin.jobSearch.linkedin_job_finder.Config.ApiKeysConfig;
import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JobScraperService implementation that uses multiple public APIs to fetch job listings
 * instead of web scraping which is often blocked by job sites.
 */
@Service
public class ApiBasedJobScraperService implements JobScraperService {
    private static final Logger logger = LoggerFactory.getLogger(ApiBasedJobScraperService.class);

    // Free Jsearch API from RapidAPI
    private static final String JSEARCH_API_URL = "https://jsearch.p.rapidapi.com/search";
    private static final String RAPID_API_HOST = "jsearch.p.rapidapi.com";

    // Alternative: Serpapi Google Jobs API
    private static final String SERPAPI_URL = "https://serpapi.com/search.json?engine=google_jobs&q=%s&api_key=%s";

    // Adzuna API (provides free tier)
    private static final String ADZUNA_URL = "https://api.adzuna.com/v1/api/jobs/us/search/1?app_id=%s&app_key=%s&results_per_page=20&what=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApiKeysConfig apiKeysConfig;

    @Autowired
    public ApiBasedJobScraperService(ApiKeysConfig apiKeysConfig) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.apiKeysConfig = apiKeysConfig;

        logger.info("ApiBasedJobScraperService initialized as an alternative job scraper");
    }

    @Override
    public List<JobListing> scrapeJobs(String searchQuery) {
        List<JobListing> jobs = new ArrayList<>();

        try {
            logger.info("Fetching jobs from various APIs for query: {}", searchQuery);

            // First try JSearch API
            boolean success = false;

            try {
                success = fetchJobsFromJsearch(searchQuery, jobs);
            } catch (Exception e) {
                logger.warn("Failed to fetch from JSearch API: {}", e.getMessage());
            }

            // If JSearch fails, try SerpApi
            if (!success) {
                try {
                    success = fetchJobsFromSerpApi(searchQuery, jobs);
                } catch (Exception e) {
                    logger.warn("Failed to fetch from SerpApi: {}", e.getMessage());
                }
            }

            // If SerpApi fails, try Adzuna
            if (!success) {
                try {
                    success = fetchJobsFromAdzuna(searchQuery, jobs);
                } catch (Exception e) {
                    logger.warn("Failed to fetch from Adzuna: {}", e.getMessage());
                }
            }

            logger.info("Found {} jobs from API(s)", jobs.size());

        } catch (Exception e) {
            logger.error("Error fetching jobs from API: {}", e.getMessage(), e);
        }

        return jobs;
    }

    private boolean fetchJobsFromJsearch(String searchQuery, List<JobListing> jobs) {
        try {
            String apiKey = apiKeysConfig.getJsearchApiKey();
            if (apiKey == null || apiKey.isEmpty() || "YOUR_JSEARCH_API_KEY".equals(apiKey)) {
                logger.warn("JSearch API key not configured. Skipping JSearch API.");
                return false;
            }

            // Set up request headers for RapidAPI
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", RAPID_API_HOST);

            Map<String, String> params = new HashMap<>();
            params.put("query", searchQuery);
            params.put("page", "1");
            params.put("num_pages", "1");

            // If you need to provide API key or other params in URL parameters
            String requestUrl = JSEARCH_API_URL + "?query=" + searchQuery + "&page=1&num_pages=1";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                processJsearchResponse(responseBody, jobs);
                return !jobs.isEmpty();
            } else {
                logger.warn("JSearch API returned non-success status: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error fetching from JSearch API: {}", e.getMessage(), e);
            return false;
        }
    }

    private void processJsearchResponse(String responseBody, List<JobListing> jobs) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode != null && dataNode.isArray()) {
            for (JsonNode jobNode : dataNode) {
                JobListing job = new JobListing();

                if (jobNode.has("job_title")) {
                    job.setTitle(jobNode.get("job_title").asText());
                }

                if (jobNode.has("employer_name")) {
                    job.setCompany(jobNode.get("employer_name").asText());
                }

                if (jobNode.has("job_city") && jobNode.has("job_state")) {
                    job.setLocation(jobNode.get("job_city").asText() + ", " + jobNode.get("job_state").asText());
                }

                if (jobNode.has("job_apply_link")) {
                    job.setJobUrl(jobNode.get("job_apply_link").asText());
                }

                if (jobNode.has("job_posted_at_timestamp")) {
                    long timestamp = jobNode.get("job_posted_at_timestamp").asLong();
                    job.setPostedDate(formatTimestamp(timestamp));
                }

                if (jobNode.has("job_employment_type")) {
                    job.setJobType(jobNode.get("job_employment_type").asText());
                }

                if (jobNode.has("job_min_salary") && jobNode.has("job_max_salary")) {
                    String salary = "$" + jobNode.get("job_min_salary").asText() +
                            " - $" + jobNode.get("job_max_salary").asText();
                    job.setSalary(salary);
                }

                if (jobNode.has("job_description")) {
                    String description = jobNode.get("job_description").asText();
                    if (description.length() > 200) {
                        description = description.substring(0, 197) + "...";
                    }
                    job.setDescription(description);
                }

                job.setSource("JSearch API");

                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } else {
            logger.warn("JSearch response had unexpected format");
        }
    }

    private boolean fetchJobsFromSerpApi(String searchQuery, List<JobListing> jobs) {
        try {
            String apiKey = apiKeysConfig.getSerpApiKey();
            if (apiKey == null || apiKey.isEmpty() || "YOUR_SERPAPI_KEY".equals(apiKey)) {
                logger.warn("SerpApi key not configured. Skipping SerpApi.");
                return false;
            }

            String url = String.format(SERPAPI_URL, searchQuery, apiKey);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                processSerpApiResponse(responseBody, jobs);
                return !jobs.isEmpty();
            } else {
                logger.warn("SerpApi returned non-success status: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error fetching from SerpApi: {}", e.getMessage(), e);
            return false;
        }
    }

    private void processSerpApiResponse(String responseBody, List<JobListing> jobs) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode jobsNode = rootNode.get("jobs_results");

        if (jobsNode != null && jobsNode.isArray()) {
            for (JsonNode jobNode : jobsNode) {
                JobListing job = new JobListing();

                if (jobNode.has("title")) {
                    job.setTitle(jobNode.get("title").asText());
                }

                if (jobNode.has("company_name")) {
                    job.setCompany(jobNode.get("company_name").asText());
                }

                if (jobNode.has("location")) {
                    job.setLocation(jobNode.get("location").asText());
                }

                if (jobNode.has("via")) {
                    job.setJobUrl("https://www.google.com/search?q=" + job.getTitle() + " " + job.getCompany());
                }

                if (jobNode.has("detected_extensions") && jobNode.get("detected_extensions").has("posted_at")) {
                    job.setPostedDate(jobNode.get("detected_extensions").get("posted_at").asText());
                }

                if (jobNode.has("detected_extensions") && jobNode.get("detected_extensions").has("schedule_type")) {
                    job.setJobType(jobNode.get("detected_extensions").get("schedule_type").asText());
                }

                if (jobNode.has("detected_extensions") && jobNode.get("detected_extensions").has("salary")) {
                    job.setSalary(jobNode.get("detected_extensions").get("salary").asText());
                }

                if (jobNode.has("snippet")) {
                    String description = jobNode.get("snippet").asText();
                    if (description.length() > 200) {
                        description = description.substring(0, 197) + "...";
                    }
                    job.setDescription(description);
                }

                job.setSource("SerpApi");

                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } else {
            logger.warn("SerpApi response had unexpected format");
        }
    }

    private boolean fetchJobsFromAdzuna(String searchQuery, List<JobListing> jobs) {
        try {
            String appId = apiKeysConfig.getAdzunaAppId();
            String appKey = apiKeysConfig.getAdzunaAppKey();

            if (appId == null || appId.isEmpty() || "YOUR_ADZUNA_APP_ID".equals(appId) ||
                    appKey == null || appKey.isEmpty() || "YOUR_ADZUNA_APP_KEY".equals(appKey)) {
                logger.warn("Adzuna API credentials not configured. Skipping Adzuna API.");
                return false;
            }

            String url = String.format(ADZUNA_URL, appId, appKey, searchQuery);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                processAdzunaResponse(responseBody, jobs);
                return !jobs.isEmpty();
            } else {
                logger.warn("Adzuna API returned non-success status: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error fetching from Adzuna API: {}", e.getMessage(), e);
            return false;
        }
    }

    private String formatTimestamp(long timestamp) {
        // Simple formatting - you can improve this as needed
        long now = System.currentTimeMillis() / 1000;
        long diff = now - timestamp;

        if (diff < 3600) {
            return (diff / 60) + " minutes ago";
        } else if (diff < 86400) {
            return (diff / 3600) + " hours ago";
        } else {
            return (diff / 86400) + " days ago";
        }
    }

    private void processAdzunaResponse(String responseBody, List<JobListing> jobs) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode resultsNode = rootNode.get("results");

        if (resultsNode != null && resultsNode.isArray()) {
            for (JsonNode jobNode : resultsNode) {
                JobListing job = new JobListing();

                if (jobNode.has("title")) {
                    job.setTitle(jobNode.get("title").asText());
                }

                if (jobNode.has("company")) {
                    job.setCompany(jobNode.get("company").get("display_name").asText());
                }

                if (jobNode.has("location")) {
                    JsonNode locationNode = jobNode.get("location");
                    if (locationNode.has("display_name")) {
                        job.setLocation(locationNode.get("display_name").asText());
                    } else if (locationNode.has("area")) {
                        JsonNode areaNode = locationNode.get("area");
                        if (areaNode.isArray() && areaNode.size() > 0) {
                            job.setLocation(areaNode.get(areaNode.size() - 1).asText());
                        }
                    }
                }

                if (jobNode.has("redirect_url")) {
                    job.setJobUrl(jobNode.get("redirect_url").asText());
                }

                if (jobNode.has("created")) {
                    job.setPostedDate(jobNode.get("created").asText());
                }

                if (jobNode.has("contract_type")) {
                    job.setJobType(jobNode.get("contract_type").asText());
                }

                if (jobNode.has("salary_min") && jobNode.has("salary_max")) {
                    String salary = "$" + Math.round(jobNode.get("salary_min").asDouble()) +
                            " - $" + Math.round(jobNode.get("salary_max").asDouble());
                    job.setSalary(salary);
                }

                if (jobNode.has("description")) {
                    String description = jobNode.get("description").asText();
                    if (description.length() > 200) {
                        description = description.substring(0, 197) + "...";
                    }
                    job.setDescription(description);
                }

                job.setSource("Adzuna");

                if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                    jobs.add(job);
                }
            }
        } else {
            logger.warn("Adzuna API response had unexpected format");
        }
    }
}