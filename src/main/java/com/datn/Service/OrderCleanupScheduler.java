package com.datn.Service;

import com.datn.dao.OrderDAO;
import com.datn.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class OrderCleanupScheduler {
    @Autowired
    private OrderDAO orderDAO;

    // Chạy mỗi 10 phút
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void removeExpiredPendingOrders() {
        // Lấy thời điểm 30 phút trước
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<Order> pendingOrders = orderDAO.findByStatus("Chờ thanh toán");
        for (Order order : pendingOrders) {
            Date createDate = order.getCreateDate();
            if (createDate != null) {
                LocalDateTime orderTime = Instant.ofEpochMilli(createDate.getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                if (orderTime.isBefore(threshold)) {
                    orderDAO.delete(order);
                    System.out.println("Đã xóa đơn chờ thanh toán quá hạn: " + order.getOrderCode());
                }
            }
        }
    }
}