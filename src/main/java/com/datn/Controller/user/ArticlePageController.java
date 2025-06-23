package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ArticlePageController {

    @RequestMapping("/article")
    public String showArticlePage(Model model) {
        model.addAttribute("view", "article");
        return "layouts/layout";
    }

    @RequestMapping("/articleDetail")
    public String showArticleDetailPage(Model model) {
        model.addAttribute("view", "articleDetail");
        return "layouts/layout";
    }

}
