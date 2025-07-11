package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.Service.ProductCategoryService;

@Controller
public class ArticlePageController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @RequestMapping("/article")
    public String showArticlePage(Model model) {
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "article");
        return "layouts/layout";
    }

    @RequestMapping("/articleDetail")
    public String showArticleDetailPage(Model model) {
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "articleDetail");
        return "layouts/layout";
    }

}
