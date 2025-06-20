package com.datn.Controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;

@Controller
public class HomeController {
    @Autowired
    ProductCategoryService pro_ca_Service;
    @Autowired
    ProductService pro_Service;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("view", "home");
        return "layouts/layout";
    }
}
