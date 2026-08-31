package com.campusmarket.backend.message.controller;

import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.message.dto.ConversationCreateRequest;
import com.campusmarket.backend.message.dto.ConversationDetailDto;
import com.campusmarket.backend.message.dto.ConversationDto;
import com.campusmarket.backend.message.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ApiResponse<List<ConversationDto>> list() {
        return ApiResponse.success(conversationService.list(currentUserId()));
    }

    @PostMapping
    public ApiResponse<ConversationDto> create(@Valid @RequestBody ConversationCreateRequest request) {
        return ApiResponse.success(conversationService.create(currentUserId(), request.getProductId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConversationDetailDto> getDetail(@PathVariable Long id) {
        return ApiResponse.success(conversationService.getDetail(id, currentUserId()));
    }

    private Long currentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}