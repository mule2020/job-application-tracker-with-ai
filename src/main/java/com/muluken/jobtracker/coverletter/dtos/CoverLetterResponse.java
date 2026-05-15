package com.muluken.jobtracker.coverletter.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CoverLetterResponse {

    private UUID id;
    private UUID applicationId;
    private String company;
    private String jobTitle;
    private String content;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}