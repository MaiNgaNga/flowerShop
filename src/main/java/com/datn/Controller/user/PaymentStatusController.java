package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.Service.OrderService;
import com.datn.model.Order;
import com.datn.utils.AuthService;
import com.datn.model.User;

import java.util.List;

@Controller
@RequestMapping("/payment-status")
public class PaymentStatusController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AuthService authService;

    /**
     * API để cập nhật đơn hàng PayOS thành "Thất bại" khi user back
     * Được gọi từ JavaScript khi detect user quay lại trang order
     */
    @PostMapping("/mark-abandoned")
    @ResponseBody
    public ResponseEntity<String> markPaymentAbandoned(@RequestParam(required = false) String orderCode) {
        try {
            User user = authService.getUser();
            if (user == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            // Tìm đơn hàng PayOS gần nhất của user
            List<Order> userOrders = orderService.getOrdersByUser(user.getId());
            Order latestPayOSOrder = null;
            
            for (Order order : userOrders) {
                if ("PAYOS".equals(order.getPaymentMethod()) && 
                    "Chờ thanh toán".equals(order.getPaymentStatus()) &&
                    "Chờ xác nhận".equals(order.getStatus())) {
                    
                    if (latestPayOSOrder == null || 
                        order.getCreateDate().after(latestPayOSOrder.getCreateDate())) {
                        latestPayOSOrder = order;
                    }
                }
            }

            if (latestPayOSOrder != null) {
                // Cập nhật thành Thất bại
                latestPayOSOrder.setStatus("Đã hủy");
                latestPayOSOrder.setPaymentStatus("Thất bại");
                orderService.saveOrder(latestPayOSOrder, latestPayOSOrder.getOrderDetails());
                
                System.out.println("Đã cập nhật đơn hàng bỏ dở qua API: " + latestPayOSOrder.getOrderCode());
                return ResponseEntity.ok("Order marked as failed");
            }

            return ResponseEntity.ok("No pending PayOS order found");
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật đơn hàng qua API: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error updating order");
        }
    }
}