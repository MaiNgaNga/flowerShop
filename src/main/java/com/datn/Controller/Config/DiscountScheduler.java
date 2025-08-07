package com.datn.Controller.Config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.dao.ProductDAO;
import com.datn.model.Product;

@Service
public class DiscountScheduler {
    @Autowired
    ProductDAO productDAO;

    // Chạy mỗi ngày lúc 00:01 giờ sáng
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void removeExpriredDiscounts() {
        LocalDate today = LocalDate.now();
        List<Product> allProducts = productDAO.findAll();

        for (Product product : allProducts) {
            if (product.getDiscountEnd() != null && today.isAfter(product.getDiscountEnd())) {
                product.setDiscountPercent(null);
                product.setDiscountStart(null);
                product.setDiscountEnd(null);
            }
        }
        productDAO.saveAll(allProducts);
    }
}
