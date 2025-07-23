package com.datn.Controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatisticalController {

    @GetMapping("/statistical")
    public String index(Model model) {
        model.addAttribute("view", "admin/statistical");
        return "admin/layout";
    }
}