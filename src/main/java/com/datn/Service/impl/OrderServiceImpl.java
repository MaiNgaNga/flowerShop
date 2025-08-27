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
    @Override
    public Page<Order> getOrdersByStatusAndShipper(String status, int shipperId, Pageable pageable) {
        return dao.findByStatusAndShipperIdOrderByIdDesc(status, shipperId, pageable);
    }

    @Override
    public List<Order> getOrdersByShipperAndMonthYear(int shipperId, int month, int year) {
        return dao.getOrdersByShipperAndMonthYear(shipperId, month, year);
    }

    @Override
    public Double getTotalAmountByShipperAndMonthYear(int shipperId, int month, int year) {
        Double total = dao.getTotalAmountByShipperAndMonthYear(shipperId, month, year);
        return total != null ? total : 0.0;
    }

    @Override
    public List<Order> getOrdersByShipperAndYear(int shipperId, int year) {
        return dao.getOrdersByShipperAndYear(shipperId, year);
    }

    @Override
    public Double getTotalAmountByShipperAndYear(int shipperId, int year) {
        Double total = dao.getTotalAmountByShipperAndYear(shipperId, year);
        return total != null ? total : 0.0;
    }

    @Override
    public List<Integer> getAvailableYearsForShipper(Integer shipperId) {
        return dao.getAvailableYearsForShipper(shipperId);
    }

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

    // ServiceImpl: Thống kê doanh thu theo ngày trong tháng/năm (chỉ lấy đơn hàng
    // 'Hoàn tất')
    @Override
    public Map<Integer, Double> getDailyRevenueByMonthAndYear(int month, int year) {
        List<Object[]> results = dao.getDailyRevenueByMonthAndYear(month, year);
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : results) {
            Integer day = (Integer) row[0];
            Double revenue = (Double) row[1];
            revenueMap.put(day, revenue);
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
        if (!order.getStatus().equals("Chờ giao") && !order.getStatus().equals("Giao lại")) {
            throw new IllegalStateException("Chỉ có thể nhận đơn hàng ở trạng thái 'Chờ giao' hoặc 'Giao lại'");
        }

        order.setStatus("Đang giao");
        order.setShipper(shipper);
        return dao.save(order);
    }

    @Override
    public List<Order> getOrdersByStatusAndShipper(String status, int shipperId) {
        List<Order> orders = dao.findByStatusAndShipperId(status, shipperId);
        orders.sort((o1, o2) -> {
            if (o1.getDeliveryDate() == null && o2.getDeliveryDate() == null)
                return 0;
            if (o1.getDeliveryDate() == null)
                return 1;
            if (o2.getDeliveryDate() == null)
                return -1;
            return o2.getDeliveryDate().compareTo(o1.getDeliveryDate());
        });
        return orders;
    }

    @Override
    public List<Order> getHistoryOrders(int shipperId) {
        return dao.findByStatusInAndShipperId(List.of("Hoàn tất"), shipperId);
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
            order.setStatus("Hoàn tất");
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
        order.setStatus("Giao thất bại");
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
        if (!canceledOrder.getStatus().equals("Giao thất bại")) {
            throw new IllegalStateException("Chỉ có thể tạo lại đơn hàng giao thất bại");
        }
        canceledOrder.setStatus("Đã hủy");
        dao.save(canceledOrder);
        // Tạo đơn hàng mới
        Order newOrder = new Order();
        newOrder.setStatus("Giao lại");
        newOrder.setOriginalId(canceledOrder.getId()); // Gán ID đơn hàng gốc
        newOrder.setTotalAmount(canceledOrder.getTotalAmount());
        newOrder.setAddress(canceledOrder.getAddress());
        newOrder.setSdt(canceledOrder.getSdt());
        newOrder.setDescription(canceledOrder.getDescription());
        newOrder.setCreateDate(new Date());
        newOrder.setDeliveryDate(canceledOrder.getDeliveryDate());
        newOrder.setUser(canceledOrder.getUser());
        newOrder.setShipper(canceledOrder.getShipper());

        newOrder.setShipFee(canceledOrder.getShipFee());
        newOrder.setOrderType(canceledOrder.getOrderType());
        newOrder.setPromotion(canceledOrder.getPromotion());
        newOrder.setDiscount(canceledOrder.getDiscount());
        newOrder.setDeliveryTime(canceledOrder.getDeliveryTime());
        newOrder.setReceiverName(canceledOrder.getReceiverName());
        newOrder.setReceiverPhone(canceledOrder.getReceiverPhone());
        newOrder.setPaymentMethod(canceledOrder.getPaymentMethod());
        newOrder.setPaymentStatus(canceledOrder.getPaymentStatus());
        newOrder.setPaymentUrl(canceledOrder.getPaymentUrl());
        newOrder.setTransactionId(canceledOrder.getTransactionId());

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

    public Long getTotalOrdersInYear(int year) {
        return dao.countTotalOrdersByYear(year);
    }

    @Override
    public Long countDeliveredOrdersByYear(int year) {
        return dao.countDeliveredOrdersByYear(year);
    }

    @Override
    public Long countTotalOrdersByMonthAndYear1(int month, int year) {
        return dao.countTotalOrdersByMonthAndYear1(month, year);
    }

    @Override
    public Long countPaidOrdersByYear(int year) {
        return dao.countPaidOrdersByYear(year);
    }

    public Order findByID(Long id) {
        return dao.findById(id).orElse(null);

    }

    @Override
    public Page<Order> findOrdersWithFilter(String status, String keyword, LocalDate fromDate, LocalDate toDate,
            Pageable pageable) {
        return dao.findOrdersWithFilter(status, keyword, fromDate, toDate, pageable);
    }

    public Page<Order> findAllOrders(Pageable pageable) {
        return dao.findAll(pageable);
    }

    @Override
    public long newOrders() {
        return dao.newOrders();
    }

    @Override
    // 📦 Đơn cần giao hôm nay
    public long getOrdersToDeliverToday() {
        return dao.countOrdersToDeliverToday();
    }

    @Override

    // 📅 Đơn sắp giao trong 3 ngày tới
    public long getOrdersNext3Days() {
        return dao.countOrdersNext3Days();
    }

    @Override

    // 🚚 Đơn giao thất bại
    public long getFailedOrders() {
        return dao.countFailedOrders();
    }

    // ✅ Đơn đã hoàn tất hôm nay
    public long getCompletedOrdersToday() {
        return dao.countCompletedOrdersToday();
    }

}