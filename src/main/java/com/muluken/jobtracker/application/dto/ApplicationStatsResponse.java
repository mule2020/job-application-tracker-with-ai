package com.muluken.jobtracker.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationStatsResponse {

    private long total;
    private long pending;
    private long applied;
    private long interviewing;
    private long offered;
    private long accepted;
    private long rejected;
    private long withdrawn;
}