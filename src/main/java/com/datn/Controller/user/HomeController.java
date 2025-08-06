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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

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
        model.addAttribute("defaultBestSeller", productService.findBestSellerByCategory("Lãng hoa tươi "));
        model.addAttribute("posts", posts);
        model.addAttribute("discountProducts", discountProducts);
        model.addAttribute("top10PhuKien", top10PhuKien);
        model.addAttribute("view", "home");

        return "layouts/layout";
    }

    /**
     * API lấy danh sách sản phẩm bán chạy theo loại (dùng cho filter Best Seller
     * trên trang chủ).
     * - Endpoint: GET /api/best-seller
     * - Tham số: type (String) - "lang", "gio", "bo" hoặc bất kỳ giá trị khác.
     * - Nếu type="lang" trả về danh sách bán chạy của "Lãng hoa tươi".
     * - Nếu type="gio" trả về danh sách bán chạy của "Giỏ hoa tươi".
     * - Nếu type="bo" trả về danh sách bán chạy của "Bó hoa tươi".
     * - Nếu không truyền hoặc truyền giá trị khác, trả về sản phẩm bán chạy nhất
     * của từng danh mục.
     * - Trả về: List<Product> (dạng JSON)
     *
     * Ví dụ request: /api/best-seller?type=lang
     * Ví dụ response: [ {"id":1, "name":"Hoa A", ...}, ... ]
     */
    @GetMapping("/api/best-seller")
    @ResponseBody
    public List<Product> getBestSellerByType(@RequestParam String type) {
        switch (type.toLowerCase()) {
            case "lang":
                return productService.findBestSellerByCategory("Lãng hoa tươi");
            case "gio":
                return productService.findBestSellerByCategory("Giỏ hoa tươi");
            case "bo":
                return productService.findBestSellerByCategory("Bó hoa tươi");
            default:
                return productService.findBestSellingProductPerCategory();
        }
    }

    /**
     * - API tìm kiếm sản phẩm.
     * - Endpoint: GET /search
     * - Tham số:
     * + keyword (String, optional): từ khóa tìm kiếm
     * + page (int, optional): trang hiện tại (mặc định 0)
     * + size (int, optional): số sản phẩm/trang (mặc định 12)
     * - Luồng tìm kiếm:
     * 1. Nếu có keyword, ưu tiên tìm theo tên danh mục (category name)
     * 2. Nếu không có kết quả, tìm theo tên loại hoa (productCategory name)
     * 3. Nếu vẫn không có kết quả, tìm theo tên sản phẩm
     * 4. Nếu không có keyword, không trả về sản phẩm nào
     * - Trả về: view layouts/layout với danh sách sản phẩm tìm được, keyword, danh
     * mục
     */
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        List<ProductCategory> productCategories = productCategoryService.findAll();
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty()) {
            Page<Product> resultPage = Page.empty();
            // Ưu tiên tìm theo danh mục (category)
            resultPage = productService.searchByCategoryName(keyword, pageable);
            // Nếu không có kết quả, thử tìm theo loại hoa (productCategory)
            if (resultPage.isEmpty()) {
                resultPage = productService.searchByProductCategoryName(keyword, pageable);
            }
            // Nếu vẫn không có kết quả, thử tìm theo tên sản phẩm
            if (resultPage.isEmpty()) {
                resultPage = productService.searchByName(keyword, pageable);
            }
            model.addAttribute("products", resultPage.getContent());
        }
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("view", "search");
        return "layouts/layout";
    }

    /**
     * API gợi ý từ khóa tìm kiếm sản phẩm (autocomplete).
     * - Endpoint: GET /api/search-suggestions
     * - Tham số: keyword (String) - từ khóa người dùng nhập
     * - Trả về: List<String> - danh sách gợi ý (tối đa 10 từ khóa liên quan)
     *
     * Ví dụ request: /api/search-suggestions?keyword=hoa
     * Ví dụ response: ["Hoa hồng", "Hoa lan", ...]
     */
    @GetMapping("/api/search-suggestions")
    @ResponseBody
    public List<String> getSearchSuggestions(@RequestParam String keyword) {
        return productService.findSearchSuggestionsByKeyword(keyword, 10);
    }

    /**
     * API lấy số lượng sản phẩm trong giỏ hàng của user hiện tại.
     * - Endpoint: GET /cart/count
     * - Lấy userId từ session, trả về số lượng sản phẩm trong giỏ hàng.
     * - Nếu chưa đăng nhập hoặc không có userId, trả về 0.
     *
     * Ví dụ response: 3
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
