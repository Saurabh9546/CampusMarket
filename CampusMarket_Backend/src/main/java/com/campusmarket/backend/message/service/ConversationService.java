package com.campusmarket.backend.message.service;

import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.message.dto.ConversationDetailDto;
import com.campusmarket.backend.message.dto.ConversationDto;
import com.campusmarket.backend.message.dto.MessageDto;
import com.campusmarket.backend.message.entity.Conversation;
import com.campusmarket.backend.message.entity.Message;
import com.campusmarket.backend.message.mapper.ConversationMapper;
import com.campusmarket.backend.message.mapper.MessageMapper;
import com.campusmarket.backend.message.repository.ConversationRepository;
import com.campusmarket.backend.message.repository.MessageRepository;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.product.repository.ProductRepository;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {
    private static final int PREVIEW_MAX_LENGTH = 60;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                                ProductRepository productRepository, UserRepository userRepository,
                                ConversationMapper conversationMapper, MessageMapper messageMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    public List<ConversationDto> list(Long userId) {
        return conversationRepository.findByBuyerIdOrSellerId(userId, userId)
                .stream()
                .map(c -> buildListDto(c, userId))
                .toList();
    }

    public ConversationDto create(Long buyerId, Long productId) {
        // Check for an existing conversation FIRST. If buyer and seller
        // already have a thread about this product, keep it working
        // regardless of college — that thread was already legitimately
        // established. The college check below only applies to genuinely
        // NEW conversations, so it can't retroactively break something
        // that already exists (e.g. seeded test data, or any conversation
        // created before this check was added).
        Conversation existing = conversationRepository.findByBuyerIdAndProductId(buyerId, productId).orElse(null);
        if (existing != null) {
            return buildListDto(existing, buyerId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // College-scoping enforced only when starting a brand-new conversation.
        if (!product.getCollegeId().equals(buyer.getCollege().getId())) {
            throw new ResourceNotFoundException("Listing not found");
        }

        Long sellerId = product.getSellerId();
        if (sellerId.equals(buyerId)) {
            throw new ConflictException("You cannot message yourself about your own listing", "CANNOT_MESSAGE_SELF");
        }

        Conversation conversation = new Conversation();
        conversation.setBuyerId(buyerId);
        conversation.setSellerId(sellerId);
        conversation.setProductId(productId);
        conversationRepository.save(conversation);

        return buildListDto(conversation, buyerId);
    }

    public ConversationDetailDto getDetail(Long conversationId, Long requestingUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        assertParticipant(conversation, requestingUserId);

        List<MessageDto> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(messageMapper::toDto)
                .toList();

        Long otherPartyId = otherPartyId(conversation, requestingUserId);
        String otherPartyName = resolveUserName(otherPartyId);
        String productTitle = resolveProductTitle(conversation.getProductId());

        return conversationMapper.toDetailDto(conversation, messages, otherPartyId, otherPartyName, productTitle);
    }

    private ConversationDto buildListDto(Conversation conversation, Long requestingUserId) {
        Long otherPartyId = otherPartyId(conversation, requestingUserId);
        String otherPartyName = resolveUserName(otherPartyId);
        String productTitle = resolveProductTitle(conversation.getProductId());

        // NOTE: fetches the full message list just to read the last entry.
        // Acceptable for expected conversation volume in this app; if this
        // module ever needs to scale, replace with a dedicated
        // "most recent message per conversation" repository query instead.
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId());
        Message last = messages.isEmpty() ? null : messages.get(messages.size() - 1);
        String preview = last != null ? truncate(last.getMessage()) : null;
        LocalDateTime lastAt = last != null ? last.getSentAt() : null;

        return conversationMapper.toDto(conversation, otherPartyId, otherPartyName, productTitle, preview, lastAt);
    }

    private Long otherPartyId(Conversation conversation, Long requestingUserId) {
        return conversation.getBuyerId().equals(requestingUserId)
                ? conversation.getSellerId()
                : conversation.getBuyerId();
    }

    private String resolveUserName(Long userId) {
        return userRepository.findById(userId).map(User::getName).orElse("Unknown user");
    }

    private String resolveProductTitle(Long productId) {
        return productRepository.findById(productId).map(Product::getTitle).orElse("Listing removed");
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() <= PREVIEW_MAX_LENGTH ? text : text.substring(0, PREVIEW_MAX_LENGTH) + "…";
    }

    void assertParticipant(Conversation conversation, Long userId) {
        if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
            throw new ForbiddenException("NOT_A_PARTICIPANT");
        }
    }
}