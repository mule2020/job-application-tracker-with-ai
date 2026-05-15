package com.muluken.jobtracker.auth.controller;

import com.muluken.jobtracker.auth.dto.AuthResponse;
import com.muluken.jobtracker.auth.dto.LoginRequest;
import com.muluken.jobtracker.auth.dto.RefreshTokenRequest;
import com.muluken.jobtracker.auth.dto.RegisterRequest;
import com.muluken.jobtracker.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return new AuthResponse(
                null,
                null,
                "User registered successfully. Please verify your email.",
                null,
                null
        );
    }

    @GetMapping("/verify")
    public AuthResponse verifyEmail(
            @RequestParam String token
    ) {

        authService.verifyEmail(token);

        return new AuthResponse(
              null,
                null,
                "Email verified successfully",
                null,
                null
        );
    }


    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {

        return authService.login(request);
    }


    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {

        return authService.refreshToken(request);
    }




    @PostMapping("/logout")
    public AuthResponse logout(
            @RequestBody RefreshTokenRequest request
    ) {

        authService.logout(request);

        return new AuthResponse(
                null,
                null,
                "Logout successful",
                null,
                null
        );
    }





}