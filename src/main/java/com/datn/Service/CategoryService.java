package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Category;


public interface CategoryService {
   List<Category> findAll();
   Category findByID(int id);
   Category create (Category entity);
   void update (Category entity);
   void deleteById(int id);
   boolean existsById(int id);
   Page<Category> searchByName(String name, Pageable pageable);
   Page<Category> findAllPaginated(Pageable pageable) ;

}
