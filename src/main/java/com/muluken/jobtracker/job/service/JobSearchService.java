package com.muluken.jobtracker.job.service;

import com.muluken.jobtracker.job.client.AdzunaClient;
import com.muluken.jobtracker.job.dto.JobSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSearchService {

    private final AdzunaClient adzunaClient;

    public List<JobSearchResponse> searchJobs(String keyword,
                                              String location,
                                              int page) {

        try {

            Map<String, Object> response =
                    adzunaClient.searchJobs(keyword, location, page);

            if (response == null) {
                log.error("Adzuna returned null response");
                return Collections.emptyList();
            }

            log.info("Response keys: {}", response.keySet());

            if (!response.containsKey("results")) {
                log.error("No 'results' key found in response");
                log.error("Response body: {}", response);
                return Collections.emptyList();
            }

            Object rawResults = response.get("results");

            if (rawResults == null) {
                log.error("'results' is null");
                return Collections.emptyList();
            }

            log.info("Raw results type: {}", rawResults.getClass().getName());

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) rawResults;

            log.info("Results size: {}", results.size());

            List<JobSearchResponse> jobs = new ArrayList<>();

            for (Map<String, Object> result : results) {

                log.info("Mapping job: {}", result);

                jobs.add(mapToResponse(result));
            }

            log.info("Mapped {} jobs successfully", jobs.size());

            return jobs;

        } catch (Exception e) {

            log.error("Error fetching jobs", e);

            return Collections.emptyList();
        }
    }

    private JobSearchResponse mapToResponse(Map<String, Object> result) {

        Map<String, Object> company =
                (Map<String, Object>) result.get("company");

        Map<String, Object> location =
                (Map<String, Object>) result.get("location");

        String companyName = company != null
                ? String.valueOf(company.getOrDefault("display_name", "Unknown"))
                : "Unknown";

        String locationName = location != null
                ? String.valueOf(location.getOrDefault("display_name", ""))
                : "";

        String salary = null;

        Object salaryMin = result.get("salary_min");
        Object salaryMax = result.get("salary_max");

        if (salaryMin != null && salaryMax != null) {

            salary = "$" +
                    Math.round(Double.parseDouble(salaryMin.toString()))
                    + " - $" +
                    Math.round(Double.parseDouble(salaryMax.toString()));
        }

        return JobSearchResponse.builder()
                .id(String.valueOf(result.getOrDefault("id", "")))
                .title(String.valueOf(result.getOrDefault("title", "")))
                .company(companyName)
                .location(locationName)
                .description(String.valueOf(result.getOrDefault("description", "")))
                .redirectUrl(String.valueOf(result.getOrDefault("redirect_url", "")))
                .salary(salary)
                .postedDate(String.valueOf(result.getOrDefault("created", "")))
                .build();
    }
}