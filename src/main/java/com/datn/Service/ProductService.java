package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.datn.model.Product;

public interface ProductService {
    List<Product> findAll();

    Product findByID(long id);

    Product create(Product entity, MultipartFile imageFile);

    void update(Product entity, MultipartFile imageFile, String oldImage);

    void deleteById(long id);

    boolean existsById(long id);

    List<Product> findBestSellingProductPerCategory();

    List<Product> findByProductOrderByQuantityDesc();

    List<Product> findLatestProductsPerCategory();

    List<Product> findProductByNameProductCategory(String name);

    List<Product> findByCategory_Id(int id);

    List<Product> findByProductCategoryId(int id);

    Page<Product> findByPriceRange(
            Integer productCategoryId,
            Double minPrice,
            Double maxPrice,
            Pageable pageable);

    Page<Product> findByProductCategoryIdPage(
            Integer productCategoryId,
            Pageable pageable);

    Page<Product> findByColor(
            Integer productCategoryId,
            String color,
            Pageable pageable);

    Page<Product> findByCaId(
            Integer productCategoryId,
            int categoryId,
            Pageable pageable);

    // Page<Product> findBestSellingProductsByCategory( Integer
    // productCatelogyId,Pageable pageable);
    Page<Product> findByAllProduct(Pageable pageable);

    Page<Product> searchByName(String name, Pageable pageable);

}
