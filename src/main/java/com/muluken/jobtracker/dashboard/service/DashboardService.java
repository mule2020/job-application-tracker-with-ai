package com.muluken.jobtracker.dashboard.service;

import com.muluken.jobtracker.application.model.ApplicationStatus;
import com.muluken.jobtracker.application.repository.JobApplicationRepository;
import com.muluken.jobtracker.dashboard.dto.DashboardStatsResponse;
import com.muluken.jobtracker.resume.repository.GeneratedResumeRepository;
import com.muluken.jobtracker.coverletter.repository.CoverLetterRepository;
import com.muluken.jobtracker.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JobApplicationRepository jobApplicationRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final CoverLetterRepository coverLetterRepository;

    public DashboardStatsResponse getStats(User user) {
        UUID userId = user.getId();
        return new DashboardStatsResponse(
                jobApplicationRepository.countByUserId(userId),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.PENDING),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEWING),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFERED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.ACCEPTED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.WITHDRAWN),
                generatedResumeRepository.countByApplicationUserId(userId),
                coverLetterRepository.countByUserId(userId)
        );
    }
}