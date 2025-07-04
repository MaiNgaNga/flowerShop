package com.datn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.Category;

public interface CategoryDAO extends JpaRepository<Category, Integer> {
     boolean existsByName(String name);

     boolean existsByNameAndIdNot(String name, int id);
}
