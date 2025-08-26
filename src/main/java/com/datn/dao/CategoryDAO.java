package com.datn.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Category;


public interface CategoryDAO extends JpaRepository<Category,Integer> {
     boolean existsByName(String name);
     boolean existsByNameAndIdNot(String name, int id);
      @Query("SELECT p FROM Category p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Category> searchByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Category c ORDER BY c.id DESC")
    Page<Category> findAll(Pageable pageable);

}
