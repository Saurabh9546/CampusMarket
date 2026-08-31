package com.campusmarket.backend.message.dto;

import java.time.LocalDateTime;

public class ConversationDto {
    private final Long id;
    private final Long productId;
    private final Long buyerId;
    private final Long sellerId;
    private final LocalDateTime createdAt;
    /** Whichever of buyer/seller is NOT the requesting user — computed
     * server-side since the frontend has no reliable way to know which
     * side of the conversation the current user is on without this. */
    private final Long otherPartyId;
    private final String otherPartyName;
    private final String productTitle;
    /** Nullable — a conversation can exist with zero messages sent yet. */
    private final String lastMessagePreview;
    private final LocalDateTime lastMessageAt;

    public ConversationDto(Long id, Long productId, Long buyerId, Long sellerId, LocalDateTime createdAt,
                            Long otherPartyId, String otherPartyName, String productTitle,
                            String lastMessagePreview, LocalDateTime lastMessageAt) {
        this.id = id;
        this.productId = productId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.createdAt = createdAt;
        this.otherPartyId = otherPartyId;
        this.otherPartyName = otherPartyName;
        this.productTitle = productTitle;
        this.lastMessagePreview = lastMessagePreview;
        this.lastMessageAt = lastMessageAt;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getBuyerId() { return buyerId; }
    public Long getSellerId() { return sellerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getOtherPartyId() { return otherPartyId; }
    public String getOtherPartyName() { return otherPartyName; }
    public String getProductTitle() { return productTitle; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
}