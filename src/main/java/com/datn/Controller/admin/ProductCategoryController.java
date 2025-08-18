package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "tab", defaultValue = "list") String tab) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductCategory> productCategoryPage = productCategoryService.findAllPaginated(pageable);

        model.addAttribute("productCategories", productCategoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productCategoryPage.getTotalPages());
        model.addAttribute("totalElements", productCategoryPage.getTotalElements());
        model.addAttribute("hasPrevious", productCategoryPage.hasPrevious());
        model.addAttribute("hasNext", productCategoryPage.hasNext());

        model.addAttribute("productCategory", new ProductCategory()); // Khởi tạo form trống
        model.addAttribute("view", "admin/productCategoryCRUD"); // Nạp giao diện quản lý danh mục
        model.addAttribute("activeTab", tab);
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
    public String edit(Model model, @PathVariable("id") int id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "tab", defaultValue = "edit") String tab) {

        ProductCategory productCategory = productCategoryService.findByID(id); // Tìm danh mục theo ID
        
        // Nạp lại danh sách phân trang (cho tab danh sách)
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductCategory> productCategoryPage = productCategoryService.findAllPaginated(pageable);

        model.addAttribute("productCategories", productCategoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productCategoryPage.getTotalPages());
        model.addAttribute("totalElements", productCategoryPage.getTotalElements());
        model.addAttribute("hasPrevious", productCategoryPage.hasPrevious());
        model.addAttribute("hasNext", productCategoryPage.hasNext());

        model.addAttribute("productCategory", productCategory); // Đẩy dữ liệu lên form
        model.addAttribute("view", "admin/productCategoryCRUD"); // Nạp lại view
        model.addAttribute("activeTab", tab);
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
