package com.campusmarket.backend.product.dto;

import com.campusmarket.backend.product.entity.ProductStatus;
import java.time.LocalDateTime;

public class ProductDto {
    private final Long id;
    private final String title;
    private final String description;
    private final Double price;
    private final String category;
    private final ProductStatus status;
    private final Long sellerId;
    private final LocalDateTime createdAt;
    /** Nullable — only populated for single-product detail fetch (getById).
     * Browse/list results leave this null to avoid an extra join per row
     * on every listing in a paginated grid. */
    private final String sellerName;

    public ProductDto(Long id, String title, String description, Double price,
                       String category, ProductStatus status, Long sellerId,
                       LocalDateTime createdAt, String sellerName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.sellerId = sellerId;
        this.createdAt = createdAt;
        this.sellerName = sellerName;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public String getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public Long getSellerId() { return sellerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getSellerName() { return sellerName; }
}