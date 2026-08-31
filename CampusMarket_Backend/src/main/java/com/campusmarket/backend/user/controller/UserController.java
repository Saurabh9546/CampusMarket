package com.campusmarket.backend.user.controller;

import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.user.dto.UserDto;
import com.campusmarket.backend.user.mapper.UserMapper;
import com.campusmarket.backend.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me() {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ApiResponse.success(userMapper.toDto(user));
    }
}