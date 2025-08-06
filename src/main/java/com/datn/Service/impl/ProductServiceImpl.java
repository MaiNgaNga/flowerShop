
package com.datn.Service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datn.Service.ProductService;
import com.datn.dao.ProductDAO;
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

        if (image1 != null && !image1.isEmpty()) {
            entity.setImage_url(param.save(image1,
                    "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images").getName());
        }
        if (image2 != null && !image2.isEmpty()) {
            entity.setImage_url2(param.save(image2,
                    "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images").getName());
        }
        if (image3 != null && !image3.isEmpty()) {
            entity.setImage_url3(param.save(image3,
                    "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images").getName());
        }
        return dao.save(entity);
    }

    @Override
    public void update(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3,
            String[] oldImages) {
        if (dao.existsById(entity.getId())) {
            if (image1 != null && !image1.isEmpty()) {
                entity.setImage_url(param.save(image1,
                        "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images").getName());
            } else {
                entity.setImage_url(oldImages.length > 0 ? oldImages[0] : null);
            }

            // Ảnh 2
            if (image2 != null && !image2.isEmpty()) {
                entity.setImage_url2(param.save(image2,
                        "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images").getName());
            } else {
                entity.setImage_url2(oldImages.length > 1 ? oldImages[1] : null);
            }

            // Ảnh 3
            if (image3 != null && !image3.isEmpty()) {
                entity.setImage_url3(param.save(image3,
                        "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images").getName());
            } else {
                entity.setImage_url3(oldImages.length > 2 ? oldImages[2] : null);
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

    @Override
    public Page<Product> findByPriceRange(Integer productCategoryId, Double minPrice, Double maxPrice,
            Pageable pageable) {
        return dao.findByPriceRange(productCategoryId, minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> findByColor(Integer productCategoryId, String color,
            Pageable pageable) {
        return dao.findByColor(productCategoryId, color, pageable);
    }

    @Override
    public Page<Product> findByCaId(Integer productCategoryId, int categoryId,
            Pageable pageable) {
        return dao.findByCaId(productCategoryId, categoryId, pageable);
    }

    @Override
    public Page<Product> findByProductCategoryIdPage(Integer productCategoryId,
            Pageable pageable) {
        return dao.findByProductCategoryId(productCategoryId, pageable);
    }

    @Override
    public Page<Product> findByAllProduct(Pageable pageable) {
        return dao.findAll(pageable);
    }

    @Override
    public Page<Product> searchByName(String name, Pageable pageable) {
        return dao.searchByName(name, pageable);
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
    public List<Product> findProductByCategory(Integer categoryId) {
        // TODO Auto-generated method stub
        return dao.findProductByCategory(categoryId);
    }

    @Override
    public Page<Product> findByProductCategoryName(String name, Pageable pageable) {
        return dao.findByProductCategoryName(name, pageable);
    }

    @Override
    public List<Product> findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(
            int minDiscount) {
        // Lấy tất cả sản phẩm giảm giá, filter trong Java rồi limit 4
        List<Product> allDiscountProducts = dao
                .findAllByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(minDiscount);
        java.time.LocalDate now = java.time.LocalDate.now();
        return allDiscountProducts.stream()
                .filter(p -> p.getDiscountPercent() != null && p.getDiscountPercent() > 0
                        && (p.getDiscountStart() == null || !now.isBefore(p.getDiscountStart()))
                        && (p.getDiscountEnd() == null || !now.isAfter(p.getDiscountEnd())))
                .limit(4)
                .toList();
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

}
