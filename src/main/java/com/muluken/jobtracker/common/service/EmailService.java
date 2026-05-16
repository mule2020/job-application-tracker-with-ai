package com.muluken.jobtracker.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail-from}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                    <div style="background: #2563eb; width: 40px; height: 40px; border-radius: 10px;
                                display:flex; align-items:center; justify-content:center; margin-bottom:24px;">
                        <span style="color:white; font-size:20px;">✉</span>
                    </div>
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
                    <hr style="border:none; border-top:1px solid #e2e8f0; margin:24px 0;" />
                    <p style="color:#cbd5e1; font-size:12px;">JobTrackr · Built for job seekers</p>
                </div>
                """.formatted(verifyUrl);

        sendEmail(toEmail, "Verify your JobTrackr email", html);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
                    <div style="background: #dc2626; width: 40px; height: 40px; border-radius: 10px;
                                display:flex; align-items:center; justify-content:center; margin-bottom:24px;">
                        <span style="color:white; font-size:20px;">🔒</span>
                    </div>
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
                    <hr style="border:none; border-top:1px solid #e2e8f0; margin:24px 0;" />
                    <p style="color:#cbd5e1; font-size:12px;">JobTrackr · Built for job seekers</p>
                </div>
                """.formatted(resetUrl);

        sendEmail(toEmail, "Reset your JobTrackr password", html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}