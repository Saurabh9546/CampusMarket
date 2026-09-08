package com.campusmarket.backend.message.controller;

import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.message.dto.*;
import com.campusmarket.backend.message.service.ConversationService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ConversationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    private MockMvc mockMvc;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 50L;
    private static final Long CONVERSATION_ID = 100L;
    private static final Long SELLER_ID = 2L;

    private ConversationCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        createRequest = new ConversationCreateRequest();
        createRequest.setProductId(PRODUCT_ID);
    }

    private ConversationDto sampleDto() {
        return new ConversationDto(CONVERSATION_ID, PRODUCT_ID, USER_ID, SELLER_ID,
                LocalDateTime.now(), SELLER_ID, "Seller Name", "Used Textbook", null, null);
    }

    private ConversationDetailDto sampleDetailDto() {
        MessageDto message = new MessageDto(1L, CONVERSATION_ID, USER_ID, "Hey, still available?", LocalDateTime.now());
        return new ConversationDetailDto(CONVERSATION_ID, PRODUCT_ID, USER_ID, SELLER_ID,
                LocalDateTime.now(), List.of(message), SELLER_ID, "Seller Name", "Used Textbook");
    }

    // ---------- GET /conversations ----------

    @Test
    @WithMockUser(username = "1")
    void list_returns200_withConversations() throws Exception {
        when(conversationService.list(USER_ID)).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/v1/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.data[0].otherPartyName").value("Seller Name"));
    }

    @Test
    void list_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/conversations"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- POST /conversations ----------

    @Test
    @WithMockUser(username = "1")
    void create_returns200_whenValid() throws Exception {
        when(conversationService.create(USER_ID, PRODUCT_ID)).thenReturn(sampleDto());

        mockMvc.perform(post("/api/v1/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productTitle").value("Used Textbook"));
    }

    @Test
    @WithMockUser(username = "1")
    void create_returns400_whenProductIdMissing() throws Exception {
        createRequest.setProductId(null);

        mockMvc.perform(post("/api/v1/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(conversationService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = "1")
    void create_returns404_whenProductNotFoundOrWrongCollege() throws Exception {
        when(conversationService.create(USER_ID, PRODUCT_ID))
                .thenThrow(new ResourceNotFoundException("Listing not found"));

        mockMvc.perform(post("/api/v1/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "1")
    void create_returns409_whenMessagingSelf() throws Exception {
        when(conversationService.create(USER_ID, PRODUCT_ID))
                .thenThrow(new ConflictException("You cannot message yourself about your own listing", "CANNOT_MESSAGE_SELF"));

        mockMvc.perform(post("/api/v1/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CANNOT_MESSAGE_SELF"));
    }

    // ---------- GET /conversations/{id} ----------

    @Test
    @WithMockUser(username = "1")
    void getDetail_returns200_whenParticipant() throws Exception {
        when(conversationService.getDetail(CONVERSATION_ID, USER_ID)).thenReturn(sampleDetailDto());

        mockMvc.perform(get("/api/v1/conversations/{id}", CONVERSATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages[0].message").value("Hey, still available?"));
    }

    @Test
    @WithMockUser(username = "1")
    void getDetail_returns403_whenNotParticipant() throws Exception {
        when(conversationService.getDetail(CONVERSATION_ID, USER_ID))
                .thenThrow(new ForbiddenException("NOT_A_PARTICIPANT"));

        mockMvc.perform(get("/api/v1/conversations/{id}", CONVERSATION_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("NOT_A_PARTICIPANT"));
    }

    @Test
    @WithMockUser(username = "1")
    void getDetail_returns404_whenConversationNotFound() throws Exception {
        when(conversationService.getDetail(CONVERSATION_ID, USER_ID))
                .thenThrow(new ResourceNotFoundException("Conversation not found"));

        mockMvc.perform(get("/api/v1/conversations/{id}", CONVERSATION_ID))
                .andExpect(status().isNotFound());
    }
}