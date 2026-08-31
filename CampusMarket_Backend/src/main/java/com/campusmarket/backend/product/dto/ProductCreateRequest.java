package com.campusmarket.backend.product.dto;

import jakarta.validation.constraints.*;

public class ProductCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title too long")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotBlank(message = "Category is required")
    private String category;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}