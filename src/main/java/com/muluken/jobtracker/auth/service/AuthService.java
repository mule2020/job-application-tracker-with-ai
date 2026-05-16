package com.muluken.jobtracker.auth.service;

import com.muluken.jobtracker.auth.dto.*;
import com.muluken.jobtracker.auth.model.RefreshToken;
import com.muluken.jobtracker.auth.repository.RefreshTokenRepository;
import com.muluken.jobtracker.common.exception.ApiException;
import com.muluken.jobtracker.common.service.EmailService;
import com.muluken.jobtracker.security.jwt.JwtService;
import com.muluken.jobtracker.user.model.User;
import com.muluken.jobtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;

    //REGISTER
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        System.out.println(
                "Verify your email by clicking the link: " + user.getVerificationToken()
        );
    }

    // VERIFY EMAIL
    public void verifyEmail(String token) {

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() ->
                        new ApiException("Invalid verification token", HttpStatus.BAD_REQUEST)
                );
        if (user.getIsVerified()) {
            return;
        }
        user.setIsVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED)
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (!user.getIsVerified()) {
            throw new ApiException("Email not verified", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(refreshToken);
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenEntity.setRevoked(false);

        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, refreshToken, "Login successful", user.getEmail(),
                user.getIsVerified());
    }

    // REFRESH TOKEN
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        String email;
        try {
            email = jwtService.extractEmail(request.getRefreshToken());
        } catch (Exception e) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND)
                );

        boolean validTokenExists = refreshTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .filter(token -> !token.getRevoked())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .anyMatch(token -> request.getRefreshToken().equals(token.getTokenHash()));

        if (!validTokenExists) {
            throw new ApiException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }

        String newAccessToken = jwtService.generateAccessToken(email);

        return new AuthResponse(newAccessToken, request.getRefreshToken(), "Token refreshed", null, null);
    }

    // LOGOUT
    public void logout(RefreshTokenRequest request) {

        refreshTokenRepository.findAll()
                .stream()
                .filter(token -> request.getRefreshToken().equals(token.getTokenHash()))
                .findFirst()
                .ifPresentOrElse(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                }, () -> {
                    throw new ApiException("Refresh token not found", HttpStatus.BAD_REQUEST);
                });
    }


    //Change Password
    public void changePassword(String email, ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException("User not found", HttpStatus.NOT_FOUND)
                );

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Revoke all refresh tokens — force re-login on all devices
        refreshTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .filter(token -> !token.getRevoked())
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    // Forgot Password

    public void forgotPassword(ForgotPasswordRequest request) {

        // Always return success even if email not found — security best practice
        // Don't reveal whether an email exists in the system
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        });
    }

    // Reset Password

    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() ->
                        new ApiException("Invalid or expired reset token", HttpStatus.BAD_REQUEST)
                );

        if (user.getPasswordResetTokenExpiresAt() == null ||
                user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        userRepository.save(user);
        // Revoke all refresh tokens — force re-login
        refreshTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUser().getId().equals(user.getId()))
                .filter(token -> !token.getRevoked())
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
}
