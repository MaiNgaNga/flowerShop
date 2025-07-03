package com.datn.dao;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.Order;
import com.datn.model.OrderDetail;

public interface OrderDetailDAO extends JpaRepository<OrderDetail,Long> {
    List<OrderDetail> findByOrder(Order order);
    void deleteByOrderId(Long id);
   

}
