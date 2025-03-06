package com.linkedin.jobSearch.linkedin_job_finder.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for loading API keys from application.properties
 */
@Configuration
public class ApiKeysConfig {

    @Value("${jooble.api.key:}")
    private String joobleApiKey;

    @Value("${jsearch.api.key:}")
    private String jsearchApiKey;

    @Value("${serpapi.api.key:}")
    private String serpApiKey;

    @Value("${adzuna.app.id:}")
    private String adzunaAppId;

    @Value("${adzuna.app.key:}")
    private String adzunaAppKey;

    public String getJoobleApiKey() {
        return joobleApiKey;
    }

    public String getJsearchApiKey() {
        return jsearchApiKey;
    }

    public String getSerpApiKey() {
        return serpApiKey;
    }

    public String getAdzunaAppId() {
        return adzunaAppId;
    }

    public String getAdzunaAppKey() {
        return adzunaAppKey;
    }
}