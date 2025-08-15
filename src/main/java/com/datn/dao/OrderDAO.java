package com.datn.dao;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Order;

public interface OrderDAO extends JpaRepository<Order, Long> {
        // Tìm kiếm đơn hàng POS theo mã đơn hàng, có phân trang, lọc ngày, loại đơn
        @Query("SELECT o FROM Order o WHERE o.orderType = :orderType "
                        + "AND (:fromDate IS NULL OR o.createDate >= :fromDate) "
                        + "AND (:toDate IS NULL OR o.createDate <= :toDate) "

                        + "AND o.orderCode LIKE %:orderCode%")
        Page<Order> searchPosOrdersByOrderCode(@Param("orderType") String orderType,
                 + "AND o.orderCode LIKE %:orderCode% "
                        + "ORDER BY o.createDate DESC")
        Page<Order> searchPosOrdersByOrderCode(
                        @Param("orderType") String orderType,

                        @Param("orderCode") String orderCode,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        Pageable pageable);

        List<Order> findByUserIdOrderByIdDesc(int userId);

        List<Order> findByStatus(String status);

        List<Order> findByStatusOrderByIdDesc(String status);

        List<Order> findByStatusAndShipperId(String status, int shipperId);

        @Query("SELECT o FROM Order o WHERE UPPER(o.orderCode) = UPPER(:orderCode)")
        Optional<Order> findByOrderCode(@Param("orderCode") String orderCode);

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
                        + "AND (:toDate IS NULL OR o.createDate <= :toDate) "
                        + "ORDER BY o.createDate DESC")
        Page<Order> findPosOrders(@Param("orderType") String orderType,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        Pageable pageable);

        // thống kê đơn hàng thành công theo tháng / năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE status = N'Đã giao' AND MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Long countCanceledOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // thống kê Đếm tổng số đơn hàng trong tháng/năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Long countTotalOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // Thống kê doanh thu theo năm (chỉ lấy đơn hàng Đã giao)
        @Query(value = "SELECT SUM(o.total_amount) FROM orders o " +
                        "WHERE YEAR(o.create_date) = :year AND o.status = N'Đã giao'", nativeQuery = true)
        Double getTotalRevenueInYear(@Param("year") int year);

        // Thống kê doanh thu theo tháng trong năm (chỉ lấy đơn hàng Đã giao)
        @Query(value = "SELECT MONTH(o.create_date) AS month, SUM(o.total_amount) AS revenue " +
                        "FROM orders o " +
                        "WHERE YEAR(o.create_date) = :year AND o.status = N'Đã giao' " +
                        "GROUP BY MONTH(o.create_date) " +
                        "ORDER BY MONTH(o.create_date)", nativeQuery = true)
        List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);


        // DAO query: Thống kê doanh thu theo ngày trong tháng/năm (chỉ lấy đơn hàng 'Đã
        // giao')
        @Query(value = "SELECT DAY(o.create_date) AS day, SUM(o.total_amount) AS revenue " +
                        "FROM orders o " +
                        "WHERE YEAR(o.create_date) = :year AND MONTH(o.create_date) = :month AND o.status = N'Đã giao' "
                        +
                        "GROUP BY DAY(o.create_date) " +
                        "ORDER BY DAY(o.create_date)", nativeQuery = true)
        List<Object[]> getDailyRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // Đếm đơn giao trong năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE status = N'Đã giao' AND YEAR(create_date) = :year", nativeQuery = true)
        Long countDeliveredOrdersByYear(@Param("year") int year);

        // Đếm tổng số đơn đặt hàng trong năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE YEAR(create_date) = :year", nativeQuery = true)
        Long countTotalOrdersByYear(@Param("year") int year);

        // Thống kê số lượng đơn đặt dịch vụ trong tháng/năm với trạng thái PAID
        @Query(value = "SELECT COUNT(*) FROM service_orders WHERE MONTH(confirmed_at) = :month AND YEAR(confirmed_at) = :year AND status = 'PAID'", nativeQuery = true)
        Long countTotalOrdersByMonthAndYear1(@Param("month") int month, @Param("year") int year);

        // Đếm đơn dịch vụ theo năm với trạng thái PAID
        @Query(value = "SELECT COUNT(*) FROM service_orders WHERE status = 'PAID' AND YEAR(confirmed_at) = :year", nativeQuery = true)
        Long countPaidOrdersByYear(@Param("year") int year);



}
