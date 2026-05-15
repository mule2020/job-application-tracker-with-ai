package com.muluken.jobtracker.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResumeRequest {

    @NotBlank
    private String generatedContent;
}