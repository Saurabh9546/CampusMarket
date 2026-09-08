package com.campusmarket.backend.auth.controller;

import com.campusmarket.backend.auth.dto.LoginRequest;
import com.campusmarket.backend.auth.dto.RegisterRequest;
import com.campusmarket.backend.auth.service.AuthService;
import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.exception.UnauthorizedException;
import com.campusmarket.backend.user.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("student@dumka.ac.in");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("student@dumka.ac.in");
        loginRequest.setPassword("password123");
    }

    // ---------- POST /register ----------

    @Test
    void register_returns200_whenRequestIsValid() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(42L);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(42));
    }

    @Test
    void register_returns400_whenEmailIsInvalid() throws Exception {
        registerRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(authService, never()).register(any());
    }

    @Test
    void register_returns400_whenPasswordTooShort() throws Exception {
        registerRequest.setPassword("short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(authService, never()).register(any());
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("An account with this email already exists", "EMAIL_ALREADY_REGISTERED"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void register_returns404_whenCollegeDomainUnrecognized() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResourceNotFoundException("Unrecognized college email domain"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    // ---------- POST /login ----------

    @Test
    void login_returns200AndSetsRefreshCookie_whenCredentialsAreValid() throws Exception {
        AuthService.LoginResult result = new AuthService.LoginResult(
                "fake-access-token", "fake-refresh-token", mock(UserDto.class));
        when(authService.login(any(LoginRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("fake-access-token"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void login_returns401_whenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_returns401_whenAccountUnverified() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Please verify your email before logging in", "UNVERIFIED_ACCOUNT"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNVERIFIED_ACCOUNT"));
    }

    // ---------- POST /logout ----------

    @Test
    void logout_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("NOT_AUTHENTICATED"));

        verify(authService, never()).logout(any());
    }

    @Test
    @WithMockUser(username = "7")
    void logout_returns200_whenAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout(7L);
    }

    // ---------- GET /verify ----------

    @Test
    void verify_returns404_whenTokenInvalid() throws Exception {
        doThrow(new ResourceNotFoundException("Invalid or already-used verification link"))
                .when(authService).verifyEmail("bad-token");

        mockMvc.perform(get("/api/v1/auth/verify").param("token", "bad-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verify_returns200_whenTokenValid() throws Exception {
        mockMvc.perform(get("/api/v1/auth/verify").param("token", "good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).verifyEmail("good-token");
    }

    // ---------- POST /refresh ----------

    @Test
    void refresh_returns401_whenCookieMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("REFRESH_TOKEN_MISSING"));

        verify(authService, never()).refresh(any());
    }

    @Test
    void refresh_returns200_whenCookiePresent() throws Exception {
        when(authService.refresh("valid-refresh-token")).thenReturn("new-access-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }
}