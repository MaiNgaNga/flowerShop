    package com.datn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.ProductCategory;

public interface ProductCategoryDAO extends JpaRepository<ProductCategory, Integer> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, int id);
}
