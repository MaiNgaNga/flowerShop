package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import com.datn.Service.CategoryService;
import com.datn.Service.ColorService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.Category;
import com.datn.model.Color;
import com.datn.model.Product;
import com.datn.model.ProductCategory;

@Controller
@RequestMapping("/Product")
public class ProductCRUDController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ColorService colorService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductCategoryService productCategoryService;

    @ModelAttribute("products")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @ModelAttribute("colors")
    public List<Color> getAllColors() {
        return colorService.findAll();
    }

    @ModelAttribute("categories")
    public List<Category> getAllCategorys() {
        return categoryService.findAll();
    }

    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size); // Tạo Pageable từ tham số page và size
        Page<Product> productPage = productService.findByAllProduct(pageable);

        model.addAttribute("products", productPage.getContent()); // Thêm dữ liệu phân trang vào model
        model.addAttribute("currentPage", page); // Lưu trang hiện tại
        model.addAttribute("totalPages", productPage.getTotalPages()); // Lưu tổng số trang

        model.addAttribute("product", new Product());
        model.addAttribute("view", "admin/ProductCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("product") Product product, Errors errors,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/ProductCRUD");
            return "admin/layout";
        }
        try {
            productService.create(product, imageFile);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            return "redirect:/Product/index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/ProductCRUD");
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Product product = productService.findByID(id);

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.findByAllProduct(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        model.addAttribute("product", product);
        model.addAttribute("view", "admin/ProductCRUD");
        return "admin/layout";
    }

    @PostMapping("/update")
    public String update(Model model, @Valid @ModelAttribute("product") Product product, Errors errors,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "oldImage", required = false) String oldImage,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/ProductCRUD");
            return "admin/layout";
        }
        try {
            // Nếu không áp dụng giảm giá, reset về null
            if (product.getDiscountPercent() == null &&
                    product.getDiscountStart() == null &&
                    product.getDiscountEnd() == null) {
                product.setDiscountPercent(null);
                product.setDiscountStart(null);
                product.setDiscountEnd(null);
            }
            productService.update(product, imageFile, oldImage);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            return "redirect:/Product/edit/" + product.getId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/ProductCRUD");
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model,
            @ModelAttribute("product") Product product, Errors errors,
            @PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm!");
            return "redirect:/Product/index";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Product/edit/" + product.getId();

        }
    }
}
