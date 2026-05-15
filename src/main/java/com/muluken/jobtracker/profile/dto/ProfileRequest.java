package com.muluken.jobtracker.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileRequest {

    @Size(max = 255)
    private String fullName;

    @Size(max = 255)
    private String title;

    private String summary;

    private List<String> skills;

    private String baseResumeText;

    private String phone;

    private String location;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;
}
