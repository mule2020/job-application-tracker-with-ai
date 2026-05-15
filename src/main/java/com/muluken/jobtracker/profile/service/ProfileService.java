package com.muluken.jobtracker.profile.service;

import com.muluken.jobtracker.profile.dto.ProfileRequest;
import com.muluken.jobtracker.profile.dto.ProfileResponse;
import com.muluken.jobtracker.user.model.User;
import com.muluken.jobtracker.user.model.UserProfile;
import com.muluken.jobtracker.user.repository.UserProfileRepository;
import com.muluken.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElse(new UserProfile());

        return mapToResponse(profile);
    }

    public ProfileResponse updateProfile(String email, ProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElse(new UserProfile());

        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setTitle(request.getTitle());
        profile.setSummary(request.getSummary());
        profile.setSkills(request.getSkills());
        profile.setBaseResumeText(request.getBaseResumeText());
        profile.setPhone(request.getPhone());
        profile.setLocation(request.getLocation());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setPortfolioUrl(request.getPortfolioUrl());

        UserProfile saved = userProfileRepository.save(profile);

        return mapToResponse(saved);
    }

    private ProfileResponse mapToResponse(UserProfile profile) {
        return new ProfileResponse(
                profile.getFullName(),
                profile.getTitle(),
                profile.getSummary(),
                profile.getSkills(),
                profile.getBaseResumeText(),
                profile.getPhone(),
                profile.getLocation(),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getPortfolioUrl()
        );
    }
}
