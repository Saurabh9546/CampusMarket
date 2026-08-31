package com.campusmarket.backend.auth.dto;

import com.campusmarket.backend.user.dto.UserDto;

public class LoginResponse {
    private String accessToken;
    private UserDto user;

    public LoginResponse(String accessToken, UserDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public UserDto getUser() { return user; }
}