package com.campusmarket.backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageCreateRequest {
    @NotNull(message = "Conversation id is required")
    private Long conversationId;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}