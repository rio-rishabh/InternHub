////package com.linkedin.jobSearch.linkedin_job_finder.service;
////
////import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
////import org.openqa.selenium.*;
////import org.openqa.selenium.chrome.ChromeDriver;
////import org.openqa.selenium.chrome.ChromeOptions;
////import org.openqa.selenium.support.ui.ExpectedConditions;
////import org.openqa.selenium.support.ui.WebDriverWait;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.context.annotation.Primary;
////import org.springframework.stereotype.Service;
////
////import java.time.Duration;
////import java.util.*;
////
/////**
//// * Service implementation that scrapes job listings from LinkedIn.
//// * This implementation uses public job listings that don't require login.
//// */
////@Service
////@Primary
////public class LinkedInScraperServiceImpl_OLD implements JobScraperService {
////    private static final Logger logger = LoggerFactory.getLogger(LinkedInScraperServiceImpl_OLD.class);
////
////    // Base URL for LinkedIn job search
////    private static final String LINKEDIN_JOBS_URL = "https://www.linkedin.com/jobs/search/?keywords=%s&location=United%%20States&f_TPR=r86400";
////
////    // Maximum number of jobs to scrape
////    private static final int MAX_JOBS = 25;
////
////    // Time to wait between actions (ms)
////    private static final int MIN_WAIT = 1000;
////    private static final int MAX_WAIT = 3000;
////
////    @Override
////    public List<JobListing> scrapeJobs(String searchQuery) {
////        logger.info("Starting LinkedIn job scraping for query: {}", searchQuery);
////        List<JobListing> jobs = new ArrayList<>();
////        WebDriver driver = null;
////
////        try {
////            // Initialize driver directly - don't use WebDriverManager
////            driver = setupWebDriver();
////
////            // Navigate to search URL with encoded query
////            String encodedQuery = searchQuery.replace(" ", "%20");
////            String searchUrl = String.format(LINKEDIN_JOBS_URL, encodedQuery);
////            logger.info("Navigating to: {}", searchUrl);
////            driver.get(searchUrl);
////
////            // Wait for page to load
////            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
////            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".jobs-search__results-list")));
////
////            // Random human-like delay
////            randomDelay();
////
////            // Scroll to load more jobs
////            scrollToLoadJobs(driver);
////
////            // Extract job listings
////            jobs = extractJobListings(driver, searchQuery);
////
////            logger.info("Successfully scraped {} job listings from LinkedIn", jobs.size());
////
////        } catch (Exception e) {
////            logger.error("Error scraping LinkedIn jobs: {}", e.getMessage(), e);
////        } finally {
////            if (driver != null) {
////                try {
////                    driver.quit();
////                } catch (Exception e) {
////                    logger.error("Error closing WebDriver: {}", e.getMessage());
////                }
////            }
////        }
////
////        return jobs;
////    }
////
////    /**
////     * Setup WebDriver with appropriate options to avoid detection
////     */
////    private WebDriver
////    setupWebDriver() {
////        ChromeOptions options = new ChromeOptions();
////
////        // Basic options for better performance
////        options.addArguments("--headless=new");
////        options.addArguments("--disable-gpu");
////        options.addArguments("--no-sandbox");
////        options.addArguments("--disable-dev-shm-usage");
////        options.addArguments("--window-size=1920,1080");
////
////        // Avoid detection as a bot
////        options.addArguments("--disable-blink-features=AutomationControlled");
////        options.addArguments("--disable-extensions");
////        options.addArguments("--disable-notifications");
////        options.addArguments("--lang=en-US,en");
////
////        // Set a realistic user agent
////        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
////                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
////
////        // Disable WebDriver flags
////        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
////        options.setExperimentalOption("useAutomationExtension", false);
////
////        // Disable password saving
////        Map<String, Object> prefs = new HashMap<>();
////        prefs.put("credentials_enable_service", false);
////        prefs.put("profile.password_manager_enabled", false);
////        options.setExperimentalOption("prefs", prefs);
////
////        return new ChromeDriver(options);
////    }
////
////    /**
////     * Scroll down the page to load more job listings
////     */
////    private void scrollToLoadJobs(WebDriver driver) {
////        try {
////            JavascriptExecutor js = (JavascriptExecutor) driver;
////
////            // Initial scroll to trigger job loading
////            js.executeScript("window.scrollTo(0, 300)");
////            randomDelay();
////
////            // Scroll a few times with random delays to simulate human behavior
////            for (int i = 0; i < 5; i++) {
////                js.executeScript("window.scrollBy(0, 500)");
////                randomDelay();
////            }
////
////            // Try to click "Show more jobs" button if present
////            try {
////                WebElement showMoreButton = driver.findElement(
////                        By.cssSelector("button.infinite-scroller__show-more-button"));
////                if (showMoreButton.isDisplayed()) {
////                    showMoreButton.click();
////                    randomDelay();
////
////                    // Scroll a bit more after clicking
////                    js.executeScript("window.scrollBy(0, 800)");
////                    randomDelay();
////                }
////            } catch (Exception e) {
////                logger.debug("No 'Show more' button found or unable to click it: {}", e.getMessage());
////            }
////
////        } catch (Exception e) {
////            logger.error("Error scrolling to load more jobs: {}", e.getMessage());
////        }
////    }
////
////    /**
////     * Extract job listings from the page
////     */
////    private List<JobListing> extractJobListings(WebDriver driver, String searchQuery) {
////        List<JobListing> jobs = new ArrayList<>();
////
////        try {
////            // Find all job cards
////            List<WebElement> jobCards = driver.findElements(
////                    By.cssSelector(".jobs-search__results-list > li"));
////
////            logger.info("Found {} job cards on the page", jobCards.size());
////
////            // Process each job card
////            int count = 0;
////            for (WebElement card : jobCards) {
////                try {
////                    JobListing job = new JobListing();
////
////                    // Extract job title
////                    job.setTitle(getTextSafely(card, ".base-search-card__title"));
////
////                    // Extract company name
////                    job.setCompany(getTextSafely(card, ".base-search-card__subtitle"));
////
////                    // Extract location
////                    job.setLocation(getTextSafely(card, ".job-search-card__location"));
////
////                    // Extract job URL
////                    job.setJobUrl(getAttributeSafely(card, "a", "href"));
////
////                    // Extract posted date
////                    job.setPostedDate(getTextSafely(card, ".job-search-card__listdate"));
////
////                    // Set source
////                    job.setSource("LinkedIn");
////
////                    // Extract additional fields when possible
////                    extractAdditionalJobDetails(driver, job);
////
////                    // Add to results if we have at least a title and company
////                    if (isValidJob(job)) {
////                        jobs.add(job);
////                        count++;
////                        logger.debug("Added job: {}", job.getTitle());
////                    }
////
////                    // Limit the number of jobs
////                    if (count >= MAX_JOBS) {
////                        break;
////                    }
////
////                } catch (Exception e) {
////                    logger.error("Error extracting job data: {}", e.getMessage());
////                }
////            }
////
////        } catch (Exception e) {
////            logger.error("Error finding job cards: {}", e.getMessage());
////        }
////
////        // If no jobs were found, try the alternative extraction method
////        if (jobs.isEmpty()) {
////            logger.info("No jobs found with primary method, trying alternative extraction");
////            jobs = extractJobsAlternativeMethod(driver, searchQuery);
////        }
////
////        return jobs;
////    }
////
////    /**
////     * Alternative method to extract jobs when the primary method fails
////     */
////    private List<JobListing> extractJobsAlternativeMethod(WebDriver driver, String searchQuery) {
////        List<JobListing> jobs = new ArrayList<>();
////
////        try {
////            // Try different selectors for job cards
////            List<WebElement> jobCards = driver.findElements(
////                    By.cssSelector(".job-card-container, .job-search-card"));
////
////            logger.info("Found {} job cards with alternative method", jobCards.size());
////
////            // Process each card
////            int count = 0;
////            for (WebElement card : jobCards) {
////                try {
////                    JobListing job = new JobListing();
////
////                    // Try multiple selectors for each field
////                    job.setTitle(getTextWithMultipleSelectors(card, Arrays.asList(
////                            ".job-card-list__title",
////                            ".base-search-card__title",
////                            "h3")));
////
////                    job.setCompany(getTextWithMultipleSelectors(card, Arrays.asList(
////                            ".job-card-container__company-name",
////                            ".base-search-card__subtitle",
////                            ".job-card-container__primary-description")));
////
////                    job.setLocation(getTextWithMultipleSelectors(card, Arrays.asList(
////                            ".job-card-container__metadata-item",
////                            ".job-search-card__location",
////                            ".job-card-container__secondary-description")));
////
////                    job.setJobUrl(getAttributeWithMultipleSelectors(card, "a", "href"));
////
////                    // Set defaults
////                    job.setPostedDate("Recently");
////                    job.setSource("LinkedIn");
////
////                    // Add if valid
////                    if (isValidJob(job)) {
////                        jobs.add(job);
////                        count++;
////                    }
////
////                    if (count >= MAX_JOBS) {
////                        break;
////                    }
////
////                } catch (Exception e) {
////                    logger.error("Error in alternative extraction: {}", e.getMessage());
////                }
////            }
////
////        } catch (Exception e) {
////            logger.error("Alternative extraction failed: {}", e.getMessage());
////        }
////
////        // If still no jobs found, fallback to a minimal extraction
////        if (jobs.isEmpty()) {
////            logger.info("Using minimal fallback extraction method");
////            jobs = extractJobsMinimalFallback(driver, searchQuery);
////        }
////
////        return jobs;
////    }
////
////    /**
////     * Minimal fallback method when other methods fail
////     */
////    private List<JobListing> extractJobsMinimalFallback(WebDriver driver, String searchQuery) {
////        List<JobListing> jobs = new ArrayList<>();
////
////        try {
////            // Just look for any links on the page that might be job listings
////            List<WebElement> links = driver.findElements(By.tagName("a"));
////
////            for (WebElement link : links) {
////                try {
////                    String href = link.getAttribute("href");
////                    String text = link.getText();
////
////                    // Check if this might be a job link
////                    if (href != null && href.contains("/jobs/view/") && !text.isEmpty()) {
////                        JobListing job = new JobListing();
////                        job.setTitle(text);
////                        job.setJobUrl(href);
////                        job.setCompany("LinkedIn Job");
////                        job.setLocation("Unknown Location");
////                        job.setSource("LinkedIn");
////
////                        if (!jobs.contains(job)) {
////                            jobs.add(job);
////                        }
////
////                        if (jobs.size() >= 10) {
////                            break;
////                        }
////                    }
////                } catch (Exception e) {
////                    // Continue to next link
////                }
////            }
////
////        } catch (Exception e) {
////            logger.error("Minimal fallback extraction failed: {}", e.getMessage());
////        }
////
////        return jobs;
////    }
////
////    /**
////     * Try to extract additional job details by examining the job cards more closely
////     */
////    private void extractAdditionalJobDetails(WebDriver driver, JobListing job) {
////        try {
////            // Try to find salary information
////            String salarySelector = ".job-search-card__salary-info";
////            List<WebElement> salaryElements = driver.findElements(By.cssSelector(salarySelector));
////
////            for (WebElement element : salaryElements) {
////                try {
////                    String salaryText = element.getText().trim();
////                    if (!salaryText.isEmpty()) {
////                        job.setSalary(salaryText);
////                        break;
////                    }
////                } catch (Exception e) {
////                    // Try next element
////                }
////            }
////
////            // Try to determine job type from the title or other elements
////            if (job.getTitle() != null) {
////                String title = job.getTitle().toLowerCase();
////                if (title.contains("full-time") || title.contains("full time")) {
////                    job.setJobType("Full-time");
////                } else if (title.contains("part-time") || title.contains("part time")) {
////                    job.setJobType("Part-time");
////                } else if (title.contains("contract")) {
////                    job.setJobType("Contract");
////                } else if (title.contains("intern")) {
////                    job.setJobType("Internship");
////                } else {
////                    // Default job type
////                    job.setJobType("Full-time");
////                }
////            }
////
////        } catch (Exception e) {
////            logger.debug("Error extracting additional details: {}", e.getMessage());
////        }
////    }
////
////    /**
////     * Check if a job has the minimum required fields
////     */
////    private boolean isValidJob(JobListing job) {
////        return job.getTitle() != null && !job.getTitle().isEmpty() &&
////                job.getCompany() != null && !job.getCompany().isEmpty();
////    }
////
////    /**
////     * Safely get text from an element
////     */
////    private String getTextSafely(WebElement parent, String selector) {
////        try {
////            WebElement element = parent.findElement(By.cssSelector(selector));
////            return element.getText().trim();
////        } catch (Exception e) {
////            return "";
////        }
////    }
////
////    /**
////     * Safely get attribute from an element
////     */
////    private String getAttributeSafely(WebElement parent, String tagName, String attribute) {
////        try {
////            WebElement element = parent.findElement(By.tagName(tagName));
////            return element.getAttribute(attribute);
////        } catch (Exception e) {
////            return "";
////        }
////    }
////
////    /**
////     * Try multiple selectors to extract text
////     */
////    private String getTextWithMultipleSelectors(WebElement parent, List<String> selectors) {
////        for (String selector : selectors) {
////            try {
////                WebElement element = parent.findElement(By.cssSelector(selector));
////                String text = element.getText().trim();
////                if (!text.isEmpty()) {
////                    return text;
////                }
////            } catch (Exception e) {
////                // Try next selector
////            }
////        }
////        return "";
////    }
////
////    /**
////     * Try multiple selectors to get an attribute
////     */
////    private String getAttributeWithMultipleSelectors(WebElement parent, String tagName, String attribute) {
////        try {
////            WebElement element = parent.findElement(By.tagName(tagName));
////            return element.getAttribute(attribute);
////        } catch (Exception e) {
////            try {
////                List<WebElement> elements = parent.findElements(By.tagName("a"));
////                if (!elements.isEmpty()) {
////                    return elements.get(0).getAttribute(attribute);
////                }
////            } catch (Exception ex) {
////                // Ignore
////            }
////        }
////        return "";
////    }
////
////    /**
////     * Introduce random delay to mimic human behavior
////     */
////    private void randomDelay() {
////        try {
////            Random random = new Random();
////            int delay = MIN_WAIT + random.nextInt(MAX_WAIT - MIN_WAIT);
////            Thread.sleep(delay);
////        } catch (InterruptedException e) {
////            Thread.currentThread().interrupt();
////        }
////    }
////}
//
//
//
//package com.linkedin.jobSearch.linkedin_job_finder.service;
//
//import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.openqa.selenium.By;
//import org.openqa.selenium.JavascriptExecutor;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@Primary
//public class LinkedInScraperServiceImpl_OLD implements JobScraperService {
//    private static final Logger logger = LoggerFactory.getLogger(LinkedInScraperServiceImpl_OLD.class);
//
//    // Using a more reliable search URL for job listings
//    private static final String GOOGLE_JOBS_URL = "https://www.google.com/search?q=%s+jobs&ibp=htl;jobs";
//
//    @Override
//    public List<JobListing> scrapeJobs(String searchQuery) {
//        List<JobListing> jobs = new ArrayList<>();
//        WebDriver driver = null;
//
//        try {
//            // Initialize WebDriverManager instead of setting system property manually
//            WebDriverManager.chromedriver().setup();
//
//            driver = setupWebDriver();
//
//            // Format and navigate to search URL
//            String formattedQuery = searchQuery.replace(" ", "+");
//            String url = String.format(GOOGLE_JOBS_URL, formattedQuery);
//            logger.info("Navigating to URL: {}", url);
//            driver.get(url);
//
//            // Wait for page to load
//            Thread.sleep(3000);
//
//            // Scroll down to load more content
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            js.executeScript("window.scrollTo(0, document.body.scrollHeight/2)");
//            Thread.sleep(2000);
//
//            // Look for job elements with multiple selectors to adapt to Google's changing structure
//            List<WebElement> jobElements = new ArrayList<>();
//            List<String> possibleSelectors = List.of(
//                    "div.gws-plugins-horizon-jobs__li",
//                    "ul.fjE9Db li",
//                    "div.PwjeAc",
//                    "div.BjJfJf",
//                    "div.g"
//            );
//
//            for (String selector : possibleSelectors) {
//                try {
//                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
//                    if (!elements.isEmpty()) {
//                        jobElements = elements;
//                        logger.info("Found {} job elements with selector: {}", elements.size(), selector);
//                        break;
//                    }
//                } catch (Exception e) {
//                    logger.debug("Selector {} failed: {}", selector, e.getMessage());
//                }
//            }
//
//            if (jobElements.isEmpty()) {
//                // Fallback approach - get page structure for debugging
//                logger.warn("No job elements found with standard selectors");
//                String pageSource = driver.getPageSource();
//                logger.debug("Page structure hint: {}", pageSource.length() > 1000 ?
//                        pageSource.substring(0, 1000) + "..." : pageSource);
//
//                // Try a more generic approach
//                jobElements = driver.findElements(By.cssSelector("div[role='article'], div.job-item, li.job-result"));
//                logger.info("Using fallback selectors, found {} elements", jobElements.size());
//            }
//
//            // Process job elements
//            for (WebElement element : jobElements) {
//                try {
//                    JobListing job = new JobListing();
//
//                    // Try to extract title with multiple possible selectors
//                    String title = extractTextWithMultipleSelectors(element,
//                            List.of("h3", "div[role='heading']", ".KLsYvd", ".BjJfJf", ".JobTitle", ".title"));
//                    job.setTitle(title);
//
//                    // Try to extract company
//                    String company = extractTextWithMultipleSelectors(element,
//                            List.of(".YgLbBe", ".nJlQNd", ".vNEEBe", ".company", ".employer"));
//                    job.setCompany(company);
//
//                    // Try to extract location
//                    String location = extractTextWithMultipleSelectors(element,
//                            List.of(".Qk80Jf", ".Es2jea", ".HBvzbc", ".location", "div.location"));
//                    job.setLocation(location);
//
//                    // Try to extract posted date
//                    String postedDate = extractTextWithMultipleSelectors(element,
//                            List.of(".LL4CDc", ".SuWscb", ".date", ".posted-date"));
//                    job.setPostedDate(postedDate);
//
//                    // Try to extract description, even a snippet
//                    String description = extractTextWithMultipleSelectors(element,
//                            List.of(".HBvzbc", ".job-snippet", ".description"));
//                    job.setDescription(description);
//
//                    // Extract URL if possible, otherwise use the search URL
//                    String jobUrl = extractAttributeWithMultipleSelectors(element, "a", "href",
//                            List.of("a", "a.jobtitle", "a.job-link"));
//                    job.setJobUrl(jobUrl != null && !jobUrl.isEmpty() ? jobUrl : url);
//
//                    // Set source
//                    job.setSource("Google Jobs");
//
//                    // Add job to list if it has at least a title or company
//                    if ((job.getTitle() != null && !job.getTitle().isEmpty()) ||
//                            (job.getCompany() != null && !job.getCompany().isEmpty())) {
//                        jobs.add(job);
//                        logger.debug("Added job: {}", job.getTitle());
//                    }
//
//                } catch (Exception e) {
//                    logger.error("Error extracting job data: {}", e.getMessage());
//                }
//
//                // Limit to 20 jobs to avoid overwhelming the response
//                if (jobs.size() >= 20) break;
//            }
//
//        } catch (Exception e) {
//            logger.error("Error in job scraping: {}", e.getMessage(), e);
//        } finally {
//            // Make sure to quit the driver to free resources
//            if (driver != null) {
//                try {
//                    driver.quit();
//                } catch (Exception e) {
//                    logger.error("Error closing WebDriver: {}", e.getMessage());
//                }
//            }
//        }
//
//        // If no jobs found from scraping, log that we're using fallback mechanism
//        if (jobs.isEmpty()) {
//            logger.info("No jobs found from scraping. Using fallback service.");
//            // Let the fallback implementation handle this case via Spring's autowiring
//        }
//
//        return jobs;
//    }
//
//    private WebDriver setupWebDriver() {
//        ChromeOptions options = new ChromeOptions();
//
//        // Basic Chrome options for headless operation
//        options.addArguments("--headless=new");  // Modern headless mode
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--window-size=1920,1080");
//
//        // Anti-detection measures
//        options.addArguments("--disable-blink-features=AutomationControlled");
//        options.addArguments("--disable-notifications");
//        options.addArguments("--lang=en-US,en");
//
//        // Set a realistic user agent
//        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
//                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
//
//        return new ChromeDriver(options);
//    }
//
//    private String extractTextWithMultipleSelectors(WebElement parent, List<String> selectors) {
//        for (String selector : selectors) {
//            try {
//                WebElement element = parent.findElement(By.cssSelector(selector));
//                String text = element.getText().trim();
//                if (!text.isEmpty()) {
//                    return text;
//                }
//            } catch (Exception e) {
//                // Continue to next selector silently
//            }
//        }
//        return "";
//    }
//
//    private String extractAttributeWithMultipleSelectors(WebElement parent,
//                                                         String tagName,
//                                                         String attribute,
//                                                         List<String> selectors) {
//        for (String selector : selectors) {
//            try {
//                WebElement element = parent.findElement(By.cssSelector(selector));
//                String value = element.getAttribute(attribute);
//                if (value != null && !value.isEmpty()) {
//                    return value;
//                }
//            } catch (Exception e) {
//                // Continue to next selector silently
//            }
//        }
//        return "";
//    }
//}






package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Qualifier("linkedInScraperService")
public class LinkedInScraperServiceImpl_OLD implements JobScraperService {
    private static final Logger logger = LoggerFactory.getLogger(LinkedInScraperServiceImpl_OLD.class);

    // Use multiple job search sites for better results
    private static final String LINKEDIN_JOBS_URL = "https://www.linkedin.com/jobs/search/?keywords=%s";
    private static final String INDEED_JOBS_URL = "https://www.indeed.com/jobs?q=%s";
    private static final String GLASSDOOR_JOBS_URL = "https://www.glassdoor.com/Job/jobs.htm?sc.keyword=%s";
    private static final String MONSTER_JOBS_URL = "https://www.monster.com/jobs/search?q=%s";
    private static final String SIMPLY_HIRED_URL = "https://www.simplyhired.com/search?q=%s";
    private static final String HANDSHAKE_URL = "https://www.hanshake.com/search?q=%s";

    @Override
    public List<JobListing> scrapeJobs(String searchQuery) {
        List<JobListing> jobs = new ArrayList<>();
        WebDriver driver = null;

        try {
            WebDriverManager.chromedriver().setup();
            driver = setupWebDriver();

            // Try multiple job sites
            boolean success = false;

            // Try Simple Hired first (often less strict with scraping)
            success = trySimplyHired(driver, searchQuery, jobs);

            // If no success with SimplyHired, try Indeed
            if (!success) {
                success = tryIndeed(driver, searchQuery, jobs);
            }

            // If no success with Indeed, try Monster
            if (!success) {
                success = tryMonster(driver, searchQuery, jobs);
            }

        } catch (Exception e) {
            logger.error("Error in job scraping: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.error("Error closing WebDriver: {}", e.getMessage());
                }
            }
        }

        if (jobs.isEmpty()) {
            logger.info("No jobs found from scraping.");
        } else {
            logger.info("Successfully scraped {} jobs", jobs.size());
        }

        return jobs;
    }

    private boolean tryHandShake(WebDriver driver, String searchQuery, List<JobListing> jobs) {
        try{
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String url = String.format(HANDSHAKE_URL, encodedQuery);
            logger.info("Handshake URL: {}", url);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));


            handlePopups(driver);

            scrollPageToLoadContent(driver);

            List<WebElement> jobCards = driver.findElements(
                    By.cssSelector(".SerpJob-jobCard, .jobs .job"));

            logger.info("Found {} potential job elements on HandShake", jobCards.size());

            if (jobCards.isEmpty()) {
                // Try alternative selectors
                jobCards = driver.findElements(By.cssSelector(".jobposting-widget, .card, article"));
                logger.info("Found {} jobs with alternative selectors", jobCards.size());
            }

            for (WebElement card : jobCards) {
                try {
                    JobListing job = new JobListing();

                    // Extract job details - SimplyHired
                    job.setTitle(getTextFromElement(card, ".jobTitle, .CardJobTitle, h2"));
                    job.setCompany(getTextFromElement(card, ".companyName, .company, span.company"));
                    job.setLocation(getTextFromElement(card, ".location, .jobLocation"));
                    job.setPostedDate(getTextFromElement(card, ".datePosted, .postDate"));
                    job.setJobType(getTextFromElement(card, ".jobType, .type, .employmentType"));
                    job.setSalary(getTextFromElement(card, ".salary, .jobSalary"));
                    job.setDescription(getTextFromElement(card, ".jobDescriptionSnippet, .snippet, .description"));
                    job.setSource("Handshake");

                    // Get URL
                    WebElement linkElement = card.findElement(By.cssSelector("a"));
                    if (linkElement != null) {
                        job.setJobUrl(linkElement.getAttribute("href"));
                    } else {
                        job.setJobUrl(url);
                    }

                    // Add job if it has at least a title
                    if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                        jobs.add(job);
                        logger.debug("Added job from Handshake: {}", job.getTitle());

                        // For debugging
                        if (jobs.size() == 1) {
                            logger.info("First job found: {} at {}", job.getTitle(), job.getCompany());
                        }
                    }

                } catch (Exception e) {
                    logger.debug("Error extracting Handshake job: {}", e.getMessage());
                }
            }

            return !jobs.isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to scrape from Handshake: {}", e.getMessage());
            return false;
        }
    }

    private boolean trySimplyHired(WebDriver driver, String searchQuery, List<JobListing> jobs) {
        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String url = String.format(SIMPLY_HIRED_URL, encodedQuery);
            logger.info("Trying SimplyHired: {}", url);

            driver.get(url);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Handle any potential cookie dialogs or popups
            handlePopups(driver);

            // Scroll down to load more content
            scrollPageToLoadContent(driver);

            // SimplyHired job cards
            List<WebElement> jobCards = driver.findElements(
                    By.cssSelector(".SerpJob-jobCard, .jobs .job"));

            logger.info("Found {} potential job elements on SimplyHired", jobCards.size());

            if (jobCards.isEmpty()) {
                // Try alternative selectors
                jobCards = driver.findElements(By.cssSelector(".jobposting-widget, .card, article"));
                logger.info("Found {} jobs with alternative selectors", jobCards.size());
            }

            for (WebElement card : jobCards) {
                try {
                    JobListing job = new JobListing();

                    // Extract job details - SimplyHired
                    job.setTitle(getTextFromElement(card, ".jobTitle, .CardJobTitle, h2"));
                    job.setCompany(getTextFromElement(card, ".companyName, .company, span.company"));
                    job.setLocation(getTextFromElement(card, ".location, .jobLocation"));
                    job.setPostedDate(getTextFromElement(card, ".datePosted, .postDate"));
                    job.setJobType(getTextFromElement(card, ".jobType, .type, .employmentType"));
                    job.setSalary(getTextFromElement(card, ".salary, .jobSalary"));
                    job.setDescription(getTextFromElement(card, ".jobDescriptionSnippet, .snippet, .description"));
                    job.setSource("SimplyHired");

                    // Get URL
                    WebElement linkElement = card.findElement(By.cssSelector("a"));
                    if (linkElement != null) {
                        job.setJobUrl(linkElement.getAttribute("href"));
                    } else {
                        job.setJobUrl(url);
                    }

                    // Add job if it has at least a title
                    if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                        jobs.add(job);
                        logger.debug("Added job from SimplyHired: {}", job.getTitle());

                        // For debugging
                        if (jobs.size() == 1) {
                            logger.info("First job found: {} at {}", job.getTitle(), job.getCompany());
                        }
                    }

                } catch (Exception e) {
                    logger.debug("Error extracting SimplyHired job: {}", e.getMessage());
                }
            }

            return !jobs.isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to scrape from SimplyHired: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryIndeed(WebDriver driver, String searchQuery, List<JobListing> jobs) {
        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String url = String.format(INDEED_JOBS_URL, encodedQuery);
            logger.info("Trying Indeed: {}", url);

            driver.get(url);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Handle any potential cookie dialogs or popups
            handlePopups(driver);

            // Scroll down to load more content
            scrollPageToLoadContent(driver);

            // Indeed job listings
            List<WebElement> jobCards = driver.findElements(
                    By.cssSelector(".job_seen_beacon, .resultContent, .jobCard, [data-testid='jobListing']"));

            logger.info("Found {} potential job elements on Indeed", jobCards.size());

            if (jobCards.isEmpty()) {
                // Try alternative selectors
                jobCards = driver.findElements(By.cssSelector(".tapItem, .job_result, div.job"));
                logger.info("Found {} jobs with alternative selectors", jobCards.size());
            }

            for (WebElement card : jobCards) {
                try {
                    JobListing job = new JobListing();

                    // Extract job details - Indeed
                    job.setTitle(getTextFromElement(card, "h2.jobTitle, a.jcs-JobTitle, .title"));
                    job.setCompany(getTextFromElement(card, ".companyName, span.companyName, .company"));
                    job.setLocation(getTextFromElement(card, ".companyLocation, .location"));
                    job.setPostedDate(getTextFromElement(card, ".date, .new, .jobAge"));
                    job.setJobType(getTextFromElement(card, ".attribute_snippet, .jobType, .type"));
                    job.setSalary(getTextFromElement(card, ".salary-snippet-container, .salaryText"));
                    job.setDescription(getTextFromElement(card, ".job-snippet, .summary"));
                    job.setSource("Indeed");

                    // Get job URL
                    try {
                        WebElement linkElement = card.findElement(By.cssSelector("a"));
                        String href = linkElement.getAttribute("href");
                        if (href != null && !href.isEmpty()) {
                            job.setJobUrl(href);
                        } else {
                            job.setJobUrl(url);
                        }
                    } catch (Exception e) {
                        job.setJobUrl(url);
                    }

                    // Add job if it has at least a title
                    if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                        jobs.add(job);
                        logger.debug("Added job from Indeed: {}", job.getTitle());

                        // For debugging
                        if (jobs.size() == 1) {
                            logger.info("First job found: {} at {}", job.getTitle(), job.getCompany());
                        }
                    }

                } catch (Exception e) {
                    logger.debug("Error extracting Indeed job: {}", e.getMessage());
                }
            }

            return !jobs.isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to scrape from Indeed: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryMonster(WebDriver driver, String searchQuery, List<JobListing> jobs) {
        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            String url = String.format(MONSTER_JOBS_URL, encodedQuery);
            logger.info("Trying Monster: {}", url);

            driver.get(url);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // Handle any potential cookie dialogs or popups
            handlePopups(driver);

            // Scroll down to load more content
            scrollPageToLoadContent(driver);

            // Monster job cards
            List<WebElement> jobCards = driver.findElements(
                    By.cssSelector(".job-card, .card-content, .results-card"));

            logger.info("Found {} potential job elements on Monster", jobCards.size());

            if (jobCards.isEmpty()) {
                // Try alternative selectors
                jobCards = driver.findElements(By.cssSelector(".search-card, article, .job-listing"));
                logger.info("Found {} jobs with alternative selectors", jobCards.size());
            }

            for (WebElement card : jobCards) {
                try {
                    JobListing job = new JobListing();

                    // Extract job details - Monster
                    job.setTitle(getTextFromElement(card, ".title, h3.title, .job-title"));
                    job.setCompany(getTextFromElement(card, ".company, .name, .company-name"));
                    job.setLocation(getTextFromElement(card, ".location, .job-location"));
                    job.setPostedDate(getTextFromElement(card, ".date-time-ago, .posted-date"));
                    job.setJobType(getTextFromElement(card, ".job-type, .type"));
                    job.setSalary(getTextFromElement(card, ".salary, .job-salary"));
                    job.setDescription(getTextFromElement(card, ".job-description, .descriptionText"));
                    job.setSource("Monster");

                    // Get job URL
                    try {
                        WebElement linkElement = card.findElement(By.cssSelector("a"));
                        String href = linkElement.getAttribute("href");
                        if (href != null && !href.isEmpty()) {
                            job.setJobUrl(href);
                        } else {
                            job.setJobUrl(url);
                        }
                    } catch (Exception e) {
                        job.setJobUrl(url);
                    }

                    // Add job if it has at least a title
                    if (job.getTitle() != null && !job.getTitle().isEmpty()) {
                        jobs.add(job);
                        logger.debug("Added job from Monster: {}", job.getTitle());

                        // For debugging
                        if (jobs.size() == 1) {
                            logger.info("First job found: {} at {}", job.getTitle(), job.getCompany());
                        }
                    }

                } catch (Exception e) {
                    logger.debug("Error extracting Monster job: {}", e.getMessage());
                }
            }

            return !jobs.isEmpty();
        } catch (Exception e) {
            logger.warn("Failed to scrape from Monster: {}", e.getMessage());
            return false;
        }
    }

    private void handlePopups(WebDriver driver) {
        try {
            // Try handling common popup and cookie consent dialogs
            List<String> popupSelectors = List.of(
                    "button#onetrust-accept-btn-handler",
                    "button.accept-cookies",
                    "button.cookie-consent-accept",
                    "button.agree-button",
                    "button.consent-accept",
                    "button.modal-close",
                    "button.close-modal",
                    "[aria-label='Close']",
                    ".modal-close",
                    ".popup-close"
            );

            for (String selector : popupSelectors) {
                try {
                    List<WebElement> buttons = driver.findElements(By.cssSelector(selector));
                    for (WebElement button : buttons) {
                        if (button.isDisplayed() && button.isEnabled()) {
                            button.click();
                            logger.info("Clicked popup/cookie button: {}", selector);
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    // Ignore errors and try next selector
                }
            }

            // Try to handle popups by pressing ESC key
            try {
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
                Thread.sleep(500);
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            } catch (Exception e) {
                // Ignore errors
            }

        } catch (Exception e) {
            logger.debug("Error handling popups: {}", e.getMessage());
        }
    }

    private void scrollPageToLoadContent(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Scroll down in stages to simulate human behavior and load lazy content
            for (int i = 0; i < 5; i++) {
                js.executeScript("window.scrollBy(0, " + (500 + Math.random() * 300) + ")");
                Thread.sleep(500 + (int)(Math.random() * 500));
            }

            // Scroll back up a bit to trigger any potential events
            js.executeScript("window.scrollBy(0, -400)");
            Thread.sleep(500);

            // Scroll down again
            js.executeScript("window.scrollBy(0, 600)");

        } catch (Exception e) {
            logger.debug("Error scrolling page: {}", e.getMessage());
        }
    }

    private WebDriver setupWebDriver() {
        ChromeOptions options = new ChromeOptions();

        // Basic Chrome options
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        // Anti-detection measures
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=en-US,en");

        // Set a realistic user agent
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // Additional preferences to avoid detection
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.cookies", 2); // Allow cookies
        prefs.put("profile.default_content_setting_values.images", 2);  // Block images for faster loading
        prefs.put("profile.managed_default_content_settings.javascript", 1); // Allow JavaScript
        options.setExperimentalOption("prefs", prefs);

        // Mask WebDriver usage
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        ChromeDriver driver = new ChromeDriver(options);

        // Execute CDP commands to modify navigator.webdriver flag
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("source", "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", parameters);

        return driver;
    }

    private String getTextFromElement(WebElement parent, String selectors) {
        String[] selectorArray = selectors.split(", ");
        for (String selector : selectorArray) {
            try {
                WebElement element = parent.findElement(By.cssSelector(selector));
                String text = element.getText().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            } catch (Exception ignored) {
                // Continue to next selector
            }
        }
        return "";
    }
}