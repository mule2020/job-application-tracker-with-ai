package com.muluken.jobtracker.application.dto;

import com.muluken.jobtracker.application.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateJobApplicationRequest {

    @NotBlank
    private String company;

    @NotBlank
    private String jobTitle;

    private String jobDescription;

    private ApplicationStatus status;

    private String jobUrl;

    private String salaryRange;

    private String location;

    private String notes;

    private LocalDateTime appliedAt;
}