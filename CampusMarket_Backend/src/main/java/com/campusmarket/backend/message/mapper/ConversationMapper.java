package com.campusmarket.backend.message.mapper;

import com.campusmarket.backend.message.dto.ConversationDetailDto;
import com.campusmarket.backend.message.dto.ConversationDto;
import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.entity.Conversation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ConversationMapper {

    public ConversationDto toDto(Conversation conversation, Long otherPartyId, String otherPartyName,
                                  String productTitle, String lastMessagePreview, LocalDateTime lastMessageAt) {
        return new ConversationDto(
                conversation.getId(),
                conversation.getProductId(),
                conversation.getBuyerId(),
                conversation.getSellerId(),
                conversation.getCreatedAt(),
                otherPartyId,
                otherPartyName,
                productTitle,
                lastMessagePreview,
                lastMessageAt
        );
    }

    public ConversationDetailDto toDetailDto(Conversation conversation, List<MessageDto> messages,
                                              Long otherPartyId, String otherPartyName, String productTitle) {
        return new ConversationDetailDto(
                conversation.getId(),
                conversation.getProductId(),
                conversation.getBuyerId(),
                conversation.getSellerId(),
                conversation.getCreatedAt(),
                messages,
                otherPartyId,
                otherPartyName,
                productTitle
        );
    }
}