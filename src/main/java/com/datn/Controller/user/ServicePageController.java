package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/services")
public class ServicePageController {
    @GetMapping
    public String showServiceGift(Model model) {
        model.addAttribute("view", "service");
        return "layouts/layout";
    }

}
