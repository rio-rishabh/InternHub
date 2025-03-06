package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import java.util.List;

/**
 * Interface for job scraping services
 * This interface defines the contract for all job scraping implementations,
 * whether they use Selenium, HTTP clients, or mock data.
 */
public interface JobScraperService {
    /**
     * Scrape jobs based on search keywords
     *
     * @param searchQuery The search keywords
     * @return List of job listings matching the search criteria
     */
    List<JobListing> scrapeJobs(String searchQuery);
}