# LinkedIn Job Finder API

A Spring Boot application that provides REST endpoints for searching and exporting job listings from various sources.

## Features

- Search for jobs using keywords
- Multiple job source providers (Jooble API, fallback providers)
- Download job listings as Excel files
- Health check endpoint for monitoring
- Web interface for basic interaction

## Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Chrome browser (for Selenium-based fallback scraper)

### Configuration

1. Clone the repository
2. Configure API keys in `src/main/resources/application.properties`:

```properties
jooble.api.key=YOUR_JOOBLE_API_KEY
jsearch.api.key=YOUR_JSEARCH_API_KEY
serpapi.api.key=YOUR_SERPAPI_KEY
adzuna.app.id=YOUR_ADZUNA_APP_ID
adzuna.app.key=YOUR_ADZUNA_APP_KEY
```

3. Build the application with Maven:

```bash
mvn clean package
```

4. Run the application:

```bash
java -jar target/linkedin-job-finder-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Search for Jobs

```
GET /api/jobs/search?keywords={keywords}&limit={limit}
```

Parameters:
- `keywords`: Job search keywords (required)
- `limit`: Maximum number of results to return (optional, default: 20)

Example response:
```json
{
  "jobs": [
    {
      "title": "Software Engineer",
      "company": "Google",
      "location": "Mountain View, CA",
      "jobUrl": "https://example.com/job/123",
      "postedDate": "2 days ago",
      "salary": "$120,000 - $150,000",
      "jobType": "Full-time",
      "description": "We are looking for a Software Engineer to join our team...",
      "source": "Jooble"
    }
  ],
  "count": 1,
  "keywords": "software engineer",
  "executionTimeMs": 1234
}
```

### Download Jobs as Excel

```
GET /api/download/excel?keywords={keywords}&limit={limit}
```

Parameters:
- `keywords`: Job search keywords (required)
- `limit`: Maximum number of results to include (optional, default: 100)

Returns: Excel file as a downloadable attachment

### Health Check

```
GET /api/health
```

Example response:
```json
{
  "status": "UP",
  "message": "Job search API is running",
  "primaryImplementation": "JoobleApiJobScraperService",
  "fallbackImplementation": "FallbackScraperService",
  "timestamp": 1672531200000
}
```

## Implementation Details

The application uses a primary job scraper service that interfaces with the Jooble API. If this fails or returns no results, it falls back to other job sources:

1. **JoobleApiJobScraperService** (Primary) - Uses the Jooble API, which requires registration
2. **FallbackScraperService** - Provides mock data when external APIs are unavailable
3. **ArbeitnowApiJobScraperService** - Uses the Arbeitnow API (no authentication required)
4. **ApiBasedJobScraperService** - Uses multiple public APIs (JSearch, SerpApi, Adzuna)
5. **LinkedInScraperServiceImpl_OLD** - Legacy implementation using Selenium (kept for compatibility)

## Getting API Keys

- **Jooble API**: Register at https://jooble.org/api/about
- **JSearch API**: Get a free key at RapidAPI
- **SerpAPI**: Get a key at https://serpapi.com
- **Adzuna API**: Register at https://developer.adzuna.com

## License

This project is licensed under the MIT License - see the LICENSE file for details.
