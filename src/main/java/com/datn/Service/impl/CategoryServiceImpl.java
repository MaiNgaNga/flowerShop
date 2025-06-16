package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.CategoryService;
import com.datn.dao.CategoryDAO;
import com.datn.model.Category;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryDAO dao;
    @Autowired
    productDAO productDAO;

    @Override
    public List<Category> findAll() {
        return dao.findAll();
    }

    @Override
    public Category findByID(int id) {
        Optional<Category> category = dao.findById(id);
        return category.orElse(null);
    }

    @Override
    public Category create(Category entity) {
        if (dao.existsByName(entity.getName())) {
            throw new IllegalArgumentException("Tên loại hoa này đã tồn tại!");
        }
        return dao.save(entity);
    }

    @Override
    public void update(Category entity) {
        if (dao.existsById(entity.getId())) {
            if (dao.existsByNameAndIdNot(entity.getName(), entity.getId())) {
                throw new IllegalArgumentException("Loại hoa này đã tồn tại!");
            }
            if (entity.getName().isEmpty()) {
                throw new IllegalArgumentException("Tên loại hoa không được để trống!");
            }
            dao.save(entity);
        } else {
            throw new IllegalArgumentException("Chưa chọn loại hoa để cập nhật!");
        }
    }

    @Override
    public void deleteById(int id) {

        if (dao.existsById(id)) {
            if (!productDAO.findByCategory_Id(id).isEmpty()) {
                throw new IllegalArgumentException("Loại hoa này đã tồn tại sản phẩm hoa!");

            }
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không xác định được loại hoa cần xóa");
        }
    }

    @Override
    public boolean existsById(int id) {
        return dao.existsById(id);
    }
}
