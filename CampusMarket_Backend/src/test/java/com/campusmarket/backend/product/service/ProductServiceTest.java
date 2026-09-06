package com.campusmarket.backend.product.service;

import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductCreateRequest;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.product.entity.ProductStatus;
import com.campusmarket.backend.product.mapper.ProductMapper;
import com.campusmarket.backend.product.repository.ProductRepository;
import com.campusmarket.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository repository;
    @Mock private ProductMapper mapper;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = new Product();
        existingProduct.setId(100L);
        existingProduct.setSellerId(1L);
        existingProduct.setCollegeId(5L);
        existingProduct.setTitle("Old Title");
    }

    @Test
    void delete_removesProduct_whenRequesterIsOwner() {
        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));

        productService.delete(100L, 1L);

        verify(repository).delete(existingProduct);
    }

    @Test
    void delete_throwsForbidden_whenRequesterIsNotOwner() {
        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.delete(100L, 999L))
                .isInstanceOf(ForbiddenException.class);

        verify(repository, never()).delete(any());
    }

    @Test
    void delete_throwsResourceNotFound_whenProductDoesNotExist() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markSold_setsStatusToSold_whenRequesterIsOwner() {
        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));
        when(repository.save(existingProduct)).thenReturn(existingProduct);
        when(mapper.toDto(existingProduct)).thenReturn(new ProductDto(
                1L, "Title", "Desc", 50.0, "Category", ProductStatus.SOLD, 1L, LocalDateTime.now(), "Seller"));

        productService.markSold(100L, 1L);

        assertThat(existingProduct.getStatus()).isEqualTo(ProductStatus.SOLD);
        verify(repository).save(existingProduct);
    }

    @Test
    void markSold_throwsForbidden_whenRequesterIsNotOwner() {
        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.markSold(100L, 999L))
                .isInstanceOf(ForbiddenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_modifiesFields_whenRequesterIsOwner() {
        ProductCreateRequest updateRequest = new ProductCreateRequest();
        updateRequest.setTitle("New Title");
        updateRequest.setDescription("New Description");
        updateRequest.setPrice(999.0);
        updateRequest.setCategory("Books");

        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));
        when(repository.save(existingProduct)).thenReturn(existingProduct);
        when(mapper.toDto(existingProduct)).thenReturn(new ProductDto(
                1L, "New Title", "New Description", 999.0, "Books", ProductStatus.AVAILABLE, 1L, LocalDateTime.now(), "Seller"));

        productService.update(100L, 1L, updateRequest);

        assertThat(existingProduct.getTitle()).isEqualTo("New Title");
        verify(repository).save(existingProduct);
    }

    @Test
    void update_throwsForbidden_whenRequesterIsNotOwner() {
        ProductCreateRequest updateRequest = new ProductCreateRequest();
        updateRequest.setTitle("Malicious Edit");

        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.update(100L, 999L, updateRequest))
                .isInstanceOf(ForbiddenException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void getById_returnsProduct_whenCollegeIdMatches() {
        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));
        when(userRepository.findById(existingProduct.getSellerId())).thenReturn(Optional.empty());
        when(mapper.toDto(existingProduct, null)).thenReturn(new ProductDto(
                100L, "Old Title", "Desc", 50.0, "Category", ProductStatus.AVAILABLE, 1L, LocalDateTime.now(), "Seller"));

        ProductDto result = productService.getById(100L, 5L);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_throwsResourceNotFound_whenCollegeIdDoesNotMatch() {
        when(repository.findById(100L)).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.getById(100L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesProductWithCorrectSellerAndCollege() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setTitle("New Item");
        request.setDescription("Description");
        request.setPrice(50.0);
        request.setCategory("Electronics");

        Product savedProduct = new Product();
        savedProduct.setSellerId(1L);
        savedProduct.setCollegeId(5L);

        when(repository.save(any(Product.class))).thenReturn(savedProduct);
        when(mapper.toDto(savedProduct)).thenReturn(new ProductDto(
                1L, "New Item", "Description", 50.0, "Electronics", ProductStatus.AVAILABLE, 1L, LocalDateTime.now(), "Seller"));

        productService.create(request, 1L, 5L);

        verify(repository).save(argThat(p ->
                p.getSellerId().equals(1L) && p.getCollegeId().equals(5L)
        ));
    }
}