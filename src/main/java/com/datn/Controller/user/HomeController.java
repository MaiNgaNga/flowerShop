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

@Controller
public class HomeController {
     @Autowired
    ProductCategoryService pro_ca_Service;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    ProductCategoryService productCategoryService;

    @GetMapping("/home")
    public String home(Model model) {
        // Load data và kiểm tra
        List<Product> productQuantities = productService.findTop6ByOrderByQuantityDesc();
        List<Product> latestProducts = productService.findLatestProductsPerCategory();
        List<Product> bestSellingProducts = productService.findBestSellingProductPerCategory();
        model.addAttribute("productCategories", pro_ca_Service.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("bestSellingProducts", bestSellingProducts);
        // Load default best seller (Bó hoa tươi) for initial display
        model.addAttribute("defaultBestSeller", productService.findBestSellerByCategory("Bó hoa tươi"));
        model.addAttribute("view", "home");

        return "layouts/layout";
    }

    // API endpoint để lọc sản phẩm best seller theo loại
    @GetMapping("/api/best-seller")
    @ResponseBody
    public List<Product> getBestSellerByType(@RequestParam String type) {
        switch (type.toLowerCase()) {
            case "lang":
                return productService.findBestSellerByCategory("Giỏ hoa tươi"); // Use Giỏ hoa tươi as substitute for Lãng hoa
            case "gio":
                return productService.findBestSellerByCategory("Giỏ hoa tươi");
            case "bo":
                return productService.findBestSellerByCategory("Bó hoa tươi");
            default:
                return productService.findBestSellingProductPerCategory();
        }
    }

    // Debug endpoints
    @GetMapping("/api/debug/categories")
    @ResponseBody
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/api/debug/products")
    @ResponseBody
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/api/debug/products-with-categories")
    @ResponseBody
    public List<Product> getProductsWithCategories() {
        List<Product> products = productService.findAll();
        for (Product product : products) {
            System.out.println("Product: " + product.getName() + 
                ", Category: " + (product.getCategory() != null ? product.getCategory().getName() : "NULL") +
                ", ProductCategory: " + (product.getProductCategory() != null ? product.getProductCategory().getName() : "NULL"));
        }
        return products;
    }

    @GetMapping("/api/debug/product-categories")
    @ResponseBody
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryService.findAll();
    }
}
