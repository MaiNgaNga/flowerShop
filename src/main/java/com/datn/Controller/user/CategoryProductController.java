package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CategoryProductController {
     @RequestMapping("/products")
    public String showServicePage(Model model) {
        model.addAttribute("view", "productCategory");
        return "layouts/layout"; // Trả về tên file trong templates (service-page.html)
    }
    
}
