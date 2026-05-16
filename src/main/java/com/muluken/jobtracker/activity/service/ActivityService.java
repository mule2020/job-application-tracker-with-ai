package com.muluken.jobtracker.activity.service;

import com.muluken.jobtracker.activity.dto.ActivityResponse;
import com.muluken.jobtracker.activity.model.ActivityLog;
import com.muluken.jobtracker.activity.model.ActivityType;
import com.muluken.jobtracker.activity.repository.ActivityLogRepository;
import com.muluken.jobtracker.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    @Async
    public void log(
            User user,
            ActivityType type,
            String description,
            String company,
            String jobTitle
    ) {
        try {
            ActivityLog log = new ActivityLog();
            log.setUser(user);
            log.setType(type);
            log.setDescription(description);
            log.setCompany(company);
            log.setJobTitle(jobTitle);
            activityLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage());
        }
    }

    public List<ActivityResponse> getRecentActivity(UUID userId, int limit) {
        return activityLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(a -> new ActivityResponse(
                        a.getId(),
                        a.getType(),
                        a.getDescription(),
                        a.getCompany(),
                        a.getJobTitle(),
                        a.getCreatedAt()
                ))
                .toList();
    }
}