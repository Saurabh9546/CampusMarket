package com.campusmarket.backend.message.dto;

import java.time.LocalDateTime;

public class MessageDto {
    private final Long id;
    private final Long conversationId;
    private final Long senderId;
    private final String message;
    private final LocalDateTime sentAt;

    public MessageDto(Long id, Long conversationId, Long senderId, String message, LocalDateTime sentAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public Long getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public LocalDateTime getSentAt() { return sentAt; }
}