package com.datn.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.OrderDetailService;

import jakarta.transaction.Transactional;
import com.datn.dao.OrderDetailDAO;
import com.datn.model.Order;
import com.datn.model.OrderDetail;

import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailDAO dao;

    public List<OrderDetail> getOrderDetailsByOrder(Order order) {
        return dao.findByOrder(order);}

    @Override
    public OrderDetail getOrderDetailById(Long id) {
        return dao.findById(id).orElse(null);
    }
   

    @Override
    public List<OrderDetail> getAllOrderDetails() {
        return dao.findAll();
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long orderId) {
        dao.deleteByOrderId(orderId);;
    }
}


package com.datn.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.OrderDetailService;
import com.datn.dao.OrderDetailDAO;
import com.datn.model.Order;
import com.datn.model.OrderDetail;

import jakarta.transaction.Transactional;

import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailDAO dao;

    public List<OrderDetail> getOrderDetailsByOrder(Order order) {
        return dao.findByOrder(order);}

    @Override
    public OrderDetail getOrderDetailById(Long id) {
        return dao.findById(id).orElse(null);
    }
   

    @Override
    public List<OrderDetail> getAllOrderDetails() {
        return dao.findAll();
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long orderId) {
        dao.deleteByOrderId(orderId);;
    }
}
