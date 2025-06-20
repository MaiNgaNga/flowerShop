package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ActiclePageController {

    @RequestMapping("/Acticle")
    public String showActiclePage(Model model) {
        model.addAttribute("view", "Acticle-page");
        return "article"; // Trả về tên file trong templates
    }

    @RequestMapping("/ActicleChitiet")
    public String showActiclePage1(Model model) {
        model.addAttribute("view", "ActicleChitiet-page");
        return "ArticleChiTiec"; // Trả về tên file trong templates
    }
    // demo
    // @RequestMapping("/home")
    // public String showhomedemoPage2(Model model) {
    // model.addAttribute("view", "homeChitiet-page");
    // return "home"; // Trả về tên file trong templates
    // }
}
