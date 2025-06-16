package com.datn.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import poly.edu.Assignment.Service.CartItemService;
import poly.edu.Assignment.dao.CartItemDAO;
import poly.edu.Assignment.dao.ProductDAO;
import poly.edu.Assignment.dao.UserDAO;
import poly.edu.Assignment.model.CartItem;
import poly.edu.Assignment.model.Product;
import poly.edu.Assignment.model.User;


import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemDAO cartItemDAO;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private ProductDAO productDAO;

    @Override
    public List<CartItem> getCartItemsByUserId(Integer userId) {
        return cartItemDAO.findByUserId(userId);
    }

    @Override
    public CartItem addCartItem(Integer userId, Long productId, int quantity) {
        Optional<User> userOpt = userDao.findById(userId);
        Optional<Product> productOpt = productDAO.findById(productId);

        if (userOpt.isPresent() && productOpt.isPresent()) {
            Product product = productOpt.get();
            CartItem cartItem = cartItemDAO.findByUserIdAndProductId(userId, productId)
                    .orElse(CartItem.builder()
                            .user(userOpt.get())
                            .product(product)
                            .quantity(0)
                            .price(product.getPrice())
                            .build());

            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartItemDAO.save(cartItem);
        }
        throw new IllegalArgumentException("User hoặc Product không tồn tại!");
    }

    @Override
    public CartItem updateCartItemQuantity(Integer cartItemId, int quantity) {
        CartItem cartItem = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("CartItem không tồn tại!"));

        if (quantity > 0) {
            cartItem.setQuantity(quantity);
            return cartItemDAO.save(cartItem);
        } else {
            cartItemDAO.delete(cartItem);
            return null;
        }
    }

    @Override
    public void removeCartItem(Integer cartItemId) {
        cartItemDAO.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCartByUserId(Integer userId) {
        cartItemDAO.deleteByUserId(userId);
    }

    @Override
    public double getTotalAmount(Integer userId) {
        List<CartItem> cartItems = getCartItemsByUserId(userId);
        double totalAmount = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        return totalAmount;
    }

   
    
}
