package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.Product;  
import com.datn.utils.AuthService;
import com.datn.model.User;
import com.datn.Service.ServiceRequestService;
import com.datn.Service.ServiceService;
import com.datn.model.ServiceEntity;
import com.datn.model.ServiceRequest;
import com.datn.Service.CartItemService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/services")

public class ServicePageController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductService productService;



    @GetMapping
    public String index(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size) {

        // Lấy danh sách dịch vụ đang hoạt động theo trang
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId(); // Sửa lại nếu getter id khác
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        Page<ServiceEntity> servicePage = serviceService.findAvailableServices(PageRequest.of(page, size));

        // Lấy tất cả dịch vụ đang hoạt động để hiển thị trong dropdown form
        List<ServiceEntity> activeServices = serviceService.findAllAvailable();

        model.addAttribute("services", servicePage.getContent());
        model.addAttribute("activeServices", activeServices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicePage.getTotalPages());
        model.addAttribute("serviceRequest", new ServiceRequest());
        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("view", "service");
        return "layouts/layout";
    }

    // Xử lý form gửi yêu cầu dịch vụ
    @PostMapping("/contact")
    public String submitRequest(@Valid @ModelAttribute("serviceRequest") ServiceRequest request,
            BindingResult result,
            Model model) {

        // Nếu chưa đăng nhập thì chuyển hướng về trang login hoặc hiển thị lỗi
        if (!authService.isAuthenticated()) {
            return "redirect:/login?redirect=/services&loginRequired=true";
        }

        if (result.hasErrors()) {
            Page<ServiceEntity> servicePage = serviceService.findAvailableServices(PageRequest.of(0, 3));
            List<ServiceEntity> activeServices = serviceService.findAllAvailable();

            model.addAttribute("services", servicePage.getContent());
            model.addAttribute("activeServices", activeServices);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", servicePage.getTotalPages());
            model.addAttribute("view", "service");
            return "layouts/layout";
        }

        User currentUser = authService.getCurrentUser();
        request.setUser(currentUser);
        request.setCreatedAt(LocalDateTime.now());
        serviceRequestService.save(request);

        return "redirect:/services?success";
    }

}
