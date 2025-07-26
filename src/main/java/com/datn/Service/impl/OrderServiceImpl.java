
package com.datn.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.OrderService;
import com.datn.dao.OrderDAO;
import com.datn.dao.OrderDetailDAO;
import com.datn.dao.UserDAO;
import com.datn.model.Order;
import com.datn.model.OrderDetail;
import com.datn.model.User;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDAO dao;
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    @Autowired
    private UserDAO userDAO;

    @Override
    public Order saveOrder(Order order, List<OrderDetail> orderDetails) {
        Order savedOrder = dao.save(order);
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(savedOrder);
            orderDetailDAO.save(detail);
        }

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public Order updateStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return dao.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return dao.findAll();
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        dao.deleteById(id);
    }

    @Override
    public List<Order> getOrdersByUser(int userid) {
        return dao.findByUserIdOrderByIdDesc(userid);
    }

    @Override
    public List<Order> findByStatus(String status) {
        return dao.findByStatusOrderByIdDesc(status);
    }

    @Override
    public Double sumTotalAmountWhereStatusLike(String status) {
        return dao.sumTotalAmount(status);
    }

    @Override
    public Double sumTotalAmountForCurrentMonth(String status) {
        return dao.sumTotalAmountForCurrentMonth(status);
    }

    @Override
    public Double getAverageOrderValue(String status) {
        return dao.getAverageOrderValue(status);
    }

    @Override
    public Long countOrdersThisMonth(String status) {
        return dao.countOrdersThisMonth(status);
    }

    @Override
    public Long getCountOrder(String status) {
        return dao.getCountOrder(status);
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        return dao.findByStatusOrderByIdDesc(status);
    }

    @Override
    public Order updateToDangGiao(Long orderId, int shipperId) {
        Order order = dao.findById(orderId).orElse(null);
        User shipper = userDAO.findById(shipperId).orElse(null); // lấy shipper theo ID
        if (order != null && "Đã xác nhận".equals(order.getStatus())) {
            order.setStatus("Đang giao");
            order.setShipper(shipper); // nếu bạn có trường này
            return dao.save(order);
        }
        return null;
    }

    @Override
    public List<Order> getOrdersByStatusAndShipper(String status, int shipperId) {
        return dao.findByStatusAndShipperId(status, shipperId);
    }

    @Override
    public List<Order> getHistoryOrders(int shipperId) {
        return dao.findByStatusInAndShipperId(List.of("Đã giao"), shipperId);

    }

    @Override
    public List<Order> getOrdersByStatusAndShipper(List<String> statuses, int shipperId) {
        return dao.findByStatusInAndShipperId(statuses, shipperId);
    }

    public void updateToCompleted(Long orderId, int shipperId) {
        Order order = dao.findById(orderId).orElse(null);
        User shipper = userDAO.findById(shipperId).orElse(null);
        if (order != null && shipper != null && "Đang giao".equals(order.getStatus())) {
            order.setStatus("Đã giao");
            order.setShipper(shipper);
            dao.save(order);
        }
    }

    @Override
    public Order updateToReturned(Long orderId, int shipperId) {
        Order order = dao.findById(orderId).orElse(null);
        if (order != null && order.getShipper().getId() == shipperId) {
            order.setStatus("Hoàn hàng");
            return dao.save(order);
        }
        return null;
    }

    public List<Order> findReturnedOrdersByShipper(int shipperId) {
        return dao.findByStatusAndShipperId("Hoàn hàng", shipperId);
    }

    @Override
    public Order cancelByShipper(Long orderId, int shipperId) {
        Order order = dao.findById(orderId).orElse(null);
        if (order != null && order.getShipper() != null && order.getShipper().getId() == shipperId) {
            order.setStatus("Đã xác nhận");
            return dao.save(order);
        }
        return null;
    }

    public Double getTotalCompletedOrdersAmount(int shipperId) {
        Double result = dao.getTotalCompletedAmountByShipperId(shipperId);
        System.out.println("Total Amount (before null check): " + result);
        return result != null ? result : 0.0;

    }

    public List<Order> getOrdersByShipperAndDate(int shipperId, java.util.Date date) {
        return dao.getOrdersByShipperAndDate(shipperId, date);
    }

    public Double getTotalAmountByShipperAndDate(int shipperId, java.util.Date date) {
        return dao.getTotalCompletedAmountByShipperIdAndDateNative(shipperId, date);

    }

    @Override
    public List<Order> getAllOfflineOrders() {
        return dao.findByOrderTypeIgnoreCase("Offline");
    }

    @Override
    public Page<Order> getPosOrdersByType(String orderType, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return dao.findPosOrders(orderType, fromDate, toDate, pageable);
    }
}
