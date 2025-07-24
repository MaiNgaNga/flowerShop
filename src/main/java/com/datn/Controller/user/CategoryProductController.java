package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.Service.ProductCategoryService;
import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
public class CategoryProductController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    @RequestMapping("/products")
    public String showServicePage(Model model) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId(); // Sửa lại nếu getter id khác
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "productCategory");
        return "layouts/layout"; // Trả về tên file trong templates (service-page.html)
    }

}
