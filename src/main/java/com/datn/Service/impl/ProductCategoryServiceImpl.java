package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.ProductCategoryService;
import com.datn.dao.ProductCategoryDAO;
import com.datn.dao.ProductDAO;
import com.datn.model.ProductCategory;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
    @Autowired
    ProductCategoryDAO dao;
    @Autowired
    ProductDAO productDAO;

    @Override
    public List<ProductCategory> findAll() {
        return dao.findAll();
    }

    @Override
    public ProductCategory findByID(int id) {
        Optional<ProductCategory> productCategory = dao.findById((long) id);
        return productCategory.orElse(null);
    }

    @Override
    public ProductCategory create(ProductCategory entity) {
        if (dao.existsByName(entity.getName())) {
            throw new IllegalArgumentException("Tên danh mục này đã tồn tại!");
        }
        return dao.save(entity);
    }

    @Override
    public void update(ProductCategory entity) {
        int id = entity.getId();
        if (dao.existsById((long) id)) {
            if (dao.existsByNameAndIdNot(entity.getName(), id)) {
                throw new IllegalArgumentException("Danh mục này đã tồn tại!");
            }
            if (entity.getName() == null || entity.getName().isEmpty()) {
                throw new IllegalArgumentException("Tên danh mục không được để trống!");
            }
            dao.save(entity);
        } else {
            throw new IllegalArgumentException("Chưa chọn danh mục để cập nhật!");
        }
    }

    @Override
    public void deleteById(int id) {
        if (dao.existsById((long) id)) {
            if (!productDAO.findByProductCategoryId(id).isEmpty()) {
                throw new IllegalArgumentException("Danh mục này đã tồn tại sản phẩm!");
            }
            dao.deleteById((long) id);
        } else {
            throw new IllegalArgumentException("Không xác định được Danh mục cần xóa");
        }
    }

    @Override
    public boolean existsById(int id) {
        return dao.existsById((long) id);
    }
}
