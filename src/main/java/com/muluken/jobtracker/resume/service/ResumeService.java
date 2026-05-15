package com.muluken.jobtracker.resume.service;

import com.muluken.jobtracker.application.model.JobApplication;
import com.muluken.jobtracker.application.repository.JobApplicationRepository;
import com.muluken.jobtracker.common.exception.ApiException;
import com.muluken.jobtracker.resume.dto.*;
import com.muluken.jobtracker.resume.model.GeneratedResume;
import com.muluken.jobtracker.resume.repository.GeneratedResumeRepository;
import com.muluken.jobtracker.user.model.User;
import com.muluken.jobtracker.user.model.UserProfile;
import com.muluken.jobtracker.user.repository.UserProfileRepository;
import com.muluken.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final GroqService groqService;

    public GenerateResumeResponse generateResume(String email, GenerateResumeRequest request) {

        User user = getUser(email);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("User profile not found", HttpStatus.NOT_FOUND));

        JobApplication application = getOwnedApplication(user, request.getApplicationId());

        // join skills list to comma-separated string for the prompt
        String skills = profile.getSkills() != null
                ? String.join(", ", profile.getSkills())
                : "";

        String prompt = """
                Generate a professional ATS-optimized resume.

                Full Name:
                %s

                Professional Title:
                %s

                Skills:
                %s

                Base Resume:
                %s

                Target Company:
                %s

                Target Job Title:
                %s

                Job Description:
                %s
                """.formatted(
                profile.getFullName(),
                profile.getTitle(),
                skills,
                profile.getBaseResumeText(),
                application.getCompany(),
                application.getJobTitle(),
                application.getJobDescription()
        );

        return new GenerateResumeResponse(groqService.generateText(prompt));
    }

    public ResumeResponse saveResume(String email, SaveResumeRequest request) {

        User user = getUser(email);
        JobApplication application = getOwnedApplication(user, request.getApplicationId());

        GeneratedResume resume = generatedResumeRepository
                .findByApplicationId(application.getId())
                .orElse(new GeneratedResume());

        resume.setUser(user);
        resume.setApplication(application);
        resume.setGeneratedContent(request.getGeneratedContent());
        resume.setName(application.getCompany() + " - " + application.getJobTitle() + " Resume");

        return map(generatedResumeRepository.save(resume));
    }

    public List<ResumeResponse> getAllResumes(String email) {

        User user = getUser(email);

        return generatedResumeRepository.findByUserId(user.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public ResumeResponse getOneResume(String email, UUID resumeId) {

        User user = getUser(email);
        return map(getOwnedResume(user, resumeId));
    }

    public ResumeResponse getResumeByApplicationId(String email, UUID applicationId) {

        User user = getUser(email);

        GeneratedResume resume = generatedResumeRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApiException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return map(resume);
    }

    public ResumeResponse updateResume(String email, UUID resumeId, UpdateResumeRequest request) {

        User user = getUser(email);
        GeneratedResume resume = getOwnedResume(user, resumeId);
        resume.setGeneratedContent(request.getGeneratedContent());

        return map(generatedResumeRepository.save(resume));
    }

    public void deleteResume(String email, UUID resumeId) {

        User user = getUser(email);
        generatedResumeRepository.delete(getOwnedResume(user, resumeId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    private JobApplication getOwnedApplication(User user, UUID applicationId) {

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found", HttpStatus.NOT_FOUND));

        if (!application.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return application;
    }

    private GeneratedResume getOwnedResume(User user, UUID resumeId) {

        GeneratedResume resume = generatedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApiException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return resume;
    }

    private ResumeResponse map(GeneratedResume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getApplication().getId(),
                resume.getApplication().getCompany(),
                resume.getApplication().getJobTitle(),
                resume.getGeneratedContent(),
                resume.getName(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}