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

    // Chạy mỗi ngày lúc 00:05 giờ sáng (tránh xung đột với QuantityResetScheduler)
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void removeExpiredDiscounts() {
        LocalDate today = LocalDate.now();
        List<Product> allProducts = productDAO.findAll();
        
        int updatedCount = 0;
        
        for (Product product : allProducts) {
            // Kiểm tra nếu giảm giá đã hết hạn (ngày hôm nay sau ngày kết thúc)
            if (product.getDiscountEnd() != null && 
                (today.isAfter(product.getDiscountEnd()) || today.isEqual(product.getDiscountEnd()))) {
                
                // Reset tất cả thông tin giảm giá
                product.setDiscountPercent(null);
                product.setDiscountStart(null);
                product.setDiscountEnd(null);
                updatedCount++;
                
                System.out.println("Đã reset giảm giá cho sản phẩm ID: " + product.getId() + 
                                 " - " + product.getName());
            }
        }
        
        if (updatedCount > 0) {
            productDAO.saveAll(allProducts);
            System.out.println("DiscountScheduler: Đã reset giảm giá cho " + updatedCount + 
                             " sản phẩm lúc " + java.time.LocalDateTime.now());
        } else {
            System.out.println("DiscountScheduler: Không có sản phẩm nào cần reset giảm giá lúc " + 
                             java.time.LocalDateTime.now());
        }
    }
}
