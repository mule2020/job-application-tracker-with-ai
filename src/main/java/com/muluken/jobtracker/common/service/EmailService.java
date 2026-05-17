package com.muluken.jobtracker.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class EmailService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail-from}")
    private String fromEmail;

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                    <h1 style="font-size:24px; font-weight:700; color:#0f172a; margin-bottom:8px;">
                        Verify your email
                    </h1>
                    <p style="color:#64748b; font-size:15px; line-height:1.6; margin-bottom:24px;">
                        Thanks for signing up for JobTrackr! Click the button below to verify
                        your email address and activate your account.
                    </p>
                    <a href="%s"
                       style="display:inline-block; background:#2563eb; color:white;
                              padding:12px 24px; border-radius:10px; text-decoration:none;
                              font-weight:600; font-size:14px; margin-bottom:24px;">
                        Verify Email Address
                    </a>
                    <p style="color:#94a3b8; font-size:13px;">
                        This link expires in 24 hours. If you didn't create an account,
                        you can safely ignore this email.
                    </p>
                </div>
                """.formatted(verifyUrl);

        sendEmail(toEmail, "Verify your JobTrackr email", html);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                    <h1 style="font-size:24px; font-weight:700; color:#0f172a; margin-bottom:8px;">
                        Reset your password
                    </h1>
                    <p style="color:#64748b; font-size:15px; line-height:1.6; margin-bottom:24px;">
                        We received a request to reset your JobTrackr password.
                        Click the button below to choose a new password.
                    </p>
                    <a href="%s"
                       style="display:inline-block; background:#dc2626; color:white;
                              padding:12px 24px; border-radius:10px; text-decoration:none;
                              font-weight:600; font-size:14px; margin-bottom:24px;">
                        Reset Password
                    </a>
                    <p style="color:#94a3b8; font-size:13px;">
                        This link expires in 1 hour. If you didn't request a password reset,
                        you can safely ignore this email.
                    </p>
                </div>
                """.formatted(resetUrl);

        sendEmail(toEmail, "Reset your JobTrackr password", html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            String body = """
                    {
                        "sender": {"email": "%s"},
                        "to": [{"email": "%s"}],
                        "subject": "%s",
                        "htmlContent": %s
                    }
                    """.formatted(
                    fromEmail,
                    to,
                    subject,
                    toJsonString(html)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", brevoApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 201) {
                log.info("Email sent to {}", to);
            } else {
                log.error("Failed to send email. Status: {}, Body: {}",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String toJsonString(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}