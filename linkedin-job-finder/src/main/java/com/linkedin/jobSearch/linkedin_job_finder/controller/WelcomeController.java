package com.linkedin.jobSearch.linkedin_job_finder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WelcomeController {

    @GetMapping("/")
    @ResponseBody
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the Job Search API");
        response.put("endpoints", Map.of(
                "Search Jobs", "/api/jobs/search?keywords=software",
                "Health Check", "/api/health"
        ));
        return response;
    }
}