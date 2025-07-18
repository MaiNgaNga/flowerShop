package com.datn.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    private int quantity;

    private double price; 
    
    public double getTotal() {
        double originalPrice = product.getPrice();
        Integer discount = product.getDiscountPercent();
        if (discount != null && discount > 0) {
            LocalDate now = LocalDate.now();
            LocalDate start = product.getDiscountStart();
            LocalDate end = product.getDiscountEnd();

            // Chỉ áp dụng giảm giá nếu trong thời gian hợp lệ
            if (start == null || !now.isBefore(start) && (end == null || !now.isAfter(end))) {
                double discountedPrice = originalPrice * (1 - discount / 100.0);
                return discountedPrice * quantity;
            }
        }
        return originalPrice * quantity;
    }
}