package com.muluken.jobtracker.job.dto;

import lombok.Data;

@Data
public class JobSearchRequest {
    private String keyword;
    private String location;
    private int page = 1;
}