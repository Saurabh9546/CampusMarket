package com.campusmarket.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public EmailService(JavaMailSender mailSender, @Value("${app.frontend-url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your CampusMarket account");
        message.setText("Welcome to CampusMarket!\n\nPlease verify your email by clicking the link below:\n"
                + link + "\n\nThis link expires in 24 hours.");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your CampusMarket password");
        message.setText("We received a request to reset your CampusMarket password.\n\n"
                + "Click the link below to set a new password:\n" + link
                + "\n\nThis link expires in 1 hour. If you didn't request this, ignore this email.");
        mailSender.send(message);
    }
}