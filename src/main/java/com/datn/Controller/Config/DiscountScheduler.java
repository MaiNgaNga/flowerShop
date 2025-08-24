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

    // Chạy mỗi ngày lúc 00:01 giờ sáng để reset giảm giá hết hạn
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void removeExpiredDiscounts() {
        LocalDate today = LocalDate.now();
        
        System.out.println("=== BẮT ĐẦU KIỂM TRA VÀ RESET GIẢM GIÁ HẾT HẠN ===");
        System.out.println("Ngày hiện tại: " + today);
        
        // Lấy tất cả sản phẩm có giảm giá đã hết hạn (bao gồm cả ngày hôm nay)
        List<Product> expiredDiscountProducts = productDAO.findByDiscountEndBefore(today.plusDays(1));
        
        System.out.println("Tìm thấy " + expiredDiscountProducts.size() + " sản phẩm có thể đã hết hạn giảm giá");
        
        int updatedCount = 0;
        
        for (Product product : expiredDiscountProducts) {
            // Kiểm tra nếu giảm giá đã hết hạn (ngày hôm nay sau ngày kết thúc hoặc bằng ngày kết thúc)
            if (product.getDiscountEnd() != null && 
                (today.isAfter(product.getDiscountEnd()) || today.isEqual(product.getDiscountEnd()))) {
                
                System.out.println("🔄 Reset giảm giá cho sản phẩm ID: " + product.getId() + 
                                 " - " + product.getName() + 
                                 " (Giảm giá: " + product.getDiscountPercent() + "%, " +
                                 "Hết hạn: " + product.getDiscountEnd() + ")");
                
                // Reset tất cả thông tin giảm giá
                product.setDiscountPercent(null);
                product.setDiscountStart(null);
                product.setDiscountEnd(null);
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            productDAO.saveAll(expiredDiscountProducts);
            System.out.println("✅ DiscountScheduler: Đã reset giảm giá cho " + updatedCount + 
                             " sản phẩm lúc " + java.time.LocalDateTime.now());
        } else {
            System.out.println("ℹ️ DiscountScheduler: Không có sản phẩm nào cần reset giảm giá lúc " + 
                             java.time.LocalDateTime.now());
        }
        
        System.out.println("=== KẾT THÚC KIỂM TRA RESET GIẢM GIÁ ===\n");
    }
    
    // Chạy mỗi giờ để kiểm tra và reset giảm giá hết hạn (để đảm bảo real-time hơn)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void hourlyDiscountCheck() {
        LocalDate today = LocalDate.now();
        
        // Chỉ lấy sản phẩm có ngày kết thúc giảm giá là hôm nay
        List<Product> todayExpiredProducts = productDAO.findByDiscountEndBefore(today.plusDays(1))
            .stream()
            .filter(p -> p.getDiscountEnd() != null && 
                        (today.isEqual(p.getDiscountEnd()) || today.isAfter(p.getDiscountEnd())))
            .toList();
        
        if (!todayExpiredProducts.isEmpty()) {
            int updatedCount = 0;
            
            for (Product product : todayExpiredProducts) {
                if (product.getDiscountPercent() != null) { // Chỉ reset nếu vẫn còn giảm giá
                    System.out.println("⏰ Hourly check - Reset giảm giá cho sản phẩm: " + product.getName() + 
                                     " (ID: " + product.getId() + ")");
                    
                    product.setDiscountPercent(null);
                    product.setDiscountStart(null);
                    product.setDiscountEnd(null);
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                productDAO.saveAll(todayExpiredProducts);
                System.out.println("✅ Hourly check: Reset " + updatedCount + " sản phẩm hết hạn giảm giá");
            }
        }
    }
    
    // Chạy mỗi ngày lúc 00:02 để kích hoạt giảm giá mới bắt đầu
    @Scheduled(cron = "0 2 0 * * *")
    @Transactional
    public void activateNewDiscounts() {
        LocalDate today = LocalDate.now();
        
        System.out.println("=== KIỂM TRA KÍCH HOẠT GIẢM GIÁ MỚI ===");
        System.out.println("Ngày hiện tại: " + today);
        
        // Tìm tất cả sản phẩm có ngày bắt đầu giảm giá là hôm nay
        List<Product> allProducts = productDAO.findAll();
        int activatedCount = 0;
        
        for (Product product : allProducts) {
            if (product.getDiscountStart() != null && 
                product.getDiscountPercent() != null && 
                product.getDiscountPercent() > 0 &&
                today.isEqual(product.getDiscountStart())) {
                
                System.out.println("🎯 Kích hoạt giảm giá cho sản phẩm: " + product.getName() + 
                                 " (ID: " + product.getId() + ", Giảm: " + product.getDiscountPercent() + "%)");
                activatedCount++;
            }
        }
        
        if (activatedCount > 0) {
            System.out.println("✅ Đã kích hoạt giảm giá cho " + activatedCount + " sản phẩm");
        } else {
            System.out.println("ℹ️ Không có sản phẩm nào bắt đầu giảm giá hôm nay");
        }
        
        System.out.println("=== KẾT THÚC KIỂM TRA KÍCH HOẠT GIẢM GIÁ ===\n");
    }
}
