package com.campusmarket.backend.auth.service;

import com.campusmarket.backend.auth.dto.LoginRequest;
import com.campusmarket.backend.auth.dto.RegisterRequest;
import com.campusmarket.backend.college.entity.College;
import com.campusmarket.backend.college.repository.CollegeRepository;
import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.exception.UnauthorizedException;
import com.campusmarket.backend.security.JwtService;
import com.campusmarket.backend.security.PasswordResetToken;
import com.campusmarket.backend.security.PasswordResetTokenRepository;
import com.campusmarket.backend.security.RefreshTokenService;
import com.campusmarket.backend.user.dto.UserDto;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.mapper.UserMapper;
import com.campusmarket.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public record LoginResult(String accessToken, String refreshToken, UserDto userDto) {}

    public AuthService(UserRepository userRepository, CollegeRepository collegeRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper,
                        RefreshTokenService refreshTokenService, EmailService emailService,
                        PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.collegeRepository = collegeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public Long register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists", "EMAIL_ALREADY_REGISTERED");
        }

        String domain = extractDomain(request.getEmail());
        College college = collegeRepository.findByEmailDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Unrecognized college email domain"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCollege(college);
        user.setVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return user.getId();
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS");
        }

        if (!user.isVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in", "UNVERIFIED_ACCOUNT");
        }

        String accessToken = jwtService.generateAccessToken(user.getId().toString(), true);
        String refreshToken = refreshTokenService.issueToken(user.getId());
        return new LoginResult(accessToken, refreshToken, userMapper.toDto(user));
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or already-used verification link"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("This verification link has expired", "TOKEN_EXPIRED");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    /**
     * Always returns silently, even if the email doesn't exist or is already
     * verified — prevents leaking which emails are registered.
     */
    public void resendVerification(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isVerified()) return;

            user.setVerificationToken(UUID.randomUUID().toString());
            user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

            emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        });
    }

    public String refresh(String rawRefreshToken) {
        Long userId = refreshTokenService.validateAndGetUserId(rawRefreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found", "INVALID_CREDENTIALS"));
        return jwtService.generateAccessToken(user.getId().toString(), user.isVerified());
    }

    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    /**
     * Always returns silently, even if the email doesn't exist — prevents
     * leaking which emails are registered (standard security practice for
     * forgot-password flows).
     */
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUserId(user.getId());
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset link"));

        if (resetToken.isUsed()) {
            throw new UnauthorizedException("This reset link has already been used", "TOKEN_ALREADY_USED");
        }
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("This reset link has expired", "TOKEN_EXPIRED");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String extractDomain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }
}