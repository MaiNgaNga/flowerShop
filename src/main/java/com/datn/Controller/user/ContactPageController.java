package com.datn.Controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.datn.Service.ProductCategoryService;
import com.datn.model.ProductCategory;

@Controller
public class ContactPageController {
    @Autowired
    private ProductCategoryService pro_ca_Service;

    // Tự động load productCategories cho tất cả các trang
    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return pro_ca_Service.findAll();
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("view", "contact");
        return "layouts/layout";
    }
    
}
