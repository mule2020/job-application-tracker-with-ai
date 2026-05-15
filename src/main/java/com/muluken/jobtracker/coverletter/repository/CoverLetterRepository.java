package com.muluken.jobtracker.coverletter.repository;

import com.muluken.jobtracker.coverletter.model.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, UUID> {

    Optional<CoverLetter> findByApplicationId(UUID applicationId);

    List<CoverLetter> findByUserId(UUID userId);

    long countByUserId(UUID userId);
}