package com.campusmarket.backend.wishlist.controller;

import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.wishlist.service.WishlistService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ApiResponse<List<ProductDto>> list() {
        return ApiResponse.success(wishlistService.list(currentUserId()));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Void> add(@PathVariable Long productId) {
        wishlistService.add(currentUserId(), productId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@PathVariable Long productId) {
        wishlistService.remove(currentUserId(), productId);
        return ApiResponse.success(null);
    }

    private Long currentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}