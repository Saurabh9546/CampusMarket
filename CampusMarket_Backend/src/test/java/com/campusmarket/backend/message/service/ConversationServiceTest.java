package com.campusmarket.backend.message.service;

import com.campusmarket.backend.college.entity.College;
import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.message.dto.ConversationDetailDto;
import com.campusmarket.backend.message.dto.ConversationDto;
import com.campusmarket.backend.message.entity.Conversation;
import com.campusmarket.backend.message.mapper.ConversationMapper;
import com.campusmarket.backend.message.mapper.MessageMapper;
import com.campusmarket.backend.message.repository.ConversationRepository;
import com.campusmarket.backend.message.repository.MessageRepository;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.product.repository.ProductRepository;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private ConversationMapper conversationMapper;
    @Mock private MessageMapper messageMapper;

    private ConversationService conversationService;

    private static final Long BUYER_ID = 1L;
    private static final Long SELLER_ID = 2L;
    private static final Long OUTSIDER_ID = 999L;
    private static final Long PRODUCT_ID = 50L;
    private static final Long CONVERSATION_ID = 100L;
    private static final Long COLLEGE_ID = 10L;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(conversationRepository, messageRepository,
                productRepository, userRepository, conversationMapper, messageMapper);
    }

    private Conversation buildConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(CONVERSATION_ID);
        conversation.setBuyerId(BUYER_ID);
        conversation.setSellerId(SELLER_ID);
        conversation.setProductId(PRODUCT_ID);
        return conversation;
    }

    private Product buildProduct(Long collegeId, Long sellerId) {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setTitle("Used Textbook");
        product.setCollegeId(collegeId);
        product.setSellerId(sellerId);
        return product;
    }

    private User buildBuyer(Long collegeId) {
        College college = mock(College.class);
        when(college.getId()).thenReturn(collegeId);

        User buyer = new User();
        buyer.setId(BUYER_ID);
        buyer.setName("Test Buyer");
        buyer.setCollege(college);
        return buyer;
    }

    // ---------- list() ----------

    @Test
    void list_returnsConversationsForUser() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findByBuyerIdOrSellerId(BUYER_ID, BUYER_ID))
                .thenReturn(List.of(conversation));
        when(messageRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID))
                .thenReturn(List.of());
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        when(conversationMapper.toDto(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(ConversationDto.class));

        List<ConversationDto> result = conversationService.list(BUYER_ID);

        assertThat(result).hasSize(1);
    }

    // ---------- create() ----------

    @Test
    void create_returnsExisting_whenConversationAlreadyExists() {
        Conversation existing = buildConversation();
        when(conversationRepository.findByBuyerIdAndProductId(BUYER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(existing));
        when(messageRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID))
                .thenReturn(List.of());
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        when(conversationMapper.toDto(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(ConversationDto.class));

        ConversationDto result = conversationService.create(BUYER_ID, PRODUCT_ID);

        assertThat(result).isNotNull();
        // Existing-conversation path must never touch product/college checks or save
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFound_whenProductDoesNotExist() {
        when(conversationRepository.findByBuyerIdAndProductId(BUYER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.create(BUYER_ID, PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFound_whenBuyerDoesNotExist() {
        Product product = buildProduct(COLLEGE_ID, SELLER_ID);
        when(conversationRepository.findByBuyerIdAndProductId(BUYER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.create(BUYER_ID, PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFound_whenProductIsFromDifferentCollege() {
        Product product = buildProduct(COLLEGE_ID, SELLER_ID);
        User buyer = buildBuyer(COLLEGE_ID + 1); // different college

        when(conversationRepository.findByBuyerIdAndProductId(BUYER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() -> conversationService.create(BUYER_ID, PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenBuyerIsAlsoSeller() {
        Product product = buildProduct(COLLEGE_ID, BUYER_ID); // seller == buyer
        User buyer = buildBuyer(COLLEGE_ID);

        when(conversationRepository.findByBuyerIdAndProductId(BUYER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() -> conversationService.create(BUYER_ID, PRODUCT_ID))
                .isInstanceOf(ConflictException.class);

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void create_savesNewConversation_whenValid() {
        Product product = buildProduct(COLLEGE_ID, SELLER_ID);
        User buyer = buildBuyer(COLLEGE_ID);

        when(conversationRepository.findByBuyerIdAndProductId(BUYER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.of(buyer));
        // buildListDto() lookups after save
        when(messageRepository.findByConversationIdOrderBySentAtAsc(any())).thenReturn(List.of());
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.empty());
        when(conversationMapper.toDto(any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(ConversationDto.class));

        ConversationDto result = conversationService.create(BUYER_ID, PRODUCT_ID);

        assertThat(result).isNotNull();

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        Conversation saved = captor.getValue();
        assertThat(saved.getBuyerId()).isEqualTo(BUYER_ID);
        assertThat(saved.getSellerId()).isEqualTo(SELLER_ID);
        assertThat(saved.getProductId()).isEqualTo(PRODUCT_ID);
    }

    // ---------- getDetail() ----------

    @Test
    void getDetail_throwsResourceNotFound_whenConversationDoesNotExist() {
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getDetail(CONVERSATION_ID, BUYER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDetail_throwsForbidden_whenRequesterIsNotParticipant() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.getDetail(CONVERSATION_ID, OUTSIDER_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getDetail_returnsDetail_whenRequesterIsParticipant() {
        Conversation conversation = buildConversation();
        when(conversationRepository.findById(CONVERSATION_ID)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderBySentAtAsc(CONVERSATION_ID)).thenReturn(List.of());
        when(userRepository.findById(SELLER_ID)).thenReturn(Optional.empty());
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        when(conversationMapper.toDetailDto(any(), any(), any(), any(), any()))
                .thenReturn(mock(ConversationDetailDto.class));

        ConversationDetailDto result = conversationService.getDetail(CONVERSATION_ID, BUYER_ID);

        assertThat(result).isNotNull();
    }
}