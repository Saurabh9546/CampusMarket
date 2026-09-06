package com.campusmarket.backend.auth.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;
    private final String frontendUrl;
    private final String fromAddress;

    public EmailService(
            @Value("${resend.api-key}") String resendApiKey,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${resend.from-address}") String fromAddress) {
        this.resend = new Resend(resendApiKey);
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String body = "Welcome to CampusMarket!<br><br>Please verify your email by clicking the link below:<br>"
                + "<a href=\"" + link + "\">" + link + "</a><br><br>This link expires in 24 hours.";
        send(toEmail, "Verify your CampusMarket account", body);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String body = "We received a request to reset your CampusMarket password.<br><br>"
                + "Click the link below to set a new password:<br>"
                + "<a href=\"" + link + "\">" + link + "</a>"
                + "<br><br>This link expires in 1 hour. If you didn't request this, ignore this email.";
        send(toEmail, "Reset your CampusMarket password", body);
    }

    private void send(String toEmail, String subject, String htmlBody) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(toEmail)
                .subject(subject)
                .html(htmlBody)
                .build();
        try {
            resend.emails().send(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Resend", e);
        }
    }
}