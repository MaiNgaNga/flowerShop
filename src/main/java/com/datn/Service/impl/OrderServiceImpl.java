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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

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

    // Thống kê doanh thu theo tháng trong năm
    @Override
    public Map<Integer, Double> getMonthlyRevenueByYear(int year) {
        List<Object[]> results = dao.getMonthlyRevenueByYear(year);
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Double revenue = (Double) row[1];
            revenueMap.put(month, revenue);
        }
        return revenueMap;
    }

    // Thống kê doanh thu theo trong năm
    @Override
    public Double getTotalRevenueInYear(int year) {
        Double total = dao.getTotalRevenueInYear(year);
        return total != null ? total : 0.0;
    }

    // đếm tổng số đơn hàng trong tháng/năm
    @Override
    public Long getTotalOrdersInMonth(int month, int year) {
        return dao.countTotalOrdersByMonthAndYear(month, year);
    }

    // thống kê đơn hàng hủy
    @Override
    public Long countCancelledOrdersByMonthAndYear(int month, int year) {
        return dao.countCanceledOrdersByMonthAndYear(month, year);
    }

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
        Order order = dao.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
        User shipper = userDAO.findById(shipperId)
                .orElseThrow(() -> new IllegalArgumentException("Shipper không tồn tại"));

        // Kiểm tra trạng thái hợp lệ
        if (!order.getStatus().equals("Đã xác nhận") && !order.getStatus().equals("Đang giao lại")) {
            throw new IllegalStateException("Chỉ có thể nhận đơn hàng ở trạng thái 'Đã xác nhận' hoặc 'Đang giao lại'");
        }

        order.setStatus("Đang giao");
        order.setShipper(shipper);
        return dao.save(order);
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

    @Override
    public void updateToCompleted(Long orderId, int shipperId) {
        Order order = dao.findById(orderId).orElse(null);
        User shipper = userDAO.findById(shipperId).orElse(null);
        if (order != null && shipper != null && "Đang giao".equals(order.getStatus())) {
            order.setStatus("Đã giao");
            order.setShipper(shipper);
            order.setDeliveryDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
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

    @Override
    public List<Order> findReturnedOrdersByShipper(int shipperId) {
        return dao.findByStatusAndShipperId("Hoàn hàng", shipperId);
    }

    @Override
    public List<Order> findFailedOrdersByShipper(int shipperId) {
        return dao.findByStatusAndShipperId("Giao thất bại", shipperId);
    }

    @Override
    @Transactional
    public Order cancelByShipper(Long orderId, int shipperId, String cancelReason, String cancelDetails) {
        Order order = dao.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        if (order.getShipper() == null || order.getShipper().getId() != shipperId) {
            throw new IllegalArgumentException("Bạn không được phép hủy đơn hàng này");
        }

        order.setReason(cancelReason); // Lưu lý do hủy vào cột reason
        order.setDescription(cancelDetails); // Lưu chi tiết lý do vào cột description
        order.setStatus("Đã hủy");
        return dao.save(order);
    }

    @Override
    public Double getTotalCompletedOrdersAmount(int shipperId) {
        Double result = dao.getTotalCompletedAmountByShipperId(shipperId);
        System.out.println("Total Amount (before null check): " + result);
        return result != null ? result : 0.0;
    }

    @Override
    public List<Order> getOrdersByShipperAndDate(int shipperId, java.util.Date date) {
        return dao.getOrdersByShipperAndDate(shipperId, date);
    }

    @Override
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

    @Override
    public Page<Order> searchPosOrdersByOrderCode(String orderType, String orderCode, LocalDate fromDate,
            LocalDate toDate, Pageable pageable) {
        return dao.searchPosOrdersByOrderCode(orderType, orderCode, fromDate, toDate, pageable);
    }

    public Order recreateOrder(Long canceledOrderId) {
        // Tìm đơn hàng bị hủy
        Order canceledOrder = getOrderById(canceledOrderId);
        if (!canceledOrder.getStatus().equals("Đã hủy")) {
            throw new IllegalStateException("Chỉ có thể tạo lại đơn hàng đã hủy");
        }

        // Tạo đơn hàng mới
        Order newOrder = new Order();
        newOrder.setStatus("Đang giao lại");
        newOrder.setOriginalId(canceledOrder.getId()); // Gán ID đơn hàng gốc
        newOrder.setTotalAmount(canceledOrder.getTotalAmount());
        newOrder.setAddress(canceledOrder.getAddress());
        newOrder.setSdt(canceledOrder.getSdt());
        newOrder.setDescription(canceledOrder.getDescription());
        newOrder.setCreateDate(new Date());
        newOrder.setDeliveryDate(canceledOrder.getDeliveryDate());
        newOrder.setUser(canceledOrder.getUser());
        newOrder.setShipper(canceledOrder.getShipper());

        // Sao chép chi tiết đơn hàng
        List<OrderDetail> newOrderDetails = new ArrayList<>();
        for (OrderDetail detail : canceledOrder.getOrderDetails()) {
            OrderDetail newDetail = new OrderDetail();
            newDetail.setOrder(newOrder);
            newDetail.setProduct(detail.getProduct());
            newDetail.setQuantity(detail.getQuantity());
            newDetail.setPrice(detail.getPrice());
            newOrderDetails.add(newDetail);
        }
        newOrder.setOrderDetails(newOrderDetails);

        // Lưu đơn hàng mới
        return dao.save(newOrder);

    }

    @Override
    public Order findByID(Long id) {
        return dao.findById(id).orElse(null);
    }

}