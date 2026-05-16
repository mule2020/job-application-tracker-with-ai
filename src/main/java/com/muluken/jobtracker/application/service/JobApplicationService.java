package com.muluken.jobtracker.application.service;

import com.muluken.jobtracker.activity.model.ActivityType;
import com.muluken.jobtracker.activity.service.ActivityService;
import com.muluken.jobtracker.application.dto.ApplicationStatsResponse;
import com.muluken.jobtracker.application.dto.CreateJobApplicationRequest;
import com.muluken.jobtracker.application.dto.JobApplicationResponse;
import com.muluken.jobtracker.application.dto.UpdateJobApplicationRequest;
import com.muluken.jobtracker.application.model.ApplicationStatus;
import com.muluken.jobtracker.application.model.JobApplication;
import com.muluken.jobtracker.application.repository.JobApplicationRepository;
import com.muluken.jobtracker.common.exception.ApiException;
import com.muluken.jobtracker.coverletter.model.CoverLetter;
import com.muluken.jobtracker.coverletter.repository.CoverLetterRepository;
import com.muluken.jobtracker.resume.model.GeneratedResume;
import com.muluken.jobtracker.resume.repository.GeneratedResumeRepository;
import com.muluken.jobtracker.user.model.User;
import com.muluken.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ActivityService activityService; // ← NEW

    public JobApplicationResponse createApplication(
            String email, CreateJobApplicationRequest request) {

        User user = getUserByEmail(email);

        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setCompany(request.getCompany());
        application.setJobTitle(request.getJobTitle());
        application.setJobDescription(request.getJobDescription());
        application.setStatus(request.getStatus());
        application.setJobUrl(request.getJobUrl());
        application.setSalaryRange(request.getSalaryRange());
        application.setLocation(request.getLocation());
        application.setNotes(request.getNotes());
        application.setAppliedAt(request.getAppliedAt());

        JobApplication saved = jobApplicationRepository.save(application);

        // ── Log activity ──────────────────────────────────
        activityService.log(
                user,
                ActivityType.APPLICATION_CREATED,
                "Created application",
                saved.getCompany(),
                saved.getJobTitle()
        );

        return map(saved);
    }

    public Page<JobApplicationResponse> getApplications(
            String email, int page, int size, ApplicationStatus status) {

        User user = getUserByEmail(email);
        PageRequest pageable = PageRequest.of(page, size);

        Page<JobApplication> applications =
                (status != null)
                        ? jobApplicationRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                        : jobApplicationRepository.findByUserId(user.getId(), pageable);

        return applications.map(this::map);
    }

    public JobApplicationResponse getApplicationById(
            String email, UUID applicationId) {

        User user = getUserByEmail(email);
        return map(getOwnedApplication(user, applicationId));
    }

    public JobApplicationResponse updateApplication(
            String email, UUID applicationId, UpdateJobApplicationRequest request) {

        User user = getUserByEmail(email);
        JobApplication application = getOwnedApplication(user, applicationId);

        // ── Track status before update ────────────────────
        ApplicationStatus oldStatus = application.getStatus();

        if (request.getCompany()        != null) application.setCompany(request.getCompany());
        if (request.getJobTitle()       != null) application.setJobTitle(request.getJobTitle());
        if (request.getJobDescription() != null) application.setJobDescription(request.getJobDescription());
        if (request.getStatus()         != null) application.setStatus(request.getStatus());
        if (request.getJobUrl()         != null) application.setJobUrl(request.getJobUrl());
        if (request.getSalaryRange()    != null) application.setSalaryRange(request.getSalaryRange());
        if (request.getLocation()       != null) application.setLocation(request.getLocation());
        if (request.getNotes()          != null) application.setNotes(request.getNotes());
        if (request.getAppliedAt()      != null) application.setAppliedAt(request.getAppliedAt());

        JobApplication saved = jobApplicationRepository.save(application);

        // ── Log status change only if status changed ──────
        if (request.getStatus() != null && !oldStatus.equals(request.getStatus())) {
            activityService.log(
                    user,
                    ActivityType.STATUS_UPDATED,
                    "Status updated to " + request.getStatus(),
                    saved.getCompany(),
                    saved.getJobTitle()
            );
        }

        return map(saved);
    }

    public void deleteApplication(String email, UUID applicationId) {

        User user = getUserByEmail(email);
        JobApplication application = getOwnedApplication(user, applicationId);
        jobApplicationRepository.delete(application);
    }

    public ApplicationStatsResponse getStats(String email) {

        User user = getUserByEmail(email);
        UUID userId = user.getId();

        return new ApplicationStatsResponse(
                jobApplicationRepository.countByUserId(userId),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.PENDING),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEWING),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFERED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.ACCEPTED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED),
                jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.WITHDRAWN)
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    private JobApplication getOwnedApplication(User user, UUID applicationId) {

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new ApiException("Application not found", HttpStatus.NOT_FOUND));

        if (!application.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return application;
    }

    private JobApplicationResponse map(JobApplication app) {

        GeneratedResume resume = generatedResumeRepository
                .findByApplicationId(app.getId()).orElse(null);
        CoverLetter cover = coverLetterRepository
                .findByApplicationId(app.getId()).orElse(null);

        return new JobApplicationResponse(
                app.getId(),
                app.getCompany(),
                app.getJobTitle(),
                app.getJobDescription(),
                app.getStatus(),
                app.getJobUrl(),
                app.getSalaryRange(),
                app.getLocation(),
                app.getNotes(),
                app.getAppliedAt(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                resume != null,
                resume != null ? trim(resume.getGeneratedContent()) : null,
                cover != null,
                cover != null ? trim(cover.getContent()) : null
        );
    }

    private String trim(String text) {
        if (text == null) return null;
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }
}