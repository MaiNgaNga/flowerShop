package com.datn.Service;

import java.util.List;

import com.datn.model.Order;
import com.datn.model.OrderDetail;

public interface OrderDetailService {
    List<OrderDetail> getOrderDetailsByOrder(Order order);

    OrderDetail getOrderDetailById(Long id);

    List<OrderDetail> getAllOrderDetails();

    void deleteOrderDetail(Long id);
}
