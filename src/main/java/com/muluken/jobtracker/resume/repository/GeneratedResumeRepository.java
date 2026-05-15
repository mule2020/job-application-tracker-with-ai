package com.muluken.jobtracker.resume.repository;

import com.muluken.jobtracker.resume.model.GeneratedResume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, UUID> {

    List<GeneratedResume> findByUserId(UUID userId);

    Optional<GeneratedResume> findByApplicationId(UUID applicationId);

    long countByApplicationUserId(UUID userId);
}