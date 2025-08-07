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

    // Reset số lượng về 10 mỗi ngày lúc 00:00
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void resetQuantitiesTo10() {
        List<Product> products = productDAO.findAll();
        for (Product product : products) {
            product.setQuantity(10);
        }
        productDAO.saveAll(products);
        System.out.println("Reset số lượng sản phẩm về 10 lúc 0h : 01");
    }
}
