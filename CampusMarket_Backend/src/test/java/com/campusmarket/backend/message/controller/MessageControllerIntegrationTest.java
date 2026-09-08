package com.campusmarket.backend.message.controller;

import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.message.dto.MessageCreateRequest;
import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class MessageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    private MockMvc mockMvc;

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 100L;

    private MessageCreateRequest sendRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        sendRequest = new MessageCreateRequest();
        sendRequest.setConversationId(CONVERSATION_ID);
        sendRequest.setMessage("Hey, is this still available?");
    }

    private MessageDto sampleDto() {
        return new MessageDto(1L, CONVERSATION_ID, USER_ID, "Hey, is this still available?", LocalDateTime.now());
    }

    // ---------- POST /messages ----------

    @Test
    @WithMockUser(username = "1")
    void send_returns200_whenValid() throws Exception {
        when(messageService.send(CONVERSATION_ID, USER_ID, "Hey, is this still available?"))
                .thenReturn(sampleDto());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Hey, is this still available?"));
    }

    @Test
    void send_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1")
    void send_returns400_whenMessageIsBlank() throws Exception {
        sendRequest.setMessage("");

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(messageService, never()).send(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "1")
    void send_returns404_whenConversationNotFound() throws Exception {
        when(messageService.send(CONVERSATION_ID, USER_ID, "Hey, is this still available?"))
                .thenThrow(new ResourceNotFoundException("Conversation not found"));

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1")
    void send_returns403_whenNotParticipant() throws Exception {
        when(messageService.send(CONVERSATION_ID, USER_ID, "Hey, is this still available?"))
                .thenThrow(new ForbiddenException("NOT_A_PARTICIPANT"));

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("NOT_A_PARTICIPANT"));
    }
}