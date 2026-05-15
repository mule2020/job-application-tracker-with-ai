package com.muluken.jobtracker.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalApplications;

    private long pendingCount;
    private long appliedCount;
    private long interviewCount;
    private long offerCount;
    private long rejectedCount;

    private long totalResumes;
    private long totalCoverLetters;
}
