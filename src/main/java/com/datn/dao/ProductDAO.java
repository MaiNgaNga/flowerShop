
package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Product;

public interface ProductDAO extends JpaRepository<Product, Long> {
        // Lấy 6 sản phẩm mới nhất từ 6 danh mục khác nhau
        @Query(value = "SELECT TOP 6 p.* FROM products p " +
                        "INNER JOIN ( " +
                        "    SELECT product_Category_Id, MAX(id) as max_id " +
                        "    FROM products " +
                        "    GROUP BY product_Category_Id " +
                        ") latest ON p.product_Category_Id = latest.product_Category_Id " +
                        "AND p.id = latest.max_id " +
                        "ORDER BY p.product_Category_Id", nativeQuery = true)
        List<Product> findLatestProductsPerCategory();

        @Query("SELECT p FROM Product p WHERE p.productCategory.name LIKE :name order by p.id desc")
        List<Product> findProductByNameProductCategory(@Param("name") String name);

        @Query(value = "SELECT p.* FROM products p " +
                        "JOIN (SELECT p2.product_Category_Id, MAX(p2.quantity) AS max_quantity " +
                        "FROM products p2 GROUP BY p2.product_Category_Id) temp " +
                        "ON p.product_Category_Id = temp.product_Category_Id " +
                        "AND p.quantity = temp.max_quantity", nativeQuery = true)
        // so luong nhieu nhat
        List<Product> findBestSellingProductPerCategory();

        @Query("select p from Product p order by p.quantity desc")
        List<Product> findByProductOrderByQuantityDesc();

        // Lấy top 6 sản phẩm có số lượng nhiều nhất
        @Query(value = "SELECT TOP 6 * FROM products ORDER BY quantity DESC", nativeQuery = true)
        List<Product> findTop6ByOrderByQuantityDesc();

        List<Product> findByCategory_Id(int categoryId);

        List<Product> findByProductCategoryId(int productCategoryId);

        @Query("SELECT p FROM Product p WHERE p.productCategory.id = :productCategoryId " +
                        "AND p.price BETWEEN :minPrice AND :maxPrice")

        Page<Product> findByPriceRange(
                        @Param("productCategoryId") Integer productCategoryId,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.productCategory.id = :productCategoryId " +
                        "AND p.color.name LIKE %:color%")

        Page<Product> findByColor(
                        @Param("productCategoryId") Integer productCategoryId,
                        @Param("color") String color,
                        Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.productCategory.id = :productCategoryId " +
                        "AND p.category.id=:categoryId")

        Page<Product> findByCaId(
                        @Param("productCategoryId") Integer productCategoryId,
                        @Param("categoryId") Integer categoryId,
                        Pageable pageable);

        // @Query("SELECT p FROM Product p JOIN OrderDetail od ON p.id = od.product.id
        // WHERE p.productCategory.id = :productCategoryId GROUP BY p.id ORDER BY
        // SUM(od.quantity) DESC")
        // Page<Product> findBestSellingProductsByCategory(@Param("productCategoryId")
        // Integer productCategoryId,Pageable pageable);

        // Method để tìm best seller theo product category name (giới hạn 10 sản phẩm)
        @Query(value = "SELECT TOP 10 p.* FROM products p INNER JOIN product_categories pc ON p.product_Category_Id = pc.id WHERE pc.name = :categoryName ORDER BY p.quantity DESC", nativeQuery = true)
        List<Product> findBestSellerByCategory(@Param("categoryName") String categoryName);

        // san pham tuong tu theo category
        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
        List<Product> findProductByCategory(Integer categoryId);

        // Gọi: productRepository.findHotProductsFromOtherCategories(id,
        // PageRequest.of(0, limit));

        // Lọc dịch vụ
        @Query("SELECT p FROM Product p WHERE p.productCategory.name LIKE %:name% ORDER BY p.id DESC")
        Page<Product> findByProductCategoryName(@Param("name") String name, Pageable pageable);

        // Lọc theo danh mục
        Page<Product> findByProductCategoryId(
                        @Param("productCategoryId") Integer productCategoryId,
                        Pageable pageable);

        // Tìm kiếm
        @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

        // Tìm kiếm theo loại hoa (category name)
        @Query("SELECT p FROM Product p WHERE LOWER(p.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
        Page<Product> searchByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

        // Tìm kiếm theo danh mục hoa (productCategory name)
        @Query("SELECT p FROM Product p WHERE LOWER(p.productCategory.name) LIKE LOWER(CONCAT('%', :productCategoryName, '%'))")
        Page<Product> searchByProductCategoryName(@Param("productCategoryName") String productCategoryName,
                        Pageable pageable);

        // Tìm kiếm theo tên sản phẩm (đã có sẵn: searchByName)

}
