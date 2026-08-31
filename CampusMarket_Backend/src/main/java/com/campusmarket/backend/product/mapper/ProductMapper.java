package com.campusmarket.backend.product.mapper;

import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    /** Existing behavior, unchanged — used by browse/list where we don't
     * want to pay for a seller-name join on every row. */
    public ProductDto toDto(Product product) {
        return toDto(product, null);
    }

    /** Used by getById (product detail) where we already have the seller loaded. */
    public ProductDto toDto(Product product, User seller) {
        return new ProductDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStatus(),
                product.getSellerId(),
                product.getCreatedAt(),
                seller != null ? seller.getName() : null
        );
    }
}