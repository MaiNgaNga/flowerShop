package com.datn.Controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.Product;
import com.datn.model.ProductCategory;
import java.util.List;

@Controller
@RequestMapping("/services")
public class ServicePageController {
    @Autowired
    private ProductCategoryService pro_ca_Service;

    // Tự động load productCategories cho tất cả các trang
    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return pro_ca_Service.findAll();
    }

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showServiceGift(Model model) {
        model.addAttribute("view", "service-gift");
        return "layouts/layout";
    }

    @GetMapping("/list")
    public String showServiceList(Model model,
            @RequestParam(defaultValue = "0") int page) {
        int size = 8; // số dịch vụ mỗi trang
        Pageable pageable = PageRequest.of(page, size);

        Page<Product> services = productService.findByProductCategoryName("Dịch Vụ", pageable);

        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        model.addAttribute("view", "service-list");
        return "layouts/layout";
    }

}
