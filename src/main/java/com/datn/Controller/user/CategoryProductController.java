package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.Service.ProductCategoryService;

@Controller
public class CategoryProductController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @RequestMapping("/products")
    public String showServicePage(Model model) {
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "productCategory");
        return "layouts/layout"; // Trả về tên file trong templates (service-page.html)
    }

}
