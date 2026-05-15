package com.muluken.jobtracker.resume.controller;

import com.muluken.jobtracker.resume.dto.*;
import com.muluken.jobtracker.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a resume using AI for a job application")
    public GenerateResumeResponse generateResume(
            @Valid @RequestBody GenerateResumeRequest request,
            Authentication authentication
    ) {
        return resumeService.generateResume(authentication.getName(), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save a generated resume")
    public ResumeResponse saveResume(
            @Valid @RequestBody SaveResumeRequest request,
            Authentication authentication
    ) {
        return resumeService.saveResume(authentication.getName(), request);
    }

    @GetMapping
    @Operation(summary = "Get all resumes for current user")
    public List<ResumeResponse> getAllResumes(Authentication authentication) {
        return resumeService.getAllResumes(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single resume by id")
    public ResumeResponse getOneResume(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return resumeService.getOneResume(authentication.getName(), id);
    }

    @GetMapping("/by-application/{applicationId}")
    @Operation(summary = "Get resume by application id")
    public ResumeResponse getResumeByApplicationId(
            @PathVariable UUID applicationId,
            Authentication authentication
    ) {
        return resumeService.getResumeByApplicationId(authentication.getName(), applicationId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a resume")
    public ResumeResponse updateResume(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResumeRequest request,
            Authentication authentication
    ) {
        return resumeService.updateResume(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a resume")
    public void deleteResume(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        resumeService.deleteResume(authentication.getName(), id);
    }
}