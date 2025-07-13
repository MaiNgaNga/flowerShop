package com.datn.dao;

import com.datn.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PosDAO extends JpaRepository<Product, Long> {
        Page<Product> findAll(Pageable pageable);

        @Query("SELECT p FROM Product p " +
                        "WHERE (:color IS NULL OR p.color.name = :color) " +
                        "AND (:type IS NULL OR p.productCategory.name = :type) " +
                        "AND (:category IS NULL OR p.category.name = :category) " +
                        "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (p.price BETWEEN :minPrice AND :maxPrice)")
        Page<Product> filterProducts(
                        @Param("color") String color,
                        @Param("type") String type,
                        @Param("category") String category,
                        @Param("keyword") String keyword,
                        @Param("minPrice") double minPrice,
                        @Param("maxPrice") double maxPrice,
                        Pageable pageable);
}