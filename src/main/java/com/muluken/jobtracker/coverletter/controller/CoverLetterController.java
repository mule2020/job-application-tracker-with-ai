package com.muluken.jobtracker.coverletter.controller;

import com.muluken.jobtracker.coverletter.dtos.*;
import com.muluken.jobtracker.coverletter.service.CoverLetterService;
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
@RequestMapping("/api/cover-letters")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a cover letter using AI for a job application")
    public GeneratedCoverLetterResponse generate(
            @Valid @RequestBody GenerateCoverLetterRequest request,
            Authentication auth
    ) {
        return coverLetterService.generate(auth.getName(), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save a cover letter")
    public CoverLetterResponse save(
            @Valid @RequestBody SaveCoverLetterRequest request,
            Authentication auth
    ) {
        return coverLetterService.save(auth.getName(), request);
    }

    @GetMapping
    @Operation(summary = "Get all cover letters for current user")
    public List<CoverLetterResponse> getAll(Authentication auth) {
        return coverLetterService.getAll(auth.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single cover letter by id")
    public CoverLetterResponse getOne(
            @PathVariable UUID id,
            Authentication auth
    ) {
        return coverLetterService.getOne(auth.getName(), id);
    }

    @GetMapping("/by-application/{applicationId}")
    @Operation(summary = "Get cover letter by application id")
    public CoverLetterResponse getByApplicationId(
            @PathVariable UUID applicationId,
            Authentication auth
    ) {
        return coverLetterService.getByApplicationId(auth.getName(), applicationId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a cover letter")
    public CoverLetterResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCoverLetterRequest request,
            Authentication auth
    ) {
        return coverLetterService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a cover letter")
    public void delete(
            @PathVariable UUID id,
            Authentication auth
    ) {
        coverLetterService.delete(auth.getName(), id);
    }
}