package com.muluken.jobtracker.profile.controller;

import com.muluken.jobtracker.profile.dto.ProfileRequest;
import com.muluken.jobtracker.profile.dto.ProfileResponse;
import com.muluken.jobtracker.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {
        return profileService.getProfile(authentication.getName());
    }

    @PutMapping
    public ProfileResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileRequest request
    ) {
        return profileService.updateProfile(authentication.getName(), request);
    }
}
