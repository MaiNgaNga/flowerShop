package com.datn.Controller.user;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.CategoryService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.Product;
import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
public class ProductController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService ca_Service;

    @Autowired
    private AuthService authService;

    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    // Xử lý hiển thị sản phẩm theo danh mục cho người dùng
    @RequestMapping("/ProductUser")
    public String index(Model model,
            @RequestParam("id") Integer pro_categoryId, // ID của danh mục sản phẩm chính
            @RequestParam(name = "categoryId", required = false) Integer ca_Id, // ID danh mục con (nếu có)
            @RequestParam(name = "color", required = false) String color, // Màu lọc
            @RequestParam(name = "min", required = false) Double minPrice, // Giá tối thiểu
            @RequestParam(name = "max", required = false) Double maxPrice, // Giá tối đa
            @RequestParam("p") Optional<Integer> p) // Trang hiện tại
            { 

        int cartCount = 0;

        // Lấy user đang đăng nhập (nếu có) để lấy số lượng sản phẩm trong giỏ hàng
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId(); // Lấy ID người dùng
            cartCount = cartItemService.getCartItemsByUserId(userId).size(); // Đếm số sản phẩm trong giỏ
        }

        model.addAttribute("cartCount", cartCount);

        // Khởi tạo phân trang với 12 sản phẩm mỗi trang
        Pageable pageable = PageRequest.of(p.orElse(0), 12);
         // Nếu chuỗi rỗng => gán null
    if (color != null && color.trim().isEmpty()) {
        color = null;
    }
    if (ca_Id != null && ca_Id == 0) { // hoặc điều kiện bạn dùng
        ca_Id = null;
    }

    // giá mặc định nếu cần
    if (minPrice == null) minPrice = 0.0;
    if (maxPrice == null) maxPrice = Double.MAX_VALUE;
        Page<Product> products = productService.findByMultipleFilters(
            pro_categoryId, ca_Id, color, minPrice, maxPrice, pageable);

        model.addAttribute("selectedCategoryId", ca_Id);
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedMin", minPrice);
        model.addAttribute("selectedMax", maxPrice);
        // Truyền các dữ liệu cần thiết sang view
        model.addAttribute("page", products); // Danh sách sản phẩm theo trang
        model.addAttribute("pro_ca", productCategoryService.findByID(pro_categoryId)); // Thông tin danh mục chính
        model.addAttribute("bestsellerProduct", productService.findBestSeller()); // Danh sách sản phẩm bán chạy
        model.addAttribute("productCategories", productCategoryService.findAll()); // Tất cả danh mục sản phẩm chính
        model.addAttribute("categogies", productService.findCategoriesByProductCategoryId(pro_categoryId)); // Tất cả danh mục con
        model.addAttribute("view", "product"); // Tên view được load bên layout

        return "layouts/layout"; // Trả về layout chính có nhúng view "product"
    }

   
}
