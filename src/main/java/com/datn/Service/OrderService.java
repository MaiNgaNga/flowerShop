package com.datn.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Order;
import com.datn.model.OrderDetail;


public interface OrderService {
    Order saveOrder(Order order, List<OrderDetail> orderDetails);

    Order getOrderById(Long id);

    List<Order> getAllOrders();

    void deleteOrder(Long id);

    Order updateStatus(Long orderId, String status);

    List<Order> getOrdersByUser(int id);

    List<Order> findByStatus(String status);

    Double sumTotalAmountWhereStatusLike(String status);

    Double sumTotalAmountForCurrentMonth(String status);

    Double getAverageOrderValue(String status);

    Long countOrdersThisMonth(String status);

    Long getCountOrder(String status);

    List<Order> getOrdersByStatus(String status);

    public Order updateToDangGiao(Long orderId, int shipperId);

    List<Order> getOrdersByStatusAndShipper(String status, int shipperId);

    List<Order> getHistoryOrders(int shipperId);

    List<Order> getOrdersByStatusAndShipper(List<String> statuses, int shipperId);

    void updateToCompleted(Long orderId, int shipperId);

    public Order updateToReturned(Long orderId, int shipperId);

    List<Order> findReturnedOrdersByShipper(int shipperId);

    Order cancelByShipper(Long orderId, int shipperId);

    Double getTotalCompletedOrdersAmount(int shipperId);

    List<Order> getOrdersByShipperAndDate(int shipperId, Date date);

    Double getTotalAmountByShipperAndDate(int shipperId, Date date);

}
