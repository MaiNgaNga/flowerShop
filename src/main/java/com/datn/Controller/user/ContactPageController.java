package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactPageController {

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("view", "contact");
        return "layouts/layout";
    }
    
}
