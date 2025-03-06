package com.linkedin.jobSearch.linkedin_job_finder.model;

import lombok.Data;

@Data
public class JobListing {
    private String title;
    private String company;
    private String location;
    private String jobUrl;
    private String postedDate;
    private String salary;
    private String jobType;    // Full-time, Part-time, Contract, etc.
    private String description; // Brief job description if available
    private String source;     // Which site the job was scraped from
}