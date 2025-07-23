package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.Product;
import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
@RequestMapping("/services")
public class ServicePageController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    @GetMapping
    public String showServiceGift(Model model) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId(); // Sửa lại nếu getter id khác
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "service-gift");
        return "layouts/layout";
    }

    @GetMapping("/list")
    public String showServiceList(Model model,
            @RequestParam(defaultValue = "0") int page) {
        int size = 8; // số dịch vụ mỗi trang
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> services = productService.findByProductCategoryName("Dịch Vụ", pageable);
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        model.addAttribute("view", "service-list");
        return "layouts/layout";
    }

}
