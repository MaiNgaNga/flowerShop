package com.datn.Controller.user;

import com.datn.Service.ProductCategoryService;
import com.datn.Service.ServiceRequestService;

import com.datn.Service.ServiceService;
import com.datn.model.ProductCategory;
import com.datn.model.ServiceEntity;
import com.datn.model.ServiceRequest;
import com.datn.model.User;
import com.datn.utils.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    ProductCategoryService productCategoryService;

    // ------------------- HIỂN THỊ TRANG DỊCH VỤ -------------------
    @GetMapping
    public String index(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size,
            @RequestParam(value = "success", required = false) String success) {
        // Phân trang dịch vụ
        Page<ServiceEntity> servicePage = serviceService.findAvailableServices(PageRequest.of(page, size));
        List<ServiceEntity> activeServices = serviceService.findAllAvailable();
        List<ProductCategory> productCategories = productCategoryService.findAll();
        // Tạo ServiceRequest và điền sẵn thông tin người dùng
        ServiceRequest request = new ServiceRequest();
        User user = authService.getUser();
        if (user != null) {
            request.setFullName(user.getName());
            request.setEmail(user.getEmail());
            request.setPhone(user.getSdt());
        }   

        model.addAttribute("productCategories", productCategories);
        model.addAttribute("services", servicePage.getContent());
        model.addAttribute("activeServices", activeServices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicePage.getTotalPages());
        model.addAttribute("serviceRequest", request);
        model.addAttribute("view", "service");

        return "layouts/layout";
    }

    // ------------------- GỬI YÊU CẦU DỊCH VỤ -------------------
    @PostMapping("/contact")
    public String submitRequest(
            @Valid @ModelAttribute("serviceRequest") ServiceRequest request,
            BindingResult result,
            Model model) {
        // Kiểm tra đăng nhập
        if (!authService.isAuthenticated()) {
            return "redirect:/login?redirect=/services&loginRequired=true";
        }

        // Kiểm tra lỗi form
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

        // Gán thông tin người dùng + thời gian
        User currentUser = authService.getCurrentUser();
        request.setUser(currentUser);
        request.setCreatedAt(LocalDateTime.now());
        serviceRequestService.save(request);

        return "redirect:/services?success";
    }
}
