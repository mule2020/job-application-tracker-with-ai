package com.muluken.jobtracker.coverletter.service;

import com.muluken.jobtracker.application.model.JobApplication;
import com.muluken.jobtracker.application.repository.JobApplicationRepository;
import com.muluken.jobtracker.common.exception.ApiException;
import com.muluken.jobtracker.coverletter.dtos.*;
import com.muluken.jobtracker.coverletter.model.CoverLetter;
import com.muluken.jobtracker.coverletter.repository.CoverLetterRepository;
import com.muluken.jobtracker.resume.model.GeneratedResume;
import com.muluken.jobtracker.resume.repository.GeneratedResumeRepository;
import com.muluken.jobtracker.resume.service.GroqService;
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
public class CoverLetterService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final GroqService groqService;

    public GeneratedCoverLetterResponse generate(String email, GenerateCoverLetterRequest request) {

        User user = getUser(email);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Profile not found", HttpStatus.NOT_FOUND));

        JobApplication application = getOwnedApplication(user, request.getApplicationId());

        // fix: join skills list to string for prompt
        String skills = profile.getSkills() != null
                ? String.join(", ", profile.getSkills())
                : "";

        // resume is optional — fall back gracefully if not generated yet
        GeneratedResume resume = generatedResumeRepository
                .findByApplicationId(application.getId())
                .orElse(null);

        String generatedResumeText = resume != null
                ? resume.getGeneratedContent()
                : "No generated resume available — use base resume only.";

        String prompt = """
                Write a professional, concise, ATS-friendly cover letter.

                Full Name:
                %s

                Professional Title:
                %s

                Skills:
                %s

                Base Resume:
                %s

                Generated Resume:
                %s

                Target Company:
                %s

                Target Job Title:
                %s

                Job Description:
                %s

                Write in a confident, human tone. Avoid buzzwords. Keep it under 300 words.
                """.formatted(
                profile.getFullName(),
                profile.getTitle(),
                skills,
                profile.getBaseResumeText(),
                generatedResumeText,
                application.getCompany(),
                application.getJobTitle(),
                application.getJobDescription()
        );

        return new GeneratedCoverLetterResponse(groqService.generateText(prompt));
    }

    public CoverLetterResponse save(String email, SaveCoverLetterRequest request) {

        User user = getUser(email);
        JobApplication application = getOwnedApplication(user, request.getApplicationId());

        CoverLetter letter = coverLetterRepository
                .findByApplicationId(application.getId())
                .orElse(new CoverLetter());

        letter.setUser(user);
        letter.setApplication(application);
        letter.setContent(request.getContent());
        letter.setName(application.getCompany() + " - " + application.getJobTitle() + " Cover Letter");

        return map(coverLetterRepository.save(letter));
    }

    public List<CoverLetterResponse> getAll(String email) {

        User user = getUser(email);

        return coverLetterRepository.findByUserId(user.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public CoverLetterResponse getOne(String email, UUID id) {

        User user = getUser(email);
        return map(getOwnedCoverLetter(user, id));
    }

    public CoverLetterResponse getByApplicationId(String email, UUID applicationId) {

        User user = getUser(email);

        CoverLetter letter = coverLetterRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApiException("Cover letter not found", HttpStatus.NOT_FOUND));

        if (!letter.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return map(letter);
    }

    public CoverLetterResponse update(String email, UUID id, UpdateCoverLetterRequest request) {

        User user = getUser(email);
        CoverLetter letter = getOwnedCoverLetter(user, id);
        letter.setContent(request.getContent());

        return map(coverLetterRepository.save(letter));
    }

    public void delete(String email, UUID id) {

        User user = getUser(email);
        coverLetterRepository.delete(getOwnedCoverLetter(user, id));
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

    private CoverLetter getOwnedCoverLetter(User user, UUID id) {

        CoverLetter letter = coverLetterRepository.findById(id)
                .orElseThrow(() -> new ApiException("Cover letter not found", HttpStatus.NOT_FOUND));

        if (!letter.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return letter;
    }

    private CoverLetterResponse map(CoverLetter letter) {
        return new CoverLetterResponse(
                letter.getId(),
                letter.getApplication().getId(),
                letter.getApplication().getCompany(),
                letter.getApplication().getJobTitle(),
                letter.getContent(),
                letter.getName(),
                letter.getCreatedAt(),
                letter.getUpdatedAt()
        );
    }
}