package com.datn.Service.impl;

import com.datn.Service.PosService;
import com.datn.dao.PosDAO;
import com.datn.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PosServiceImpl implements PosService {
    @Autowired
    private PosDAO posDAO;

    @Override
    public List<Product> getAllProducts() {
        return posDAO.findAll();
    }

    @Override
    public Page<Product> getProductsPage(Pageable pageable) {
        return posDAO.findAll(pageable);
    }

    @Override
    public Page<Product> filterProducts(String color, String type, String category, String keyword, double minPrice,
            double maxPrice,
            Pageable pageable) {
        return posDAO.filterProducts(
                (color == null || color.isEmpty()) ? null : color,
                (type == null || type.isEmpty()) ? null : type,
                (category == null || category.isEmpty()) ? null : category,
                (keyword == null || keyword.isEmpty()) ? null : keyword,
                minPrice, maxPrice, pageable);
    }
}