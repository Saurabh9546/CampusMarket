package com.campusmarket.backend.product.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "college_id", nullable = false)
    private Long collegeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}