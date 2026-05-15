package com.muluken.jobtracker.dashboard.service;

import com.muluken.jobtracker.application.model.ApplicationStatus;
import com.muluken.jobtracker.application.repository.JobApplicationRepository;
import com.muluken.jobtracker.dashboard.dto.DashboardStatsResponse;
import com.muluken.jobtracker.resume.repository.GeneratedResumeRepository;
import com.muluken.jobtracker.coverletter.repository.CoverLetterRepository;
import com.muluken.jobtracker.user.model.User;
import com.muluken.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final CoverLetterRepository coverLetterRepository;

    public DashboardStatsResponse getStats(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userId = user.getId();

        long totalApplications = jobApplicationRepository.countByUserId(userId);

        long pendingCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.PENDING);
        long appliedCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED);
        long interviewCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEWING);
        long offerCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFERED);
        long rejectedCount = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED);

        long totalResumes = generatedResumeRepository.countByApplicationUserId(userId);
        long totalCoverLetters = coverLetterRepository.countByUserId(userId);

        return new DashboardStatsResponse(
                totalApplications,
                pendingCount,
                appliedCount,
                interviewCount,
                offerCount,
                rejectedCount,
                totalResumes,
                totalCoverLetters
        );
    }
}
