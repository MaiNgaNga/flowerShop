package com.datn.Controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.Service.ProductCategoryService;
import com.datn.model.ProductCategory;

@Controller
public class ServicePageController {
    @Autowired
    private ProductCategoryService pro_ca_Service;

    // Tự động load productCategories cho tất cả các trang
    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return pro_ca_Service.findAll();
    }

    @RequestMapping("/services")
    public String showServicePage(Model model) {
        model.addAttribute("view", "service-page");
        return "layouts/layout"; // Trả về tên file trong templates (service-page.html)
    }
}
