package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class AboutController {
    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("view", "about");
        return "layouts/layout";
    }
}
