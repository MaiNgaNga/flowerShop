package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.ProductCategory;

public interface ProductCategoryService {
   List<ProductCategory> findAll();

   ProductCategory findByID(int id);

   ProductCategory create(ProductCategory entity);

   void update(ProductCategory entity);

   void deleteById(int id);

   boolean existsById(int id);

   // Phân trang
   Page<ProductCategory> findAllPaginated(Pageable pageable);

}
