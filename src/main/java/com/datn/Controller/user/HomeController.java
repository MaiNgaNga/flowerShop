package com.datn.Controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.Service.CategoryService;
import com.datn.Service.ProductService;
import com.datn.Service.ProductCategoryService;
import com.datn.model.Product;
import com.datn.model.Category;
import com.datn.model.ProductCategory;

import com.datn.Service.PostService;
import com.datn.model.Post;

@Controller
public class HomeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    ProductCategoryService productCategoryService;

    @Autowired
    PostService postService;

    @GetMapping("/")
    public String home(Model model) {
        List<ProductCategory> productCategories = productCategoryService.findAll();
        List<Category> categories = categoryService.findAll();
        List<Product> productQuantities = productService.findTop6ByOrderByQuantityDesc();
        List<Product> latestProducts = productService.findLatestProductsPerCategory();
        List<Product> bestSellingProducts = productService.findBestSellingProductPerCategory();

        // Lấy toàn bộ bài viết
        List<Post> posts = postService.findAll();

        model.addAttribute("productCategories", productCategories);
        model.addAttribute("categories", categories);
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("bestSellingProducts", bestSellingProducts);
        model.addAttribute("defaultBestSeller", productService.findBestSellerByCategory("Bó hoa tươi"));
        model.addAttribute("posts", posts);
        model.addAttribute("view", "home");

        return "layouts/layout";
    }

    @GetMapping("/api/best-seller")
    @ResponseBody
    public List<Product> getBestSellerByType(@RequestParam String type) {
        switch (type.toLowerCase()) {
            case "lang":
                return productService.findBestSellerByCategory("Giỏ hoa tươi"); // Use Giỏ hoa tươi as substitute for
                                                                                // Lãng hoa
            case "gio":
                return productService.findBestSellerByCategory("Giỏ hoa tươi");
            case "bo":
                return productService.findBestSellerByCategory("Bó hoa tươi");
            default:
                return productService.findBestSellingProductPerCategory();
        }
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        List<ProductCategory> productCategories = productCategoryService.findAll();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        java.util.Set<Product> resultSet = new java.util.LinkedHashSet<>();
        if (keyword != null && !keyword.isEmpty()) {
            String keywordNoDiacritics = com.datn.utils.StringUtils.removeVietnameseDiacritics(keyword);
            resultSet.addAll(productService.searchByName(keyword, pageable).getContent());
            resultSet.addAll(productService.searchByCategoryName(keyword, pageable).getContent());
            resultSet.addAll(productService.searchByProductCategoryName(keyword, pageable).getContent());
        }
        model.addAttribute("products", resultSet);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("view", "search");
        return "layouts/layout";
    }

}
