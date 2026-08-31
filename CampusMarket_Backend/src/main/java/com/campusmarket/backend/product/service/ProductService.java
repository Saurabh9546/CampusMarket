package com.campusmarket.backend.product.service;

import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductCreateRequest;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.Product;
import com.campusmarket.backend.product.entity.ProductStatus;
import com.campusmarket.backend.product.mapper.ProductMapper;
import com.campusmarket.backend.product.repository.ProductRepository;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final UserRepository userRepository;

    public ProductService(ProductRepository repository, ProductMapper mapper, UserRepository userRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public Page<ProductDto> browse(Long collegeId, String category, Double minPrice,
                                     Double maxPrice, String search, Pageable pageable) {
        return repository.browse(collegeId, category, minPrice, maxPrice, search, pageable)
                .map(mapper::toDto);
    }

    /** My Listings — a seller's own products regardless of status. */
    public Page<ProductDto> getMine(Long sellerId, Pageable pageable) {
        return repository.findBySellerId(sellerId, pageable).map(mapper::toDto);
    }

    public ProductDto getById(Long productId, Long requestingCollegeId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!product.getCollegeId().equals(requestingCollegeId)) {
            throw new ResourceNotFoundException("Listing not found");
        }

        User seller = userRepository.findById(product.getSellerId()).orElse(null);
        return mapper.toDto(product, seller);
    }

    public ProductDto create(ProductCreateRequest request, Long sellerId, Long collegeId) {
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setSellerId(sellerId);
        product.setCollegeId(collegeId);
        return mapper.toDto(repository.save(product));
    }

    /** Reuses ProductCreateRequest for the update payload — same fields,
     * same validation rules, no reason to duplicate a near-identical DTO. */
    public ProductDto update(Long productId, Long requestingUserId, ProductCreateRequest request) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (!product.getSellerId().equals(requestingUserId)) {
            throw new ForbiddenException("FORBIDDEN_NOT_OWNER");
        }
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        return mapper.toDto(repository.save(product));
    }

    public void delete(Long productId, Long requestingUserId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (!product.getSellerId().equals(requestingUserId)) {
            throw new ForbiddenException("FORBIDDEN_NOT_OWNER");
        }
        repository.delete(product);
    }

    public ProductDto markSold(Long productId, Long requestingUserId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (!product.getSellerId().equals(requestingUserId)) {
            throw new ForbiddenException("FORBIDDEN_NOT_OWNER");
        }
        product.setStatus(ProductStatus.SOLD);
        return mapper.toDto(repository.save(product));
    }
}