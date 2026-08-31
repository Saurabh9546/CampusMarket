package com.campusmarket.backend.message.service;

import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.entity.Conversation;
import com.campusmarket.backend.message.entity.Message;
import com.campusmarket.backend.message.mapper.MessageMapper;
import com.campusmarket.backend.message.repository.ConversationRepository;
import com.campusmarket.backend.message.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;

    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository,
                           MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.messageMapper = messageMapper;
    }

    public MessageDto send(Long conversationId, Long senderId, String text) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getBuyerId().equals(senderId) && !conversation.getSellerId().equals(senderId)) {
            throw new ForbiddenException("NOT_A_PARTICIPANT");
        }

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setMessage(text);

        return messageMapper.toDto(messageRepository.save(message));
    }
}