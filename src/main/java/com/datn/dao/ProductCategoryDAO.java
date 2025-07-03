package com.datn.dao;

import com.datn.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryDAO extends JpaRepository<ProductCategory, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, int id);
}
