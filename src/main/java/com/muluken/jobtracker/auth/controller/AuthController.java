package com.muluken.jobtracker.auth.controller;

import com.muluken.jobtracker.auth.dto.*;
import com.muluken.jobtracker.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Change password for logged-in user")
    @SecurityRequirement(name = "bearerAuth")
    public AuthResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        authService.changePassword(authentication.getName(), request);
        return new AuthResponse(null, null, "Password changed successfully", null, null);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request a password reset email")
    public AuthResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        // Always return same message — don't reveal if email exists
        return new AuthResponse(null, null,
                "If that email exists, a reset link has been sent.", null, null);
    }
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset password using token from email")
    public AuthResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return new AuthResponse(null, null, "Password reset successfully. Please log in.", null, null);
    }


}