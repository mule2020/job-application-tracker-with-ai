package com.muluken.jobtracker.resume.service;

import com.muluken.jobtracker.activity.model.ActivityType;
import com.muluken.jobtracker.activity.service.ActivityService;
import com.muluken.jobtracker.application.model.JobApplication;
import com.muluken.jobtracker.application.repository.JobApplicationRepository;
import com.muluken.jobtracker.common.exception.ApiException;
import com.muluken.jobtracker.common.service.RateLimitService;
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
    private final ActivityService activityService;
    private final RateLimitService rateLimitService;
    private final GroqService groqService;

    public GenerateResumeResponse generateResume(String email, GenerateResumeRequest request) {

        if (!rateLimitService.tryConsume(email)) {
            long remaining = rateLimitService.getRemainingTokens(email);
            throw new ApiException(
                    "Generation limit reached. You have " + remaining +
                            " generations remaining. Limit resets every hour.",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        User user = getUser(email);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("User profile not found", HttpStatus.NOT_FOUND));

        JobApplication application = getOwnedApplication(user, request.getApplicationId());

        String skills = profile.getSkills() != null
                ? String.join(", ", profile.getSkills())
                : "";

        String prompt = """
        OUTPUT ONLY THE RESUME. NO COMMENTARY. NO NOTES. NO FOOTER TEXT. NO PLACEHOLDERS.
        ANY TEXT THAT IS NOT PART OF THE RESUME IS FORBIDDEN.

        FORMAT RULES:
        - Line 1: Full name only
        - Line 2: Professional title only
        - Sections in this order: CONTACT, SUMMARY, EXPERIENCE, SKILLS, EDUCATION, PROJECTS, CERTIFICATIONS
        - Each section header on its own line in UPPERCASE
        - Bullet points start with •
        - NO markdown, NO asterisks, NO bold formatting
        - Use EXACT values from CONTACT DATA below — never write placeholders

        CONTACT DATA - COPY THESE EXACTLY:
        Full Name: %s
        Title: %s
        Email: %s
        Phone: %s
        Location: %s
        LinkedIn: %s
        GitHub: %s
        Portfolio: %s

        CANDIDATE PROFILE:
        Skills: %s
        Base Resume: %s

        TARGET JOB:
        Company: %s
        Role: %s
        Job Description:
        %s

        TAILORING INSTRUCTIONS:
        - Read the job description carefully and identify the key required skills, tools, and responsibilities
        - Rewrite the SUMMARY to directly address what this specific job is looking for
        - In EXPERIENCE, emphasize bullet points that are most relevant to this job — reword them to mirror the job description language
        - In SKILLS, list only skills that are relevant to this job — put the most relevant ones first
        - If the job requires specific technologies, make sure they appear prominently if the candidate has them
        - The resume must feel like it was written specifically for this job, not a generic resume

        START THE RESUME NOW WITH THE FULL NAME ON LINE 1:
        """.formatted(
                profile.getFullName(),
                nvl(profile.getTitle()),
                user.getEmail(),
                nvl(profile.getPhone()),
                nvl(profile.getLocation()),
                nvl(profile.getLinkedinUrl()),
                nvl(profile.getGithubUrl()),
                nvl(profile.getPortfolioUrl()),
                skills,
                nvl(profile.getBaseResumeText()),
                application.getCompany(),
                application.getJobTitle(),
                nvl(application.getJobDescription())
        );

        String raw = groqService.generateText(prompt);
        String cleaned = cleanResumeOutput(raw);

        activityService.log(
                user,
                ActivityType.RESUME_GENERATED,
                "Resume Generated",
                application.getCompany(),
                application.getJobTitle()
        );

        return new GenerateResumeResponse(cleaned);
    }

    private String cleanResumeOutput(String text) {
        if (text == null) return "";

        // remove markdown bold/italic
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        text = text.replaceAll("\\*(.+?)\\*", "$1");
        text = text.replaceAll("__(.+?)__", "$1");
        text = text.replaceAll("_(.+?)_", "$1");

        // remove markdown links [text](url) -> text
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");

        String[] lines = text.split("\n");
        StringBuilder cleaned = new StringBuilder();
        boolean started = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (!started && trimmed.isEmpty()) continue;
            if (isAiChatter(trimmed)) continue;

            started = true;
            cleaned.append(line).append("\n");
        }

        // trim trailing chatter from end
        String result = cleaned.toString().trim();
        String[] resultLines = result.split("\n");
        int lastGoodLine = resultLines.length - 1;
        while (lastGoodLine >= 0 && isAiChatter(resultLines[lastGoodLine].trim())) {
            lastGoodLine--;
        }

        StringBuilder final_ = new StringBuilder();
        for (int i = 0; i <= lastGoodLine; i++) {
            final_.append(resultLines[i]).append("\n");
        }
        return final_.toString().trim();
    }

    private boolean isAiChatter(String line) {
        if (line.isEmpty()) return false;
        String lower = line.toLowerCase();
        return lower.startsWith("here is") ||
                lower.startsWith("here's") ||
                lower.startsWith("i've") ||
                lower.startsWith("i have") ||
                lower.startsWith("below is") ||
                lower.startsWith("this resume") ||
                lower.startsWith("i hope") ||
                lower.startsWith("good luck") ||
                lower.startsWith("feel free") ||
                lower.startsWith("best of luck") ||
                lower.startsWith("please note") ||
                lower.startsWith("note:") ||
                lower.startsWith("let me know") ||
                lower.startsWith("remember to") ||
                lower.startsWith("don't hesitate") ||
                lower.startsWith("i've tailored") ||
                lower.startsWith("i have tailored") ||
                lower.contains("let me know if you need") ||
                lower.contains("feel free to reach out") ||
                lower.contains("customize your resume") ||
                lower.contains("any further assistance");
    }

    private String nvl(String value) {
        return value != null ? value : "";
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

        activityService.log(
                user,
                ActivityType.RESUME_SAVED,
                "Generated Resume Saved",
                application.getCompany(),
                application.getJobTitle()
        );

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
        GeneratedResume resume = getOwnedResume(user, resumeId);
        JobApplication application = resume.getApplication();
        if (application != null) {
            application.setGeneratedResume(null);
            jobApplicationRepository.save(application);
        }
        generatedResumeRepository.delete(resume);
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