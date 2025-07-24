package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.Service.ProductCategoryService;
import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
public class ArticlePageController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    @RequestMapping("/article")
    public String showArticlePage(Model model) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "article");
        return "layouts/layout";
    }

    @RequestMapping("/articleDetail")
    public String showArticleDetailPage(Model model) {
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "articleDetail");
        return "layouts/layout";
    }

}
