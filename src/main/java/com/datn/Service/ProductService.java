package com.datn.Service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.datn.model.Product;

public interface ProductService {
        List<Product> findAll();

        Product findByID(long id);

        Product create(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3);

        void update(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3,
                        String[] oldImages);

        void deleteById(long id);

        boolean existsById(long id);

        List<Product> findBestSellingProductPerCategory();

        List<Product> findByProductOrderByQuantityDesc();

        List<Product> findTop6ByOrderByQuantityDesc();

        List<Product> findLatestProductsPerCategory();

        List<Product> findProductByNameProductCategory(String name);

        List<Product> findByCategory_Id(int id);

        List<Product> findByProductCategoryId(int id);

        Page<Product> findByPriceRange(
                        Integer productCategoryId,
                        Double minPrice,
                        Double maxPrice,
                        Pageable pageable);

        Page<Product> findByProductCategoryIdPage(
                        Integer productCategoryId,
                        Pageable pageable);

        Page<Product> findByColor(
                        Integer productCategoryId,
                        String color,
                        Pageable pageable);

        Page<Product> findByCaId(
                        Integer productCategoryId,
                        int categoryId,
                        Pageable pageable);

        // Page<Product> findBestSellingProductsByCategory( Integer
        // productCatelogyId,Pageable pageable);
        Page<Product> findByAllProduct(Pageable pageable);

        // Tìm kiếm theo tên
        Page<Product> searchByName(String name, Pageable pageable);

        // Tìm kiếm theo loại hoa (category)
        Page<Product> searchByCategoryName(String categoryName, Pageable pageable);

        // Tìm kiếm theo danh mục hoa (productCategory)
        Page<Product> searchByProductCategoryName(String productCategoryName, Pageable pageable);

        // Method để tìm best seller theo category name
        List<Product> findBestSellerByCategory(String categoryName);

        List<Product> findProductByCategory(Integer categoryId);

        Page<Product> findByProductCategoryName(String name, Pageable pageable);

        // Lấy 4 sản phẩm giảm giá nhiều nhất, còn hiệu lực, còn hàng
        List<Product> findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(int minDiscount);

        List<Product> findTop10ByProductCategoryName(String productCategoryName);

        /// sản phẩm hot
        List<Product> findBestSeller();

        List<Map<String, Object>> getTop6SellingProductsByYear(int year);

}
