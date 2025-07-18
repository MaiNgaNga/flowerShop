package com.datn.Service;

import com.datn.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PosService {
    List<Product> getAllProducts();

    Page<Product> getProductsPage(Pageable pageable);

    Page<Product> filterProducts(String color, String type, String category, String keyword, double minPrice,
            double maxPrice,
            Pageable pageable);
}