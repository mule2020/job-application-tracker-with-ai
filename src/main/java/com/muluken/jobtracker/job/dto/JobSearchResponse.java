package com.muluken.jobtracker.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchResponse {
    private String id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String redirectUrl;
    private String salary;
    private String postedDate;
}