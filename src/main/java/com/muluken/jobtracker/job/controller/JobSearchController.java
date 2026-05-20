package com.muluken.jobtracker.job.controller;

import com.muluken.jobtracker.job.dto.JobSearchResponse;
import com.muluken.jobtracker.job.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobSearchController {

    private final JobSearchService jobSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<JobSearchResponse>> searchJobs(
            @RequestParam(defaultValue = "Software Developer") String keyword,
            @RequestParam(defaultValue = "Toronto") String location,
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<JobSearchResponse> jobs = jobSearchService.searchJobs(keyword, location, page);
        return ResponseEntity.ok(jobs);
    }
}