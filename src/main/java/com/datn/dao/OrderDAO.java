package com.datn.dao;

import java.util.List;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Order;

public interface OrderDAO extends JpaRepository<Order, Long> {
        List<Order> findByUserIdOrderByIdDesc(int userId);

        List<Order> findByStatusOrderByIdDesc(String status);

        List<Order> findByStatusAndShipperId(String status, int shipperId);

        Optional<Order> findByOrderCode(String orderCode);

        List<Order> findByStatusInAndShipperId(List<String> statuses, int shipperId);

        @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = :status")
        Double averageOrderAmount(@Param("status") String status);

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status ")
        Double sumTotalAmount(@Param("status") String status);

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status AND MONTH(o.createDate) = MONTH(CURRENT_DATE) AND YEAR(o.createDate) = YEAR(CURRENT_DATE)")
        Double sumTotalAmountForCurrentMonth(@Param("status") String status);

        @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = :status")
        Double getAverageOrderValue(@Param("status") String status);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
        Long getCountOrder(@Param("status") String status);

        @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.createDate) = MONTH(CURRENT_DATE) AND YEAR(o.createDate) = YEAR(CURRENT_DATE) AND o.status = :status")
        Long countOrdersThisMonth(@Param("status") String status);

        @Modifying
        @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
        void updateOrderStatus(@Param("status") String status, @Param("orderId") Long orderId);

        @Query(value = "SELECT SUM(total_amount) FROM orders WHERE shipper_id = :shipperId AND status = N'Đã giao'", nativeQuery = true)
        Double getTotalCompletedAmountByShipperId(@Param("shipperId") int shipperId);

        @Query(value = "SELECT SUM(total_amount) FROM orders " +
                        "WHERE shipper_id = :shipperId " +
                        "AND status = N'Đã giao' " +
                        "AND CAST(delivery_date AS DATE) = :date", nativeQuery = true)
        Double getTotalCompletedAmountByShipperIdAndDateNative(@Param("shipperId") int shipperId,
                        @Param("date") Date date);

        @Query(value = "SELECT * FROM orders o WHERE o.shipper_id = :shipperId AND o.status = N'Đã giao' AND CONVERT(date, o.create_date) = :date", nativeQuery = true)
        List<Order> getOrdersByShipperAndDate(@Param("shipperId") int shipperId, @Param("date") Date date);

        // // Lấy đơn hàng tại quầy, lọc theo ngày bán
        // @Query("SELECT o FROM Order o WHERE o.orderType = 'Offline' "
        // + "AND (:fromDate IS NULL OR o.createDate >= :fromDate) "
        // + "AND (:toDate IS NULL OR o.createDate <= :toDate)")
        // Page<Order> findPosOrders(LocalDate fromDate, LocalDate toDate, Pageable
        // pageable);

        // Lấy tất cả đơn hàng tại quầy (orderType = 'Offline')
        List<Order> findByOrderTypeIgnoreCase(String orderType);

        // Lấy đơn hàng tại quầy, lọc theo ngày bán và loại đơn hàng
        @Query("SELECT o FROM Order o WHERE o.orderType = :orderType "
                        + "AND (:fromDate IS NULL OR o.createDate >= :fromDate) "
                        + "AND (:toDate IS NULL OR o.createDate <= :toDate)")
        Page<Order> findPosOrders(@Param("orderType") String orderType,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        Pageable pageable);

        // thống kê đơn hàng hủy
        @Query(value = "SELECT COUNT(*) FROM orders WHERE status = N'Đã hủy' AND MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Long countCanceledOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // thống kê Đếm tổng số đơn hàng trong tháng/năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Long countTotalOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // Thống kê doanh thu theo năm
        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE YEAR(o.createDate) = :year")
        Double getTotalRevenueInYear(@Param("year") int year);

        // Thống kê doanh thu theo tháng trong năm
        @Query("SELECT MONTH(o.createDate) AS month, SUM(o.totalAmount) AS revenue " +
                        "FROM Order o WHERE YEAR(o.createDate) = :year " +
                        "GROUP BY MONTH(o.createDate) ORDER BY MONTH(o.createDate)")
        List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);

}
