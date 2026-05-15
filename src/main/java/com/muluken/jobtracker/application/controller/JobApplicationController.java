package com.muluken.jobtracker.application.controller;

import com.muluken.jobtracker.application.dto.ApplicationStatsResponse;
import com.muluken.jobtracker.application.dto.CreateJobApplicationRequest;
import com.muluken.jobtracker.application.dto.JobApplicationResponse;
import com.muluken.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.muluken.jobtracker.application.model.ApplicationStatus;
import com.muluken.jobtracker.application.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new job application")
    public JobApplicationResponse createApplication(
            @Valid @RequestBody CreateJobApplicationRequest request,
            Authentication authentication
    ) {
        return jobApplicationService.createApplication(authentication.getName(), request);
    }

    @GetMapping
    @Operation(summary = "Get paginated job applications for current user")
    public Page<JobApplicationResponse> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ApplicationStatus status,
            Authentication authentication
    ) {
        return jobApplicationService.getApplications(authentication.getName(), page, size, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single job application by id")
    public JobApplicationResponse getApplicationById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return jobApplicationService.getApplicationById(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job application")
    public JobApplicationResponse updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobApplicationRequest request,
            Authentication authentication
    ) {
        return jobApplicationService.updateApplication(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a job application")
    public void deleteApplication(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        jobApplicationService.deleteApplication(authentication.getName(), id);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get application statistics for current user")
    public ApplicationStatsResponse getStats(Authentication authentication) {
        return jobApplicationService.getStats(authentication.getName());
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get all valid application statuses")
    public List<String> getStatuses() {
        return Arrays.stream(ApplicationStatus.values())
                .map(Enum::name)
                .toList();
    }
}