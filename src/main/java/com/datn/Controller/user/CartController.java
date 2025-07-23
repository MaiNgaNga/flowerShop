package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.datn.Service.CartItemService;
import com.datn.Service.ProductCategoryService;
import com.datn.model.CartItem;
import com.datn.model.User;
import com.datn.utils.AuthService;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    ProductCategoryService pro_ca_Service;
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public String viewCart(Model model) {
        User user = authService.getUser();
        if (user == null)
            return "redirect:/login";
        Integer userId = user.getId();
        int cartCount = cartItemService.getCartItemsByUserId(userId).size();
        model.addAttribute("cartCount", cartCount);
        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(userId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", cartItemService.getTotalAmount(userId));
        model.addAttribute("productCategories", pro_ca_Service.findAll());

        model.addAttribute("view", "cart");
        return "layouts/layout";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity) {
        User user = authService.getUser();
        if (user == null)
            return "redirect:/login";
        Integer userId = user.getId();
        cartItemService.addCartItem(userId, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String UpdateToCart(@RequestParam("id") Integer cartItemId,
            @RequestParam("quantity") int quantity) {
        cartItemService.updateCartItemQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @GetMapping("/remove")
    public String removeCartItem(@RequestParam("cartItemId") Integer cartItemId) {
        Integer userId = authService.getUser().getId();

        if (userId != null) {
            cartItemService.removeCartItem(cartItemId);
        }
        return "redirect:/cart";
    }

    // Xóa toàn bộ giỏ hàng
    @GetMapping("/clear")
    public String clearCart() {
        Integer userId = authService.getUser().getId();

        if (userId != null) {
            cartItemService.clearCartByUserId(userId);
        }
        return "redirect:/cart";
    }
}
