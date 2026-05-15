package com.muluken.jobtracker.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SaveResumeRequest {

    @NotNull
    private UUID applicationId;

    @NotBlank
    private String generatedContent;
}