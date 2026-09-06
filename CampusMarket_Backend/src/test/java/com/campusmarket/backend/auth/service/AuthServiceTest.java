package com.campusmarket.backend.auth.service;

import com.campusmarket.backend.auth.dto.LoginRequest;
import com.campusmarket.backend.auth.dto.RegisterRequest;
import com.campusmarket.backend.college.entity.College;
import com.campusmarket.backend.college.repository.CollegeRepository;
import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.exception.UnauthorizedException;
import com.campusmarket.backend.security.JwtService;
import com.campusmarket.backend.security.PasswordResetTokenRepository;
import com.campusmarket.backend.security.RefreshTokenService;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.mapper.UserMapper;
import com.campusmarket.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CollegeRepository collegeRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private EmailService emailService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private College college;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("student@dumka.ac.in");
        registerRequest.setPassword("password123");

        college = new College();
        college.setId(1L);
        college.setEmailDomain("dumka.ac.in");
        college.setName("Dumka Engineering College");
    }

    @Test
    void register_savesUserAndSendsEmail_whenEmailAndDomainAreValid() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(collegeRepository.findByEmailDomain("dumka.ac.in")).thenReturn(Optional.of(college));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(42L);
            return u;
        });

        Long userId = authService.register(registerRequest);

        assertThat(userId).isEqualTo(42L);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("student@dumka.ac.in"), anyString());
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void register_throwsResourceNotFound_whenCollegeDomainIsUnrecognized() {
        registerRequest.setEmail("student@unknown-college.edu");
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(collegeRepository.findByEmailDomain("unknown-college.edu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Unrecognized college email domain");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_throwsUnauthorized_whenEmailDoesNotExist() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("ghost@dumka.ac.in");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_throwsUnauthorized_whenPasswordDoesNotMatch() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("student@dumka.ac.in");
        loginRequest.setPassword("wrong-password");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("student@dumka.ac.in");
        existingUser.setPassword("hashed-correct-password");
        existingUser.setVerified(true);

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "hashed-correct-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_throwsUnauthorized_whenAccountIsNotVerified() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("student@dumka.ac.in");
        loginRequest.setPassword("password123");

        User unverifiedUser = new User();
        unverifiedUser.setId(1L);
        unverifiedUser.setEmail("student@dumka.ac.in");
        unverifiedUser.setPassword("hashed-password");
        unverifiedUser.setVerified(false);

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("verify your email");
    }
}