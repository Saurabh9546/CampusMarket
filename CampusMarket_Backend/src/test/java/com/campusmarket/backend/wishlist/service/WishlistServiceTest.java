package com.campusmarket.backend.wishlist.service;

import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.product.entity.ProductStatus;
import com.campusmarket.backend.product.mapper.ProductMapper;
import com.campusmarket.backend.product.repository.ProductRepository;
import com.campusmarket.backend.wishlist.entity.Wishlist;
import com.campusmarket.backend.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private WishlistService wishlistService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = new Product();
        existingProduct.setId(200L);
        existingProduct.setSellerId(1L);
        existingProduct.setCollegeId(5L);
    }

    @Test
    void add_savesEntry_whenProductExistsAndNotAlreadyWishlisted() {
        when(productRepository.findById(200L)).thenReturn(Optional.of(existingProduct));
        when(wishlistRepository.existsByUserIdAndProductId(10L, 200L)).thenReturn(false);

        wishlistService.add(10L, 200L);

        verify(wishlistRepository).save(argThat(entry ->
                entry.getUserId().equals(10L) && entry.getProductId().equals(200L)
        ));
    }

    @Test
    void add_throwsConflict_whenAlreadyWishlisted() {
        when(productRepository.findById(200L)).thenReturn(Optional.of(existingProduct));
        when(wishlistRepository.existsByUserIdAndProductId(10L, 200L)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.add(10L, 200L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Already wishlisted");

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void add_throwsResourceNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.add(10L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void remove_delegatesToRepository() {
        wishlistService.remove(10L, 200L);

        verify(wishlistRepository).deleteByUserIdAndProductId(10L, 200L);
    }

    @Test
    void list_returnsMappedProducts_forGivenUser() {
        Wishlist entry = new Wishlist();
        entry.setUserId(10L);
        entry.setProductId(200L);

        ProductDto dto = new ProductDto(
                200L, "Title", "Desc", 50.0, "Category", ProductStatus.AVAILABLE, 1L, LocalDateTime.now(), "Seller");

        when(wishlistRepository.findByUserId(10L)).thenReturn(List.of(entry));
        when(productRepository.findAllById(List.of(200L))).thenReturn(List.of(existingProduct));
        when(productMapper.toDto(existingProduct)).thenReturn(dto);

        List<ProductDto> result = wishlistService.list(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Title");
    }

    @Test
    void list_returnsEmptyList_whenUserHasNoWishlistedItems() {
        when(wishlistRepository.findByUserId(10L)).thenReturn(List.of());
        when(productRepository.findAllById(List.of())).thenReturn(List.of());

        List<ProductDto> result = wishlistService.list(10L);

        assertThat(result).isEmpty();
    }
}