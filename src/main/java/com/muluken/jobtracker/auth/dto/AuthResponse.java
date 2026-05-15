package com.muluken.jobtracker.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String message;
    private String email;
    private Boolean isVerified;
}