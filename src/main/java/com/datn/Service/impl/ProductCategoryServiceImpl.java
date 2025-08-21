package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Optional<ProductCategory> ProductCategory = dao.findById(id);
        return ProductCategory.orElse(null);
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
        if (dao.existsById(entity.getId())) {
            if (dao.existsByNameAndIdNot(entity.getName(), entity.getId())) {
                throw new IllegalArgumentException("Danh mục này đã tồn tại!");
            }
            if (entity.getName().isEmpty()) {
                throw new IllegalArgumentException("Tên danh mục không được để trống!");
            }
            dao.save(entity);
        } else {
            throw new IllegalArgumentException("Chưa chọn danh mục để cập nhật!");
        }
    }

    @Override
    public void deleteById(int id) {

        if (dao.existsById(id)) {
            if (!productDAO.findByProductCategoryId(id).isEmpty()) {
                throw new IllegalArgumentException("Danh mục này đã tồn tại sản phẩm!");

            }
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không xác định được Danh mục cần xóa");
        }
    }

    @Override
    public boolean existsById(int id) {
        return dao.existsById(id);
    }

    @Override
    public Page<ProductCategory> findAllPaginated(Pageable pageable) {
        return dao.findAll(pageable);
    }
}
