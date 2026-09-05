package com.campusmarket.backend.user.controller;

import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.user.dto.UpdateUserRequest;
import com.campusmarket.backend.user.dto.UserDto;
import com.campusmarket.backend.user.mapper.UserMapper;
import com.campusmarket.backend.user.repository.UserRepository;
import com.campusmarket.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserMapper userMapper, UserService userService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me() {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ApiResponse.success(userMapper.toDto(user));
    }

    @PatchMapping("/me")
    public ApiResponse<UserDto> updateMe(@Valid @RequestBody UpdateUserRequest request) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        UserDto updated = userService.updateProfile(userId, request);
        return ApiResponse.success(updated);
    }
}