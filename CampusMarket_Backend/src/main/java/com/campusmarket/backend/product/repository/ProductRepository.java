package com.campusmarket.backend.product.repository;

import com.campusmarket.backend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.collegeId = :collegeId " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> browse(@Param("collegeId") Long collegeId, @Param("category") String category,
                          @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
                          @Param("search") String search, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
}