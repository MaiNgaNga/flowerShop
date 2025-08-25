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
import com.datn.validation.DiscountValidator;

@Controller
@RequestMapping("/Product")
public class ProductCRUDController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private DiscountValidator discountValidator;

    // Cung cấp danh sách tất cả sản phẩm để sử dụng trong view
    @ModelAttribute("products")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    // Cung cấp danh sách màu sắc
    @ModelAttribute("colors")
    public List<Color> getAllColors() {
        return colorService.findAll();
    }

    // Cung cấp danh sách loại sản phẩm
    @ModelAttribute("categories")
    public List<Category> getAllCategorys() {
        return categoryService.findAll();
    }

    // Cung cấp danh sách danh mục sản phẩm
    @ModelAttribute("productCategories")
    public List<ProductCategory> getAllProductCategories() {
        return productCategoryService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "productCategoryId", required = false) String productCategoryIdStr,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tab", defaultValue = "list") String tab) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        // Kết hợp tìm kiếm theo tên và lọc theo danh mục
        if (keyword != null && !keyword.isEmpty() && productCategoryIdStr != null && !productCategoryIdStr.isEmpty()) {
            // Tìm kiếm theo tên trong danh mục cụ thể
            Integer productCategoryId = Integer.parseInt(productCategoryIdStr);
            productPage = productService.searchByNameAndCategory(keyword, productCategoryId, pageable);
            model.addAttribute("keyword", keyword);
            model.addAttribute("productCategoryId", productCategoryId);
        }
        // Chỉ có từ khóa tìm kiếm
        else if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        }
        // Chỉ có lọc theo danh mục
        else if (productCategoryIdStr != null && !productCategoryIdStr.isEmpty()) {
            Integer productCategoryId = Integer.parseInt(productCategoryIdStr);
            productPage = productService.findByProductCategoryIdPage(productCategoryId, pageable);
            model.addAttribute("productCategoryId", productCategoryId);
        }
        // Hiển thị tất cả sản phẩm
        else {
            productPage = productService.findByAllProduct(pageable);
        }

        // Đẩy dữ liệu danh sách và phân trang về view
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("hasPrevious", productPage.hasPrevious());
        model.addAttribute("product", new Product());
        model.addAttribute("view", "admin/ProductCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("product") Product product, Errors errors,
            @RequestParam("image1") MultipartFile image1,
            @RequestParam("image2") MultipartFile image2,
            @RequestParam("image3") MultipartFile image3,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate logic giảm giá tùy chỉnh
            discountValidator.validate(product, errors);

            if (errors.hasErrors()) {

                // Thêm validation cho ảnh khi tạo mới
                if (product.getId() == 0 && (image1 == null || image1.isEmpty())) {
                    errors.rejectValue("image_url", "image.required", "Vui lòng chọn ảnh chính");
                }

                // Giữ lại thông tin ảnh tạm thời nếu có lỗi validation
                if (image1 != null && !image1.isEmpty()) {
                    model.addAttribute("tempImage1", image1.getOriginalFilename());
                }
                if (image2 != null && !image2.isEmpty()) {
                    model.addAttribute("tempImage2", image2.getOriginalFilename());
                }
                if (image3 != null && !image3.isEmpty()) {
                    model.addAttribute("tempImage3", image3.getOriginalFilename());
                }

                model.addAttribute("view", "admin/ProductCRUD");
                model.addAttribute("activeTab", tab);
                return "admin/layout";
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình validation: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình kiểm tra dữ liệu: " + e.getMessage());
            model.addAttribute("view", "admin/ProductCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
        try {
            // Gọi service để lưu sản phẩm cùng với 3 hình ảnh
            Product savedProduct = productService.create(product, image1, image2, image3);
            // Gửi thông báo thành công qua flash
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            redirectAttributes.addFlashAttribute("newProductId", savedProduct.getId());
            redirectAttributes.addFlashAttribute("forceRefresh", true);
            // Chuyển về tab danh sách sau khi thêm thành công
            return "redirect:/Product/index?tab=list";
        } catch (IllegalArgumentException e) {
            // Nếu có lỗi thì hiển thị lại form và thông báo lỗi
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/ProductCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "tab", defaultValue = "edit") String tab) {

        // Tìm sản phẩm theo ID
        Product product = productService.findByID(id);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.findByAllProduct(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("product", product);
        model.addAttribute("view", "admin/ProductCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @PostMapping("/update")
    public String update(Model model, @Valid @ModelAttribute("product") Product product, Errors errors,
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3,
            @RequestParam(value = "oldImages", required = false) String[] oldImages,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        try {
            // Lấy thông tin sản phẩm hiện tại để so sánh ngày
            Product currentProduct = productService.findByID(product.getId());

            // Kiểm tra xem có thay đổi ngày giảm giá không
            boolean dateChanged = false;
            if (currentProduct != null) {
                // So sánh ngày bắt đầu
                boolean startDateChanged = false;
                if (currentProduct.getDiscountStart() == null && product.getDiscountStart() != null) {
                    startDateChanged = true;
                } else if (currentProduct.getDiscountStart() != null && product.getDiscountStart() == null) {
                    startDateChanged = true;
                } else if (currentProduct.getDiscountStart() != null && product.getDiscountStart() != null) {
                    startDateChanged = !currentProduct.getDiscountStart().equals(product.getDiscountStart());
                }

                // So sánh ngày kết thúc
                boolean endDateChanged = false;
                if (currentProduct.getDiscountEnd() == null && product.getDiscountEnd() != null) {
                    endDateChanged = true;
                } else if (currentProduct.getDiscountEnd() != null && product.getDiscountEnd() == null) {
                    endDateChanged = true;
                } else if (currentProduct.getDiscountEnd() != null && product.getDiscountEnd() != null) {
                    endDateChanged = !currentProduct.getDiscountEnd().equals(product.getDiscountEnd());
                }

                dateChanged = startDateChanged || endDateChanged;
            }

            // Chỉ validate ngày khi có thay đổi ngày
            if (dateChanged) {
                discountValidator.validate(product, errors);
            } else {
                discountValidator.validateWithoutDateCheck(product, errors);
            }

            if (errors.hasErrors()) {

                // Khi có lỗi validation, giữ lại thông tin ảnh cũ để không bị mất
                Product existingProduct = productService.findByID(product.getId());
                if (existingProduct != null) {
                    if (product.getImage_url() == null || product.getImage_url().isEmpty()) {
                        product.setImage_url(existingProduct.getImage_url());
                    }
                    if (product.getImage_url2() == null || product.getImage_url2().isEmpty()) {
                        product.setImage_url2(existingProduct.getImage_url2());
                    }
                    if (product.getImage_url3() == null || product.getImage_url3().isEmpty()) {
                        product.setImage_url3(existingProduct.getImage_url3());
                    }
                }

                model.addAttribute("view", "admin/ProductCRUD");
                model.addAttribute("activeTab", tab);
                return "admin/layout";
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình validation update: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình kiểm tra dữ liệu: " + e.getMessage());
            model.addAttribute("view", "admin/ProductCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
        try {
            // Nếu không áp dụng giảm giá, reset các trường liên quan
            if (product.getDiscountPercent() == null &&
                    product.getDiscountStart() == null &&
                    product.getDiscountEnd() == null) {
                product.setDiscountPercent(null);
                product.setDiscountStart(null);
                product.setDiscountEnd(null);
            }

            // Gọi service để cập nhật sản phẩm
            productService.update(product, image1, image2, image3, oldImages);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            // Chuyển về tab danh sách sau khi cập nhật thành công
            return "redirect:/Product/index?tab=list";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/ProductCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model,
            @ModelAttribute("product") Product product, Errors errors,
            @PathVariable("id") long id,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        try {
            // Gọi service xóa sản phẩm theo ID
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm!");
            // Chuyển về tab danh sách sau khi xóa thành công
            return "redirect:/Product/index?tab=list";

        } catch (IllegalArgumentException e) {
            // Nếu xóa thất bại thì chuyển hướng về lại form chỉnh sửa
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Product/edit/" + product.getId() + "?tab=" + tab;
        }
    }

}
