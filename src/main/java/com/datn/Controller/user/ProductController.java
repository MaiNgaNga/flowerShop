package com.datn.Controller.user;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.CategoryService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.Product;

@Controller
public class ProductController {
    @Autowired
    private ProductCategoryService pro_ca_service;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService ca_Service;

    // Tự động load productCategories cho tất cả các trang
    @ModelAttribute("productCategories")
    public List<com.datn.model.ProductCategory> getAllProductCategories() {
        return pro_ca_service.findAll();
    }

    @RequestMapping("/ProductUser")
    public String index(Model model,
        @RequestParam("id") Integer pro_categoryId, 
        @RequestParam(name = "categoryId", required = false) Integer ca_Id,
        @RequestParam(name = "color", required = false) String color,
        @RequestParam(name = "min", required = false) Double minPrice,
        @RequestParam(name = "max", required = false) Double maxPrice,
        @RequestParam("p") Optional<Integer> p,
        @RequestParam(name = "filter", required = false) String filterType 
    ) { 
        Pageable pageable = PageRequest.of(p.orElse(0), 12);
        Page<Product> products = null;

        // Xác định kiểu lọc
        if ("price".equals(filterType) && minPrice != null && maxPrice != null) {
            products = productService.findByPriceRange(pro_categoryId, minPrice, maxPrice, pageable);
        } else if ("color".equals(filterType) && color != null) {
            products = productService.findByColor(pro_categoryId, color, pageable);
        } else if ("ca_id".equals(filterType) && ca_Id != null) {
            products = productService.findByCaId(pro_categoryId, ca_Id, pageable);
        } else {
            products = productService.findByProductCategoryIdPage(pro_categoryId, pageable);
        }

        model.addAttribute("page", products);
        model.addAttribute("pro_ca", pro_ca_service.findByID(pro_categoryId));
        model.addAttribute("categogies", ca_Service.findAll());
        model.addAttribute("view", "product");

        return "layouts/layout";

    }

    @RequestMapping("/search")
    public String searchProductByName(Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "p", defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, 12);
            Page<Product> result;

            if (keyword == null || keyword.trim().isEmpty()) {
                // Nếu không nhập từ khóa, chuyển hướng về trang sản phẩm
                return "redirect:/products";
            } else {
                result = productService.searchByName(keyword.trim(), pageable);
            }

            model.addAttribute("page", result);
            model.addAttribute("categogies", ca_Service.findAll());
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("pro_ca", null);
            model.addAttribute("view", "product");

            // Thêm thông báo kết quả tìm kiếm
            if (result.getTotalElements() == 0) {
                model.addAttribute("searchMessage",
                        "Không tìm thấy sản phẩm nào phù hợp với từ khóa: '" + keyword + "'");
            } else {
                model.addAttribute("searchMessage",
                        "Tìm thấy " + result.getTotalElements() + " sản phẩm cho từ khóa: '" + keyword + "'");
            }

            return "layouts/layout";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.");
            return "layouts/layout";
        }
    }
}
