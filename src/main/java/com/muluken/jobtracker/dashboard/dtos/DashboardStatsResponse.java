package com.muluken.jobtracker.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalApplications;
    private long pending;
    private long applied;
    private long interviewing;
    private long offered;
    private long accepted;
    private long rejected;
    private long withdrawn;
    private long totalResumes;
    private long totalCoverLetters;
}