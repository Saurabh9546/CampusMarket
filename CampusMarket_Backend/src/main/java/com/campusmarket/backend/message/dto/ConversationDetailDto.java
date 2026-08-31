package com.campusmarket.backend.message.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConversationDetailDto {
    private final Long id;
    private final Long productId;
    private final Long buyerId;
    private final Long sellerId;
    private final LocalDateTime createdAt;
    private final List<MessageDto> messages;
    private final Long otherPartyId;
    private final String otherPartyName;
    private final String productTitle;

    public ConversationDetailDto(Long id, Long productId, Long buyerId, Long sellerId,
                                  LocalDateTime createdAt, List<MessageDto> messages,
                                  Long otherPartyId, String otherPartyName, String productTitle) {
        this.id = id;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.createdAt = createdAt;
        this.messages = messages;
        this.otherPartyId = otherPartyId;
        this.otherPartyName = otherPartyName;
        this.productTitle = productTitle;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getBuyerId() { return buyerId; }
    public Long getSellerId() { return sellerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<MessageDto> getMessages() { return messages; }
    public Long getOtherPartyId() { return otherPartyId; }
    public String getOtherPartyName() { return otherPartyName; }
    public String getProductTitle() { return productTitle; }
}