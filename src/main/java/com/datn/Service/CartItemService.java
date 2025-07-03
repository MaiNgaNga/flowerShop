package com.datn.Service;

import com.datn.model.CartItem;
import java.util.List;

public interface CartItemService {
    
    List<CartItem> getCartItemsByUserId(Integer userId);

    CartItem addCartItem(Integer userId, Long productId, int quantity);

    CartItem updateCartItemQuantity(Integer cartItemId, int quantity);

    void removeCartItem(Integer cartItemId);

    void clearCartByUserId(Integer userId);

    double getTotalAmount(Integer userId);

}
