package com.campusmarket.backend.user.service;

import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.user.dto.UpdateUserRequest;
import com.campusmarket.backend.user.dto.UserDto;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.mapper.UserMapper;
import com.campusmarket.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto updateProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.getName().trim());
        userRepository.save(user);

        return userMapper.toDto(user);
    }
}