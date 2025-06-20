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
@RequestMapping("/ProductCategory")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("productCategory", new ProductCategory());
        model.addAttribute("view", "admin/productCategoryCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model, @Valid @ModelAttribute("productCategory") ProductCategory productCategory,
            Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }
        try {
            productCategoryService.create(productCategory);
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục sản phẩm thành công!");
            return "redirect:/ProductCategory/index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") int id) {
        ProductCategory productCategory = productCategoryService.findByID(id);
        model.addAttribute("productCategory", productCategory);
        model.addAttribute("view", "admin/productCategoryCRUD");
        return "admin/layout";
    }

    @RequestMapping("/update")
    public String update(Model model, @Valid @ModelAttribute("productCategory") ProductCategory productCategory,
            Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }
        try {
            productCategoryService.update(productCategory);
            redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục sản phẩm thành công!");
            return "redirect:/ProductCategory/edit/" + productCategory.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/productCategoryCRUD");
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            productCategoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục sản phẩm!");
            return "redirect:/ProductCategory/index";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ProductCategory/index";
        }
    }
}
