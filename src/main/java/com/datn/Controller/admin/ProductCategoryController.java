package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import com.datn.Service.ProductCategoryService;
import com.datn.model.ProductCategory;

@Controller
@RequestMapping("/ProductCategory") // Tất cả các request mapping bắt đầu bằng /ProductCategory
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    // Trước khi load view, thêm danh sách tất cả danh mục sản phẩm vào model
    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryService.findAll();
    }

    // Hiển thị trang quản lý danh mục (index)
    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("productCategory", new ProductCategory()); // Khởi tạo form trống
        model.addAttribute("view", "admin/productCategoryCRUD"); // Nạp giao diện quản lý danh mục
        return "admin/layout";
    }

    // Xử lý thêm mới danh mục sản phẩm
    @PostMapping("/create")
    public String create(Model model, 
        @Valid @ModelAttribute("productCategory") ProductCategory productCategory, // Validate dữ liệu form
        Errors errors, 
        RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            // Nếu dữ liệu không hợp lệ, quay lại form
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }

        try {
            // Gọi service để thêm mới danh mục
            productCategoryService.create(productCategory);
            // Gửi thông báo thành công sau redirect
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục sản phẩm thành công!");
            return "redirect:/ProductCategory/index";
        } catch (IllegalArgumentException e) {
            // Nếu có lỗi từ service (ví dụ tên bị trùng), hiển thị lại lỗi trên view
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }
    }

    // Truy cập vào form chỉnh sửa danh mục theo ID
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") int id) {
        ProductCategory productCategory = productCategoryService.findByID(id); // Tìm danh mục theo ID
        model.addAttribute("productCategory", productCategory); // Đẩy dữ liệu lên form
        model.addAttribute("view", "admin/productCategoryCRUD"); // Nạp lại view
        return "admin/layout";
    }

    // Xử lý cập nhật danh mục sản phẩm
    @RequestMapping("/update")
    public String update(Model model, 
        @Valid @ModelAttribute("productCategory") ProductCategory productCategory,
        Errors errors, 
        RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            // Trả về form nếu có lỗi validate
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }

        try {
            // Cập nhật dữ liệu danh mục
            productCategoryService.update(productCategory);
            // Thêm thông báo và redirect về lại trang edit (hiển thị lại dữ liệu đã cập nhật)
            redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục sản phẩm thành công!");
            return "redirect:/ProductCategory/edit/" + productCategory.getId();
        } catch (IllegalArgumentException e) {
            // Nếu có lỗi (ví dụ tên trùng), hiển thị lại lỗi
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }
    }

    // Xử lý xóa danh mục sản phẩm theo ID
    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            productCategoryService.deleteById(id); // Gọi service xóa
            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục sản phẩm!");
            return "redirect:/ProductCategory/index";
        } catch (IllegalArgumentException e) {
            // Nếu có lỗi khi xóa (ví dụ đang được sử dụng), hiển thị lỗi
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ProductCategory/index";
        }
    }
}
