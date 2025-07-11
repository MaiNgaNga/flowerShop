package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.datn.Service.ProductCategoryService;

@Controller
public class ContactPageController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "contact");
        return "layouts/layout";
    }

}
