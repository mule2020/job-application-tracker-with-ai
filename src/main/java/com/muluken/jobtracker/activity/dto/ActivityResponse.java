package com.muluken.jobtracker.activity.dto;

import com.muluken.jobtracker.activity.model.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ActivityResponse {

    private UUID id;
    private ActivityType type;
    private String description;
    private String company;
    private String jobTitle;
    private LocalDateTime createdAt;
}