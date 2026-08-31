package com.campusmarket.backend.message.controller;

import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.message.dto.MessageCreateRequest;
import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ApiResponse<MessageDto> send(@Valid @RequestBody MessageCreateRequest request) {
        Long senderId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        return ApiResponse.success(messageService.send(request.getConversationId(), senderId, request.getMessage()));
    }
}