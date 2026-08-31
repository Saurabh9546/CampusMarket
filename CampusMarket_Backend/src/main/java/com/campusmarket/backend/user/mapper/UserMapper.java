package com.campusmarket.backend.user.mapper;

import com.campusmarket.backend.user.dto.UserDto;
import com.campusmarket.backend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isVerified(),
                user.getJoinedDate()
        );
    }
}