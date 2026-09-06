package com.campusmarket.backend.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.campusmarket.backend.wishlist.entity.Wishlist;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
