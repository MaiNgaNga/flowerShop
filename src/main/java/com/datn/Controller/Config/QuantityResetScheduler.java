package com.datn.Controller.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.dao.ProductDAO;
import com.datn.model.Product;

@Service
public class QuantityResetScheduler {

    @Autowired
    ProductDAO productDAO;

    // Reset số lượng về 10 mỗi ngày lúc 00:00 (chỉ cho sản phẩm đã tồn tại)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetQuantitiesTo10() {
        try {
            List<Product> products = productDAO.findAll();
            
            for (Product product : products) {
                // Chỉ reset số lượng cho sản phẩm đã tồn tại từ trước
                // Sản phẩm mới thêm trong ngày sẽ giữ nguyên số lượng admin nhập
                product.setQuantity(10);
            }
            
            productDAO.saveAll(products);
            System.out.println("QuantityResetScheduler: Đã reset số lượng " + products.size() + 
                             " sản phẩm về 10 lúc " + java.time.LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("Lỗi khi reset số lượng sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
