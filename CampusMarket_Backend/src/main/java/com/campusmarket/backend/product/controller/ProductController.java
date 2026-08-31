package com.campusmarket.backend.product.controller;

import com.campusmarket.backend.common.ApiResponse;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductCreateRequest;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.service.ProductService;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<Page<ProductDto>> browse(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page) {
        Long collegeId = currentUser().getCollege().getId();
        Pageable pageable = PageRequest.of(page, 20);
        return ApiResponse.success(productService.browse(collegeId, category, minPrice, maxPrice, search, pageable));
    }

    @GetMapping("/mine")
    public ApiResponse<Page<ProductDto>> mine(@RequestParam(defaultValue = "0") int page) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(page, 20);
        return ApiResponse.success(productService.getMine(userId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getById(@PathVariable Long id) {
        Long collegeId = currentUser().getCollege().getId();
        return ApiResponse.success(productService.getById(id, collegeId));
    }

    @PostMapping
    public ApiResponse<ProductDto> create(@Valid @RequestBody ProductCreateRequest request) {
        User user = currentUser();
        ProductDto created = productService.create(request, user.getId(), user.getCollege().getId());
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductCreateRequest request) {
        Long userId = currentUserId();
        return ApiResponse.success(productService.update(id, userId, request));
    }

    @PatchMapping("/{id}/sold")
    public ApiResponse<ProductDto> markSold(@PathVariable Long id) {
        Long userId = currentUserId();
        return ApiResponse.success(productService.markSold(id, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = currentUserId();
        productService.delete(id, userId);
        return ApiResponse.success(null);
    }

    private Long currentUserId() {
        return Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private User currentUser() {
        Long userId = currentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}