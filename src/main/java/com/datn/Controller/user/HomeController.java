package com.datn.Controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.Service.CartItemService;
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

    // ============================ INJECTION CÁC SERVICE ============================

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
    private CartItemService cartItemService;

    @Autowired
    private CommentService commentService;

    // ============================ TRANG CHỦ ============================

    /**
     * Hiển thị giao diện trang chủ với các sản phẩm nổi bật, bài viết, khuyến mãi, v.v.
     */
    @GetMapping("/home")
    public String home(Model model) {
        // Lấy tất cả loại sản phẩm
        List<ProductCategory> productCategories = productCategoryService.findAll();

        // Lấy tất cả danh mục sản phẩm
        List<Category> categories = categoryService.findAll();

        // Lấy top 6 sản phẩm có số lượng tồn kho cao nhất
        List<Product> productQuantities = productService.findTop6ByOrderByQuantityDesc();

        // Lấy sản phẩm mới nhất của mỗi loại
        List<Product> latestProducts = productService.findLatestProductsPerCategory();

        // Lấy sản phẩm bán chạy nhất của mỗi loại
        List<Product> bestSellingProducts = productService.findBestSellingProductPerCategory();

        // Lấy toàn bộ bài viết
        List<Post> posts = postService.findAll();

        // Lấy top 10 sản phẩm thuộc loại "Phụ kiện đi kèm"
        List<Product> top10PhuKien = productService.findTop10ByProductCategoryName("Phụ kiện đi kèm");

        // Lấy 3 bình luận nổi bật
        List<Comment> comments = commentService.getTop3Comments();

        // Lấy 4 sản phẩm có mức giảm giá cao nhất (đang còn hàng)
        List<Product> discountProducts = productService
                .findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(0);

        // Đếm số lượng sản phẩm trong giỏ hàng nếu đã đăng nhập
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }

        // Gán các dữ liệu lấy được vào model để hiển thị ra view
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

    // ============================ API - TRẢ VỀ SẢN PHẨM BÁN CHẠY THEO LOẠI ============================

    /**
     * API lấy sản phẩm bán chạy theo loại hoa (lang/gio/bo)
     */
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

    // ============================ TÌM KIẾM SẢN PHẨM ============================

    /**
     * Tìm kiếm sản phẩm theo keyword (ưu tiên danh mục, sau đó loại hoa)
     */
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Lấy danh sách loại sản phẩm để hiển thị filter trong trang tìm kiếm
        List<ProductCategory> productCategories = productCategoryService.findAll();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty()) {
            org.springframework.data.domain.Page<Product> resultPage = org.springframework.data.domain.Page.empty();

            // Ưu tiên tìm theo tên danh mục
            resultPage = productService.searchByCategoryName(keyword, pageable);

            // Nếu không tìm thấy, thử tìm theo tên loại sản phẩm
            if (resultPage.isEmpty()) {
                resultPage = productService.searchByProductCategoryName(keyword, pageable);
            }

            // Gán kết quả vào model
            model.addAttribute("products", resultPage.getContent());
        }

        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("view", "search");

        return "layouts/layout";
    }

    // ============================ API - ĐẾM SỐ LƯỢNG SẢN PHẨM TRONG GIỎ HÀNG ============================

    /**
     * Trả về số lượng sản phẩm trong giỏ hàng hiện tại (dành cho AJAX client)
     */
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
