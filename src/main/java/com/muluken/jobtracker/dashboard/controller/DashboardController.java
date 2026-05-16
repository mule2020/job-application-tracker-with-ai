package com.muluken.jobtracker.dashboard.controller;

import com.muluken.jobtracker.activity.dto.ActivityResponse;
import com.muluken.jobtracker.activity.service.ActivityService;
import com.muluken.jobtracker.common.exception.ApiException;
import com.muluken.jobtracker.dashboard.dto.DashboardStatsResponse;
import com.muluken.jobtracker.dashboard.service.DashboardService;
import com.muluken.jobtracker.user.model.User;
import com.muluken.jobtracker.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Dashboard stats and activity feed")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ActivityService  activityService;
    private final UserRepository   userRepository;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics for current user")
    public DashboardStatsResponse getStats(Authentication authentication) {
        User user = getUser(authentication);
        return dashboardService.getStats(user);
    }

    @GetMapping("/activity")
    @Operation(summary = "Get recent activity for current user")
    public List<ActivityResponse> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        User user = getUser(authentication);
        return activityService.getRecentActivity(user.getId(), Math.min(limit, 20));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}