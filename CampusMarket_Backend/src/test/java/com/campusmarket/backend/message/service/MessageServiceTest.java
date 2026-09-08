package com.campusmarket.backend.message.service;

import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.entity.Conversation;
import com.campusmarket.backend.message.entity.Message;
import com.campusmarket.backend.message.mapper.MessageMapper;
import com.campusmarket.backend.message.repository.ConversationRepository;
import com.campusmarket.backend.message.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageMapper messageMapper;

    private MessageService messageService;

    private static final Long CONVERSATION_ID = 100L;
    private static final Long BUYER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long OUTSIDER_ID = 999L;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, conversationRepository, messageMapper);
    }

    private Conversation buildConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(CONVERSATION_ID);
        conversation.setBuyerId(BUYER_ID);
        conversation.setSellerId(SELLER_ID);
        conversation.setProductId(50L);
        return conversation;
    }

    @Test
    void send_savesMessage_whenSenderIsBuyer() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageMapper.toDto(any(Message.class))).thenReturn(mock(MessageDto.class));

        MessageDto result = messageService.send(CONVERSATION_ID, BUYER_ID, "Hey, is this still available?");

        assertThat(result).isNotNull();

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message saved = captor.getValue();
        assertThat(saved.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(saved.getSenderId()).isEqualTo(BUYER_ID);
        assertThat(saved.getMessage()).isEqualTo("Hey, is this still available?");
    }

    @Test
    void send_savesMessage_whenSenderIsSeller() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageMapper.toDto(any(Message.class))).thenReturn(mock(MessageDto.class));

        MessageDto result = messageService.send(CONVERSATION_ID, SELLER_ID, "Yes, still have it.");

        assertThat(result).isNotNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void send_throwsResourceNotFound_whenConversationDoesNotExist() {
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.send(CONVERSATION_ID, BUYER_ID, "Hello"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void send_throwsForbidden_whenSenderIsNotAParticipant() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.send(CONVERSATION_ID, OUTSIDER_ID, "I shouldn't be able to send this"))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).save(any());
    }
}