package com.linkedin.jobSearch.linkedin_job_finder.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for API keys loaded from application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApiConfigProperties {

    private JobSearch jobSearch = new JobSearch();
    private Excel excel = new Excel();

    public JobSearch getJobSearch() {
        return jobSearch;
    }

    public void setJobSearch(JobSearch jobSearch) {
        this.jobSearch = jobSearch;
    }

    public Excel getExcel() {
        return excel;
    }

    public void setExcel(Excel excel) {
        this.excel = excel;
    }

    public static class JobSearch {
        private int defaultLimit = 20;
        private int maxLimit = 100;

        public int getDefaultLimit() {
            return defaultLimit;
        }

        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }

        public int getMaxLimit() {
            return maxLimit;
        }

        public void setMaxLimit(int maxLimit) {
            this.maxLimit = maxLimit;
        }
    }

    public static class Excel {
        private boolean exportEnabled = true;

        public boolean isExportEnabled() {
            return exportEnabled;
        }

        public void setExportEnabled(boolean exportEnabled) {
            this.exportEnabled = exportEnabled;
        }
    }
}