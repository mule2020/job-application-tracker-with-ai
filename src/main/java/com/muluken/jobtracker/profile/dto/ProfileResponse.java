package com.muluken.jobtracker.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProfileResponse {

    private String fullName;

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
