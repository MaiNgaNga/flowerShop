package com.datn.Service.impl;

import com.datn.utils.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datn.Service.ProductService;
import com.datn.dao.ProductDAO;
import com.datn.model.Category;
import com.datn.model.Product;
import com.datn.utils.ParamService;
import com.datn.Service.CategoryService;
import com.datn.Service.ProductCategoryService;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO dao;
    @Autowired
    ParamService param;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductCategoryService productCategoryService;

    /**
     * Tạo đường dẫn thư mục images một cách nhất quán
     */
    private String getImagesPath() {
        String path = System.getProperty("user.dir") + "/src/main/resources/static/images";
        System.out.println("Images path: " + path);

        // Kiểm tra và tạo thư mục nếu chưa tồn tại
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            System.out.println("Created images directory: " + created);
        }

        return path;
    }

    @Override
    public List<Product> findAll() {
        return dao.findAll();
    }

    @Override
    public Product findByID(long id) {
        Optional<Product> product = dao.findById(id);
        return product.orElse(null);
    }

    @Override
    public Product create(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3) {
        if (dao.existsById(entity.getId())) {
            throw new IllegalArgumentException("ID sản phẩm này đã tồn tại!");
        }

        String imagePath = getImagesPath();

        if (image1 != null && !image1.isEmpty()) {
            File savedFile = param.save(image1, imagePath);
            if (savedFile != null) {
                entity.setImage_url(savedFile.getName());
                System.out.println("Saved image1: " + savedFile.getName());
            } else {
                System.err.println("Failed to save image1");
            }
        }
        if (image2 != null && !image2.isEmpty()) {
            File savedFile = param.save(image2, imagePath);
            if (savedFile != null) {
                entity.setImage_url2(savedFile.getName());
                System.out.println("Saved image2: " + savedFile.getName());
            } else {
                System.err.println("Failed to save image2");
            }
        }
        if (image3 != null && !image3.isEmpty()) {
            File savedFile = param.save(image3, imagePath);
            if (savedFile != null) {
                entity.setImage_url3(savedFile.getName());
                System.out.println("Saved image3: " + savedFile.getName());
            } else {
                System.err.println("Failed to save image3");
            }
        }
        return dao.save(entity);
    }

    @Override
    public void update(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3,
            String[] oldImages) {

        if (dao.existsById(entity.getId())) {
            String imagePath = getImagesPath();

            // Ảnh 1
            if (image1 != null && !image1.isEmpty()) {
                File savedFile = param.save(image1, imagePath);
                if (savedFile != null) {
                    entity.setImage_url(savedFile.getName());
                    System.out.println("Updated image1: " + savedFile.getName());
                } else {
                    System.err.println("Failed to update image1");
                }
            } else {
                String oldImage1 = (oldImages != null && oldImages.length > 0 && !oldImages[0].isEmpty()) ? oldImages[0]
                        : null;
                entity.setImage_url(oldImage1);
                System.out.println("Keeping old image1: " + oldImage1);
            }

            // Ảnh 2
            if (image2 != null && !image2.isEmpty()) {
                File savedFile = param.save(image2, imagePath);
                if (savedFile != null) {
                    entity.setImage_url2(savedFile.getName());
                    System.out.println("Updated image2: " + savedFile.getName());
                } else {
                    System.err.println("Failed to update image2");
                }
            } else {
                String oldImage2 = (oldImages != null && oldImages.length > 1 && !oldImages[1].isEmpty()) ? oldImages[1]
                        : null;
                entity.setImage_url2(oldImage2);
                System.out.println("Keeping old image2: " + oldImage2);
            }

            // Ảnh 3 - Fixed logic
            if (image3 != null && !image3.isEmpty()) {
                File savedFile = param.save(image3, imagePath);
                if (savedFile != null) {
                    entity.setImage_url3(savedFile.getName());
                    System.out.println("Updated image3: " + savedFile.getName());
                } else {
                    System.err.println("Failed to update image3");
                }
            } else {
                // Fix: Kiểm tra chính xác oldImages array
                String oldImage3 = null;
                if (oldImages != null && oldImages.length > 2) {
                    String img3 = oldImages[2];
                    if (img3 != null && !img3.trim().isEmpty() && !"null".equals(img3)) {
                        oldImage3 = img3;
                    }
                }
                entity.setImage_url3(oldImage3);
                System.out.println("Keeping old image3: " + oldImage3);
            }
            dao.save(entity);
        } else {
            throw new IllegalArgumentException("Chưa chọn sản phẩm để cập nhật!");
        }
    }

    @Override
    public void deleteById(long id) {
        if (dao.existsById(id)) {
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không xác định được sản phẩm cần xóa!");
        }
    }

    @Override
    public boolean existsById(long id) {
        return dao.existsById(id);
    }

    @Override
    public List<Product> findBestSellingProductPerCategory() {
        return dao.findBestSellingProductPerCategory();
    }

    @Override
    public List<Product> findByProductOrderByQuantityDesc() {
        return dao.findByProductOrderByQuantityDesc();
    }

    @Override
    public List<Product> findLatestProductsPerCategory() {
        return dao.findLatestProductsPerCategory();
    }

    @Override
    public List<Product> findProductByNameProductCategory(String name) {
        return dao.findProductByNameProductCategory(name);
    }

    @Override
    public List<Product> findByCategory_Id(int id) {
        return dao.findByCategory_Id(id);
    }

    @Override
    public List<Product> findByProductCategoryId(int id) {
        return dao.findByProductCategoryId(id);
    }

    public Page<Product> findByMultipleFilters(
            Integer proCategoryId,
            Integer ca_Id,
            String color,
            Double minPrice,
            Double maxPrice,
            Pageable pageable) {

        return dao.findByMultipleFilters(
                proCategoryId, ca_Id, color, minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> findByProductCategoryIdPage(Integer productCategoryId,
            Pageable pageable) {
        return dao.findByProductCategoryId(productCategoryId, pageable);
    }

    @Override
    public Page<Product> findByAllProduct(Pageable pageable) {
        return dao.findAllOrderByIdDesc(pageable);
    }

    @Override
    public Page<Product> searchByName(String name, Pageable pageable) {
        return dao.searchByName(name, pageable);
    }

    @Override
    public Page<Product> searchByNameAndCategory(String name, Integer productCategoryId, Pageable pageable) {
        return dao.searchByNameAndCategory(name, productCategoryId, pageable);
    }

    // Tìm kiếm theo loại hoa (category)
    @Override
    public Page<Product> searchByCategoryName(String categoryName, Pageable pageable) {
        return dao.searchByCategoryName(categoryName, pageable);
    }

    // Tìm kiếm theo danh mục hoa (productCategory)
    @Override
    public Page<Product> searchByProductCategoryName(String productCategoryName, Pageable pageable) {
        return dao.searchByProductCategoryName(productCategoryName, pageable);
    }

    @Override
    public List<Product> findTop6ByOrderByQuantityDesc() {
        return dao.findTop6ByOrderByQuantityDesc();
    }

    // TODO: Implement các phương thức cho Best Seller theo loại sản phẩm
    // (Cần thêm vào ProductService interface và ProductDAO trước)
    /*
     * @Override
     * public List<Product> findBestSellerLangHoa() {
     * return dao.findBestSellerLangHoa();
     * }
     * 
     * @Override
     * public List<Product> findBestSellerGioHoa() {
     * return dao.findBestSellerGioHoa();
     * }
     * 
     * @Override
     * public List<Product> findBestSellerBoHoa() {
     * return dao.findBestSellerBoHoa();
     * }
     * 
     * @Override
     * public List<Product> findBestSellerByCategory(Integer categoryId) {
     * return dao.findBestSellerByCategory(categoryId);
     * }
     */

    @Override
    public List<Product> findBestSellerByCategory(String categoryName) {
        return dao.findBestSellerByCategory(categoryName);
    }

    @Override
    public List<Product> findProductByProductCategory(Integer id) {
        return dao.findProductByProductCategory(id);
    }

    @Override
    public Page<Product> findByProductCategoryName(String name, Pageable pageable) {
        return dao.findByProductCategoryName(name, pageable);
    }

    @Override
    public List<Product> findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(
            int minDiscount) {
        // Lấy tất cả sản phẩm giảm giá, filter trong Java rồi limit 4
        List<Product> allDiscountProducts = dao.findDiscountProductsExcludeCategory(minDiscount, "phụ kiện");
        return allDiscountProducts.stream().limit(4).toList();
    }

    @Override
    public List<Product> findTop10ByProductCategoryName(String productCategoryName) {
        return dao.findTop10ByProductCategoryName(productCategoryName);
    }

    public List<Product> findBestSeller() {
        return dao.findSellingProducts();
    }

    @Override
    public List<Map<String, Object>> getTop6SellingProductsByYear(int year) {
        return dao.getTop6SellingProductsByYear(year);
    }

    @Override
    public List<String> findProductNamesByKeyword(String keyword, int limit) {
        Page<Product> page = dao.searchByName(keyword, PageRequest.of(0, limit));
        List<Product> products = page.getContent();
        List<String> names = new ArrayList<>();
        for (Product p : products) {
            names.add(p.getName());
        }
        return names;
    }

    @Override
    public List<String> findSearchSuggestionsByKeyword(String keyword, int limit) {
        // Gợi ý tên sản phẩm
        List<String> productNames = findProductNamesByKeyword(keyword, limit);

        // Gợi ý loại hoa (productCategory)
        List<String> categoryNames = productCategoryService.findAll().stream()
                .filter(pc -> pc.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(pc -> pc.getName())
                .collect(Collectors.toList());

        // Gợi ý danh mục (category)
        List<String> mainCategoryNames = categoryService.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(c -> c.getName())
                .collect(Collectors.toList());

        // Gộp tất cả, ưu tiên tên sản phẩm trước, loại bỏ trùng lặp
        List<String> suggestions = new ArrayList<>();
        Set<String> existed = new HashSet<>();
        for (String name : productNames) {
            if (existed.add(name))
                suggestions.add(name);
        }
        for (String name : categoryNames) {
            if (existed.add(name))
                suggestions.add(name);
        }
        for (String name : mainCategoryNames) {
            if (existed.add(name))
                suggestions.add(name);
        }
        return suggestions.size() > limit ? suggestions.subList(0, limit) : suggestions;
    }

    @Override
    public List<Map<String, Object>> getTop6SellingProductsByYearAndMonth(int year, int month) {
        return dao.getTop6SellingProductsByYearAndMonth(year, month);
    }

    @Override
    public List<Category> findCategoriesByProductCategoryId(@Param("productCategoryId") int productCategoryId) {
        return dao.findCategoriesByProductCategoryId(productCategoryId);
    }
}
