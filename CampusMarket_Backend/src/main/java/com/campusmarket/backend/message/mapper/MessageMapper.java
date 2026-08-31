package com.campusmarket.backend.message.mapper;

import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getMessage(),
                message.getSentAt()
        );
    }
}