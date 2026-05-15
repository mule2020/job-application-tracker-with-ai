package com.muluken.jobtracker.dashboard.controller;

import com.muluken.jobtracker.dashboard.dto.DashboardStatsResponse;
import com.muluken.jobtracker.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsResponse getStats(Authentication auth) {
        return dashboardService.getStats(auth.getName());
    }
}
