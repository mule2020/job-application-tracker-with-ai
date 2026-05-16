package com.muluken.jobtracker.activity.repository;

import com.muluken.jobtracker.activity.model.ActivityLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    List<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, PageRequest pageable);
}