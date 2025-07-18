package com.datn.Service;

import java.util.List;

import com.datn.model.CartItem;

public interface CartItemService {
    
    List<CartItem> getCartItemsByUserId(Integer userId);

    CartItem addCartItem(Integer userId, Long productId, int quantity);

    CartItem updateCartItemQuantity(Integer cartItemId, int quantity);

    void removeCartItem(Integer cartItemId);

    void clearCartByUserId(Integer userId);

    double getTotalAmount(Integer userId);

}
