package com.datn.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Category;
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

        @Query("SELECT DISTINCT p.category FROM Product p WHERE p.productCategory.id = :productCategoryId")
        List<Category> findCategoriesByProductCategoryId(
                        @Param("productCategoryId") int productCategoryId);

        @Query("""
                        SELECT p FROM Product p
                        WHERE p.productCategory.id = :proCategoryId
                          AND (:ca_Id IS NULL OR p.category.id = :ca_Id)
                          AND (:color IS NULL OR p.color.name LIKE :color)
                          AND (:minPrice IS NULL OR p.price >= :minPrice)
                          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
                        """)
        Page<Product> findByMultipleFilters(
                        @Param("proCategoryId") Integer proCategoryId,
                        @Param("ca_Id") Integer ca_Id,
                        @Param("color") String color,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        Pageable pageable);

        // Method để tìm best seller theo product category name (giới hạn 10 sản phẩm)
        @Query(value = "SELECT TOP 10 p.* FROM products p INNER JOIN product_categories pc ON p.product_Category_Id = pc.id WHERE pc.name = :categoryName ORDER BY p.quantity DESC", nativeQuery = true)
        List<Product> findBestSellerByCategory(@Param("categoryName") String categoryName);

        @Query(value = """
                            SELECT TOP 8
                                p.id, p.name, p.description, p.price, p.quantity,
                                p.image_url, p.image_url2, p.image_url3,
                                p.product_category_id, p.color_id, p.category_id,
                                p.discount_percent, p.discount_start, p.discount_end, p.available
                            FROM products p
                            JOIN order_details od ON p.id = od.product_id
                            GROUP BY
                                p.id, p.name, p.description, p.price, p.quantity,
                                p.image_url, p.image_url2, p.image_url3,
                                p.product_category_id, p.color_id, p.category_id,
                                p.discount_percent, p.discount_start, p.discount_end, p.available
                            ORDER BY SUM(od.quantity) DESC
                        """, nativeQuery = true)
        List<Product> findSellingProducts();

        // san pham tuong tu theo category

        @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
        List<Product> findProductByCategory(Integer categoryId);

        @Query("SELECT p FROM Product p WHERE p.productCategory.id = :productCategoryId")
        List<Product> findProductByProductCategory(Integer productCategoryId);

        // NVC
        // Lọc dịch vụ
        @Query("SELECT p FROM Product p WHERE p.productCategory.name LIKE %:name% ORDER BY p.id DESC")
        Page<Product> findByProductCategoryName(@Param("name") String name, Pageable pageable);

        // Lọc theo danh mục
        Page<Product> findByProductCategoryId(
                        @Param("productCategoryId") Integer productCategoryId,
                        Pageable pageable);

        // Tìm kiếm theo tên sản phẩm và tên loại hoa
        @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

        // Tìm kiếm theo tên sản phẩm, tên loại hoa kết hợp với lọc theo danh mục
        @Query("SELECT p FROM Product p WHERE " +
                        "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND p.productCategory.id = :productCategoryId")
        Page<Product> searchByNameAndCategory(@Param("keyword") String keyword,
                        @Param("productCategoryId") Integer productCategoryId, Pageable pageable);

        // Tìm kiếm theo loại hoa (category name)
        @Query(value = "SELECT p.* FROM products p INNER JOIN categories c ON p.Category_Id = c.id WHERE c.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :categoryName, '%')", countQuery = "SELECT COUNT(*) FROM products p INNER JOIN categories c ON p.Category_Id = c.id WHERE c.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :categoryName, '%')", nativeQuery = true)
        Page<Product> searchByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

        // Tìm kiếm theo danh mục hoa (productCategory name)
        @Query(value = "SELECT p.* FROM products p INNER JOIN product_categories pc ON p.product_Category_Id = pc.id WHERE pc.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :productCategoryName, '%')", countQuery = "SELECT COUNT(*) FROM products p INNER JOIN product_categories pc ON p.product_Category_Id = pc.id WHERE pc.name COLLATE Latin1_General_CI_AI LIKE CONCAT('%', :productCategoryName, '%')", nativeQuery = true)
        Page<Product> searchByProductCategoryName(@Param("productCategoryName") String productCategoryName,
                        Pageable pageable);

        // Bùi Anh Thiện
        List<Product> findAllByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(int minDiscount);

        @Query(value = "SELECT TOP 10 p.* FROM products p INNER JOIN product_categories pc ON p.product_Category_Id = pc.id WHERE pc.name = :productCategoryName ORDER BY p.quantity DESC", nativeQuery = true)
        List<Product> findTop10ByProductCategoryName(@Param("productCategoryName") String productCategoryName);

        @Query(value = """
                        SELECT TOP 6
                            pc.id AS id,
                            pc.name AS name,
                            SUM(od.quantity) AS total_quantity_sold
                        FROM order_details od
                        INNER JOIN orders o ON od.order_id = o.id
                        INNER JOIN products p ON od.product_id = p.id
                        INNER JOIN product_categories pc ON p.category_id = pc.id
                        WHERE YEAR(o.create_date) = :year
                        GROUP BY pc.id, pc.name
                        ORDER BY total_quantity_sold DESC
                        """, nativeQuery = true)
        List<Map<String, Object>> getTop6SellingProductsByYear(@Param("year") int year);

        @Query(value = """
                        SELECT TOP 6
                            pc.id AS id,
                            pc.name AS name,
                            COALESCE(SUM(od.quantity), 0) AS total_quantity_sold
                        FROM order_details od
                        INNER JOIN orders o ON od.order_id = o.id
                        INNER JOIN products p ON od.product_id = p.id
                        INNER JOIN product_categories pc ON p.category_id = pc.id
                        WHERE YEAR(o.create_date) = :year AND MONTH(o.create_date) = :month
                        GROUP BY pc.id, pc.name
                        ORDER BY total_quantity_sold DESC
                        """, nativeQuery = true)
        List<Map<String, Object>> getTop6SellingProductsByYearAndMonth(@Param("year") int year,
                        @Param("month") int month);

        // Tìm sản phẩm có giảm giá đã hết hạn
        @Query("SELECT p FROM Product p WHERE p.discountEnd IS NOT NULL AND p.discountEnd < :date")
        List<Product> findByDiscountEndBefore(@Param("date") LocalDate date);

        // Tìm tất cả sản phẩm sắp xếp theo ID giảm dần (sản phẩm mới nhất trước)
        @Query("SELECT p FROM Product p ORDER BY p.id DESC")
        Page<Product> findAllOrderByIdDesc(Pageable pageable);

        @Query("SELECT p FROM Product p WHERE p.discountPercent > :minDiscount AND p.available = true "
                        + "AND (p.discountStart IS NULL OR CURRENT_DATE >= p.discountStart) "
                        + "AND (p.discountEnd IS NULL OR CURRENT_DATE <= p.discountEnd) "
                        + "AND (p.productCategory IS NULL OR LOWER(p.productCategory.name) NOT LIKE %:exclude%) "
                        + "ORDER BY p.discountPercent DESC")
        List<Product> findDiscountProductsExcludeCategory(@Param("minDiscount") int minDiscount,
                        @Param("exclude") String exclude);
}