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
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "productCategoryId", required = false) String productCategoryIdStr,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        // Nếu có từ khóa tìm kiếm
        if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        }
        // Nếu có chọn danh mục cụ thể
        else if (productCategoryIdStr != null && !productCategoryIdStr.isEmpty()) {
            Integer productCategoryId = Integer.parseInt(productCategoryIdStr);
            productPage = productService.findByProductCategoryIdPage(productCategoryId, pageable);
            model.addAttribute("productCategoryId", productCategoryId);
        }
        // Nếu không chọn gì → mặc định lọc theo danh mục mới nhất
        else {
            List<ProductCategory> categories = productCategoryService.findAll();
            if (!categories.isEmpty()) {
                // Lấy danh mục có ID lớn nhất (mới nhất)
                ProductCategory newestCategory = categories.stream()
                        .max((a, b) -> Integer.compare(a.getId(), b.getId()))
                        .orElse(null);

                if (newestCategory != null) {
                    int newestId = newestCategory.getId();
                    productPage = productService.findByProductCategoryIdPage(newestId, pageable);
                    model.addAttribute("productCategoryId", newestId);
                } else {
                    productPage = productService.findByAllProduct(pageable);
                }
            } else {
                productPage = productService.findByAllProduct(pageable);
            }
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("product", new Product());
        model.addAttribute("view", "admin/ProductCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("product") Product product, Errors errors,
            @RequestParam("image1") MultipartFile image1,
            @RequestParam("image2") MultipartFile image2,
            @RequestParam("image3") MultipartFile image3,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/ProductCRUD");
            return "admin/layout";
        }
        try {
            productService.create(product, image1, image2, image3);
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
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3,
            @RequestParam(value = "oldImages", required = false) String[] oldImages,
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
            productService.update(product, image1, image2, image3, oldImages);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            return "redirect:/Product/index";

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
