package com.muluken.jobtracker.application.dto;

import com.muluken.jobtracker.application.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class JobApplicationResponse {

    private UUID id;
    private String company;
    private String jobTitle;
    private String jobDescription;
    private ApplicationStatus status;
    private String jobUrl;
    private String salaryRange;
    private String location;
    private String notes;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean resumeExists;
    private String resumePreview;

    private boolean coverLetterExists;
    private String coverLetterPreview;
}