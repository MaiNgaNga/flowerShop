package com.datn.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.CartItem;


public interface CartItemDAO extends JpaRepository <CartItem, Integer> {
    
    List<CartItem> findByUserId(Integer userId);

    Optional<CartItem> findByUserIdAndProductId(Integer userId, Long productId);

    void deleteByUserId(Integer userId);


}
