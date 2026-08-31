package com.campusmarket.backend.auth.controller;

import com.campusmarket.backend.auth.dto.ForgotPasswordRequest;
import com.campusmarket.backend.auth.dto.LoginRequest;
import com.campusmarket.backend.auth.dto.LoginResponse;
import com.campusmarket.backend.auth.dto.RegisterRequest;
import com.campusmarket.backend.auth.dto.ResetPasswordRequest;
import com.campusmarket.backend.auth.service.AuthService;
import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ApiResponse.success(Map.of("userId", userId));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request);

        setRefreshCookie(response, result.refreshToken());
        return ApiResponse.success(new LoginResponse(result.accessToken(), result.userDto()));
    }

    @GetMapping("/verify")
    public ApiResponse<Void> verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return ApiResponse.success(null);
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(HttpServletRequest request) {
        String rawToken = extractCookie(request, "refreshToken");
        String newAccessToken = authService.refresh(rawToken);
        return ApiResponse.success(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new UnauthorizedException("Not authenticated", "NOT_AUTHENTICATED");
        }
        Long userId = Long.valueOf(auth.getName());

        authService.logout(userId);
        clearRefreshCookie(response);
        return ApiResponse.success(null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ApiResponse.success(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.success(null);
    }

    private void setRefreshCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie("refreshToken", rawToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            throw new UnauthorizedException("Refresh token cookie missing", "REFRESH_TOKEN_MISSING");
        }
        for (Cookie c : request.getCookies()) {
            if (c.getName().equals(name)) return c.getValue();
        }
        throw new UnauthorizedException("Refresh token cookie missing", "REFRESH_TOKEN_MISSING");
    }
}