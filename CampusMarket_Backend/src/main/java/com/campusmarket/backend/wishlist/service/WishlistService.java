package com.campusmarket.backend.wishlist.service;

import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.product.mapper.ProductMapper;
import com.campusmarket.backend.product.repository.ProductRepository;
import com.campusmarket.backend.wishlist.entity.Wishlist;
import com.campusmarket.backend.wishlist.repository.WishlistRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository,
                            ProductMapper productMapper) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductDto> list(Long userId) {
        List<Long> productIds = wishlistRepository.findByUserId(userId)
                .stream()
                .map(Wishlist::getProductId)
                .toList();
        return productRepository.findAllById(productIds)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    public void add(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ConflictException("Already wishlisted", "DUPLICATE_WISHLIST_ENTRY");
        }

        Wishlist entry = new Wishlist();
        entry.setUserId(userId);
        entry.setProductId(product.getId());
        wishlistRepository.save(entry);
    }

    public void remove(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
