package com.muluken.jobtracker.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResumeResponse {

    private UUID id;
    private UUID applicationId;
    private String company;
    private String jobTitle;
    private String generatedContent;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}