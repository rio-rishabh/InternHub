package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * This is a complete fallback implementation that provides realistic job listings
 * without requiring Selenium or any external dependencies.
 */
@Service
public class FallbackScraperService implements JobScraperService{
    private static final Logger logger = LoggerFactory.getLogger(FallbackScraperService.class);

    // Sample companies by industry
    private static final Map<String, String[]> COMPANIES_BY_INDUSTRY = new HashMap<>();
    static {
        COMPANIES_BY_INDUSTRY.put("tech", new String[] {
                "Google", "Microsoft", "Amazon", "Apple", "Meta", "Netflix", "Salesforce",
                "Adobe", "Twitter", "LinkedIn", "Spotify", "Oracle", "IBM", "Intel", "Cisco", "Vertex", "oak AI",
                "Trepp", "Northrop Grumman"
        });

        COMPANIES_BY_INDUSTRY.put("finance", new String[] {
                "JPMorgan Chase", "Bank of America", "Wells Fargo", "Goldman Sachs", "Morgan Stanley",
                "Citigroup", "BlackRock", "Visa", "Mastercard", "Capital One", "Fidelity"
        });

        COMPANIES_BY_INDUSTRY.put("healthcare", new String[] {
                "Johnson & Johnson", "UnitedHealth Group", "Pfizer", "Roche", "Novartis",
                "Merck", "AbbVie", "Eli Lilly", "Abbott", "CVS Health", "Medtronic"
        });

        COMPANIES_BY_INDUSTRY.put("retail", new String[] {
                "Walmart", "Amazon", "Home Depot", "Target", "Costco", "Kroger",
                "Walgreens", "CVS", "Lowe's", "Best Buy", "TJX Companies"
        });
    }

    // Sample locations
    private static final String[] LOCATIONS = {
            "New York, NY", "San Francisco, CA", "Seattle, WA", "Austin, TX", "Boston, MA",
            "Chicago, IL", "Los Angeles, CA", "Denver, CO", "Atlanta, GA", "Remote",
            "San Jose, CA", "Raleigh, NC", "Portland, OR", "Pittsburgh, PA", "Dallas, TX",
            "Washington, DC", "Phoenix, AZ", "Philadelphia, PA", "Miami, FL", "Nashville, TN"
    };

    // Job titles by category
    private static final Map<String, String[]> JOB_TITLES = new HashMap<>();
    static {
        JOB_TITLES.put("software", new String[] {
                "Software Engineer", "Senior Software Engineer", "Full Stack Developer",
                "Frontend Developer", "Backend Developer", "DevOps Engineer", "SRE",
                "Mobile Developer", "iOS Developer", "Android Developer", "Java Developer",
                "Python Developer", "C++ Developer", ".NET Developer", "Ruby Developer","Full Stack Developer Intern",
                "Java Developer Intern", "iOS Developer Intern"
        });

        JOB_TITLES.put("data", new String[] {
                "Data Scientist", "Data Engineer", "Data Analyst", "Machine Learning Engineer",
                "AI Researcher", "BI Analyst", "Database Administrator", "Big Data Engineer",
                "Data Architect", "Analytics Manager", "Statistician", "ML Operations Engineer"
        });

        JOB_TITLES.put("product", new String[] {
                "Product Manager", "Senior Product Manager", "Product Owner", "Technical Product Manager",
                "Associate Product Manager", "Director of Product", "Product Marketing Manager",
                "VP of Product", "Chief Product Officer", "Product Analyst", "Product Designer"
        });

        JOB_TITLES.put("design", new String[] {
                "UX Designer", "UI Designer", "Product Designer", "Visual Designer",
                "Interaction Designer", "UX Researcher", "Creative Director", "Graphic Designer",
                "Web Designer", "Art Director", "Brand Designer", "UX/UI Designer"
        });

        JOB_TITLES.put("finance", new String[] {
                "Financial Analyst", "Investment Banker", "Financial Advisor", "Accountant",
                "Financial Controller", "Risk Analyst", "Portfolio Manager", "Investment Analyst",
                "Treasurer", "Credit Analyst", "Auditor", "Actuary", "Underwriter"
        });

        JOB_TITLES.put("marketing", new String[] {
                "Marketing Manager", "Digital Marketing Specialist", "SEO Specialist",
                "Content Marketer", "Growth Hacker", "Social Media Manager", "Brand Manager",
                "Marketing Director", "CMO", "Email Marketing Specialist", "Marketing Analyst"
        });

        JOB_TITLES.put("healthcare", new String[] {
                "Physician", "Nurse", "Medical Technician", "Healthcare Administrator",
                "Medical Researcher", "Pharmacist", "Physical Therapist", "Dentist",
                "Medical Lab Technician", "Radiologist", "Health Information Manager"
        });

        JOB_TITLES.put("retail", new String[] {
                "Store Manager", "Retail Associate", "Sales Manager", "Merchandiser",
                "Buyer", "Visual Merchandiser", "District Manager", "E-commerce Manager",
                "Supply Chain Manager", "Inventory Specialist", "Customer Service Manager"
        });
    }

    // Sample time periods for posting dates
    private static final String[] POSTED_TIMES = {
            "Just now", "1 hour ago", "2 hours ago", "Today", "Yesterday",
            "2 days ago", "3 days ago", "4 days ago", "5 days ago",
            "1 week ago", "2 weeks ago", "3 weeks ago", "30+ days ago"
    };

    // Sample job types
    private static final String[] JOB_TYPES = {
            "Full-time", "Part-time", "Contract", "Temporary", "Internship",
            "Remote", "Hybrid", "On-site"
    };

    // Sample salary ranges based on job level
    private static final Map<String, String[]> SALARY_RANGES = new HashMap<>();
    static {
        SALARY_RANGES.put("entry", new String[] {
                "$50,000 - $70,000", "$55,000 - $75,000", "$60,000 - $80,000",
                "$45,000 - $65,000", "$40,000 - $60,000"
        });

        SALARY_RANGES.put("mid", new String[] {
                "$80,000 - $110,000", "$90,000 - $120,000", "$100,000 - $130,000",
                "$85,000 - $115,000", "$95,000 - $125,000"
        });

        SALARY_RANGES.put("senior", new String[] {
                "$120,000 - $160,000", "$130,000 - $180,000", "$140,000 - $200,000",
                "$150,000 - $220,000", "$160,000 - $250,000"
        });

        SALARY_RANGES.put("executive", new String[] {
                "$180,000 - $250,000", "$200,000 - $300,000", "$220,000 - $350,000",
                "$250,000 - $400,000", "$300,000+"
        });
    }

    @Override
    public List<JobListing> scrapeJobs(String searchQuery) {
        logger.info("Generating mock jobs for query: {}", searchQuery);

        // Determine the job category and industry based on search query
        String category = determineJobCategory(searchQuery.toLowerCase());
        String industry = determineIndustry(searchQuery.toLowerCase());

        // Generate between 5-20 jobs
        int jobCount = 5 + new Random().nextInt(16);

        List<JobListing> jobs = new ArrayList<>();
        for (int i = 0; i < jobCount; i++) {
            jobs.add(generateJobListing(searchQuery, category, industry));
        }

        logger.info("Generated {} mock jobs for query: {}", jobs.size(), searchQuery);
        return jobs;
    }

    private String determineJobCategory(String query) {
        // Check for software engineering related terms
        if (query.contains("software") || query.contains("developer") ||
                query.contains("engineer") || query.contains("programming") ||
                query.contains("java") || query.contains("python") ||
                query.contains("javascript") || query.contains("web") ||
                query.contains("code") || query.contains("coding") ||
                query.contains("app") || query.contains("application")) {
            return "software";
        }
        // Check for data science related terms
        else if (query.contains("data") || query.contains("scientist") ||
                query.contains("analytics") || query.contains("machine learning") ||
                query.contains("ai") || query.contains("statistics") ||
                query.contains("analysis") || query.contains("database")) {
            return "data";
        }
        // Check for product management related terms
        else if (query.contains("product") || query.contains("manager") ||
                query.contains("product owner") || query.contains("roadmap") ||
                query.contains("agile") || query.contains("scrum")) {
            return "product";
        }
        // Check for design related terms
        else if (query.contains("design") || query.contains("ux") ||
                query.contains("ui") || query.contains("visual") ||
                query.contains("graphic") || query.contains("creative")) {
            return "design";
        }
        // Check for marketing related terms
        else if (query.contains("marketing") || query.contains("seo") ||
                query.contains("content") || query.contains("growth") ||
                query.contains("social media") || query.contains("brand")) {
            return "marketing";
        }
        // Check for finance related terms
        else if (query.contains("finance") || query.contains("accounting") ||
                query.contains("financial") || query.contains("analyst") ||
                query.contains("investment") || query.contains("banking")) {
            return "finance";
        }
        // Check for healthcare related terms
        else if (query.contains("health") || query.contains("medical") ||
                query.contains("doctor") || query.contains("nurse") ||
                query.contains("healthcare") || query.contains("clinical")) {
            return "healthcare";
        }
        // Check for retail related terms
        else if (query.contains("retail") || query.contains("sales") ||
                query.contains("store") || query.contains("shop") ||
                query.contains("customer") || query.contains("merchandising")) {
            return "retail";
        }
        else {
            // Default to software if no match
            return "software";
        }
    }

    private String determineIndustry(String query) {
        // Tech industry
        if (query.contains("tech") || query.contains("software") ||
                query.contains("it") || query.contains("computer") ||
                query.contains("developer") || query.contains("web") ||
                query.contains("app")) {
            return "tech";
        }
        // Finance industry
        else if (query.contains("finance") || query.contains("bank") ||
                query.contains("investment") || query.contains("financial") ||
                query.contains("accounting") || query.contains("insurance")) {
            return "finance";
        }
        // Healthcare industry
        else if (query.contains("health") || query.contains("medical") ||
                query.contains("hospital") || query.contains("care") ||
                query.contains("pharma") || query.contains("medicine")) {
            return "healthcare";
        }
        // Retail industry
        else if (query.contains("retail") || query.contains("store") ||
                query.contains("shop") || query.contains("market") ||
                query.contains("consumer") || query.contains("goods")) {
            return "retail";
        }
        else {
            // Default to tech if no match
            return "tech";
        }
    }

    private String determineJobLevel(String title) {
        title = title.toLowerCase();
        if (title.contains("senior") || title.contains("lead") ||
                title.contains("principal") || title.contains("architect")) {
            return "senior";
        } else if (title.contains("manager") || title.contains("director") ||
                title.contains("head") || title.contains("vp") ||
                title.contains("chief")) {
            return "executive";
        } else if (!title.contains("junior") && !title.contains("associate") &&
                !title.contains("intern")) {
            return "mid";
        } else {
            return "entry";
        }
    }

    private JobListing generateJobListing(String searchQuery, String category, String industry) {
        Random random = new Random();
        JobListing job = new JobListing();

        // Job titles based on category
        String[] titles = JOB_TITLES.getOrDefault(category, JOB_TITLES.get("software"));
        String title = titles[random.nextInt(titles.length)];

        // Sometimes customize the title with the search query
        if (random.nextBoolean() && !searchQuery.isEmpty()) {
            String[] queryWords = searchQuery.split("\\s+");
            if (queryWords.length > 0) {
                String queryWord = queryWords[random.nextInt(queryWords.length)];
                if (queryWord.length() > 2 && !title.toLowerCase().contains(queryWord.toLowerCase())) {
                    title = title + " (" + capitalizeFirstLetter(queryWord) + ")";
                }
            }
        }

        // Add seniority level sometimes
        if (random.nextBoolean() && !title.toLowerCase().contains("senior") &&
                !title.toLowerCase().contains("junior") && !title.toLowerCase().contains("lead")) {
            String[] levels = {"Junior", "Senior", "Lead", "Principal"};
            String level = levels[random.nextInt(levels.length)];
            title = level + " " + title;
        }

        job.setTitle(title);

        // Set company based on industry
        String[] companies = COMPANIES_BY_INDUSTRY.getOrDefault(industry, COMPANIES_BY_INDUSTRY.get("tech"));
        job.setCompany(companies[random.nextInt(companies.length)]);

        // Set location
        job.setLocation(LOCATIONS[random.nextInt(LOCATIONS.length)]);

        // Posted date
        job.setPostedDate(POSTED_TIMES[random.nextInt(POSTED_TIMES.length)]);

        // Job URL - create a realistic dummy URL
        String companySlug = job.getCompany().toLowerCase().replace(" ", "-");
        String titleSlug = job.getTitle().toLowerCase().replace(" ", "-")
                .replaceAll("[^a-z0-9-]", "");
        job.setJobUrl("https://" + companySlug + ".com/careers/" + titleSlug + "-" +
                UUID.randomUUID().toString().substring(0, 8));

        // Try to add additional fields if they exist in the JobListing model
        try {
            // Try to set salary based on job level
            String jobLevel = determineJobLevel(title);
            String[] salaryRange = SALARY_RANGES.getOrDefault(jobLevel, SALARY_RANGES.get("mid"));
            job.getClass().getMethod("setSalary", String.class)
                    .invoke(job, salaryRange[random.nextInt(salaryRange.length)]);

            // Try to set job type
            job.getClass().getMethod("setJobType", String.class)
                    .invoke(job, JOB_TYPES[random.nextInt(JOB_TYPES.length)]);

            // Try to set source
            job.getClass().getMethod("setSource", String.class)
                    .invoke(job, "Mock Data");

            // Try to set description
            StringBuilder description = new StringBuilder();
            description.append("We are looking for a talented ")
                    .append(title)
                    .append(" to join our team at ")
                    .append(job.getCompany())
                    .append(". ");

            description.append("This is a great opportunity to work with cutting-edge technology and tackle challenging problems. ");
            description.append("The ideal candidate will have experience in the ")
                    .append(industry)
                    .append(" industry with strong ")
                    .append(category)
                    .append(" skills.");

            job.getClass().getMethod("setDescription", String.class)
                    .invoke(job, description.toString());

        } catch (Exception e) {
            // Fields don't exist in the model, ignore
            logger.debug("Some fields could not be set on JobListing model", e);
        }

        return job;
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}