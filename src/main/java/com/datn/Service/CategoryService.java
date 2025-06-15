package com.datn.Service;

import java.util.List;

import com.datn.model.Category;


public interface CategoryService {
   List<Category> findAll();
   Category findByID(int id);
   Category create (Category entity);
   void update (Category entity);
   void deleteById(int id);
   boolean existsById(int id);
   
}
