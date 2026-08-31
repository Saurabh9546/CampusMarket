package com.campusmarket.backend.user.dto;

import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    private String name;
    private String email;
    private boolean verified;
    private LocalDateTime joinedDate;

    public UserDto(Long id, String name, String email, boolean verified, LocalDateTime joinedDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.verified = verified;
        this.joinedDate = joinedDate;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isVerified() { return verified; }
    public LocalDateTime getJoinedDate() { return joinedDate; }
}