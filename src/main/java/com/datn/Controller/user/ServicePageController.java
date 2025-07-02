package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ServicePageController {

    @RequestMapping("/services")
    public String showServicePage(Model model) {
        model.addAttribute("view", "service-page");
        return "layouts/layout";
    }
}
