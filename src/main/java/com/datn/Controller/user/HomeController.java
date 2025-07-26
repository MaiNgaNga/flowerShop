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
import com.datn.Service.PromotionService;
import com.datn.Service.ProductCategoryService;
import com.datn.model.Product;
import com.datn.model.Comment;
import com.datn.model.Category;
import com.datn.model.ProductCategory;
import jakarta.servlet.http.HttpSession;
import com.datn.Service.PostService;
import com.datn.Service.CommentService;
import com.datn.model.Post;
import com.datn.utils.AuthService;
import com.datn.model.User;

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

    @Autowired
    PromotionService promotionservice;

    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/home")
    public String home(Model model) {
        List<ProductCategory> productCategories = productCategoryService.findAll();
        List<Category> categories = categoryService.findAll();
        List<Product> productQuantities = productService.findTop6ByOrderByQuantityDesc();
        List<Product> latestProducts = productService.findLatestProductsPerCategory();
        List<Product> bestSellingProducts = productService.findBestSellingProductPerCategory();
        List<Post> posts = postService.findAll();
        List<Product> top10PhuKien = productService.findTop10ByProductCategoryName("Phụ kiện đi kèm");

        List<Comment> comments = commentService.getTop3Comments();
        List<Product> discountProducts = productService
                .findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(0);

        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("comments", comments);
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("categories", categories);
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("promotionsCode", promotionservice.findValidPromotion());
        model.addAttribute("bestSellingProducts", bestSellingProducts);
        model.addAttribute("defaultBestSeller", productService.findBestSellerByCategory("Hoa khai trương"));
        model.addAttribute("posts", posts);
        model.addAttribute("discountProducts", discountProducts);
        model.addAttribute("top10PhuKien", top10PhuKien);
        model.addAttribute("view", "home");

        return "layouts/layout";
    }

    @GetMapping("/api/best-seller")
    @ResponseBody
    public List<Product> getBestSellerByType(@RequestParam String type) {
        switch (type.toLowerCase()) {
            case "lang":
                return productService.findBestSellerByCategory("Hoa khai trương");
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

        if (keyword != null && !keyword.isEmpty()) {
            org.springframework.data.domain.Page<Product> resultPage = org.springframework.data.domain.Page.empty();
            // Ưu tiên tìm theo danh mục (category)
            resultPage = productService.searchByCategoryName(keyword, pageable);
            // Nếu không có kết quả, thử tìm theo loại hoa (productCategory)
            if (resultPage.isEmpty()) {
                resultPage = productService.searchByProductCategoryName(keyword, pageable);
            }
            model.addAttribute("products", resultPage.getContent());
        }
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("view", "search");
        return "layouts/layout";
    }

    @GetMapping("/cart/count")
    @ResponseBody
    public int getCartItemCount(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            return cartItemService.getCartItemsByUserId(userId).size();
        }
        return 0;
    }

}
