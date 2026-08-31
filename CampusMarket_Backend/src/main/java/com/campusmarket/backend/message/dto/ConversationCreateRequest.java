package com.campusmarket.backend.message.dto;

import jakarta.validation.constraints.NotNull;

public class ConversationCreateRequest {
    @NotNull(message = "Product id is required")
    private Long productId;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}