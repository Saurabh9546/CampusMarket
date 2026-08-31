package com.campusmarket.backend.message.repository;

import com.campusmarket.backend.message.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);
    Optional<Conversation> findByBuyerIdAndProductId(Long buyerId, Long productId);
}