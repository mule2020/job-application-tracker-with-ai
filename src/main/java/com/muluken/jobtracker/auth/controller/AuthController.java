package com.muluken.jobtracker.auth.controller;

import com.muluken.jobtracker.auth.dto.*;
import com.muluken.jobtracker.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new AuthResponse(null, null,
                "User registered successfully. Please verify your email.", null, null);
    }

    @GetMapping("/verify")
    public AuthResponse verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return new AuthResponse(null, null, "Email verified successfully", null, null);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request);

        // Set refresh token as httpOnly cookie
        Cookie refreshCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);        // set true in production (HTTPS)
        refreshCookie.setPath("/api/auth");    // only sent to /api/auth/* endpoints
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshCookie);

        // Return only accessToken in body
        return new AuthResponse(
                authResponse.getAccessToken(),
                null,  // don't expose refresh token in body
                authResponse.getMessage(),
                authResponse.getEmail(),
                authResponse.getIsVerified()
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(HttpServletRequest request) {
        // Read refresh token from cookie
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken == null) {
            throw new com.muluken.jobtracker.common.exception.ApiException(
                    "No refresh token found", org.springframework.http.HttpStatus.UNAUTHORIZED
            );
        }
        return authService.refreshToken(new RefreshTokenRequest(refreshToken));
    }

    @PostMapping("/logout")
    public AuthResponse logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        //  Read refresh token from cookie
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null) {
            try {
                authService.logout(new RefreshTokenRequest(refreshToken));
            } catch (Exception ignored) {}
        }

        //  Clear the cookie
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0); // delete immediately
        response.addCookie(cookie);

        return new AuthResponse(null, null, "Logout successful", null, null);
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
    public AuthResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return new AuthResponse(null, null,
                "If that email exists, a reset link has been sent.", null, null);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset password using token from email")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new AuthResponse(null, null,
                "Password reset successfully. Please log in.", null, null);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}