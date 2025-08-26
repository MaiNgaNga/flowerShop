package com.datn.dao;

import java.util.List;
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
        Page<Order> findByStatusAndShipperIdOrderByDeliveryDateDesc(String status, int shipperId,
                        org.springframework.data.domain.Pageable pageable);

        @Query(value = "SELECT * FROM orders WHERE shipper_id = :shipperId AND status = N'Đã giao' AND MONTH(create_date) = :month AND YEAR(create_date) = :year ORDER BY delivery_date DESC", nativeQuery = true)
        List<Order> getOrdersByShipperAndMonthYear(@Param("shipperId") int shipperId, @Param("month") int month,
                        @Param("year") int year);

        @Query(value = "SELECT SUM(total_amount) FROM orders WHERE shipper_id = :shipperId AND status = N'Đã giao' AND MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Double getTotalAmountByShipperAndMonthYear(@Param("shipperId") int shipperId, @Param("month") int month,
                        @Param("year") int year);

        @Query(value = "SELECT * FROM orders WHERE shipper_id = :shipperId AND status = N'Đã giao' AND YEAR(create_date) = :year ORDER BY delivery_date DESC", nativeQuery = true)
        List<Order> getOrdersByShipperAndYear(@Param("shipperId") int shipperId, @Param("year") int year);

        @Query(value = "SELECT SUM(total_amount) FROM orders WHERE shipper_id = :shipperId AND status = N'Đã giao' AND YEAR(create_date) = :year", nativeQuery = true)
        Double getTotalAmountByShipperAndYear(@Param("shipperId") int shipperId, @Param("year") int year);

        @Query(value = "SELECT DISTINCT YEAR(create_date) FROM orders WHERE shipper_id = :shipperId AND status = N'Đã giao' ORDER BY YEAR(create_date) DESC", nativeQuery = true)
        List<Integer> getAvailableYearsForShipper(@Param("shipperId") Integer shipperId);

        // Tìm kiếm đơn hàng POS theo mã đơn hàng, có phân trang, lọc ngày, loại đơn
        @Query(value = "SELECT * FROM orders o WHERE o.order_type = :orderType "
                        + "AND (:fromDate IS NULL OR o.create_date >= :fromDate) "
                        + "AND (:toDate IS NULL OR o.create_date <= :toDate) "
                        + "AND o.order_code LIKE CONCAT('%', :orderCode, '%') "
                        + "ORDER BY o.create_date DESC", nativeQuery = true)
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

        @Query(value = "SELECT * FROM orders o WHERE o.shipper_id = :shipperId AND o.status = N'Đã giao' AND CONVERT(date, o.delivery_date) = :date ORDER BY delivery_date DESC", nativeQuery = true)
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
        @Query(value = "SELECT * FROM orders o WHERE o.order_type = :orderType "
                        + "AND (:fromDate IS NULL OR o.create_date >= :fromDate) "
                        + "AND (:toDate IS NULL OR o.create_date <= :toDate) "
                        + "ORDER BY o.create_date DESC", nativeQuery = true)
        Page<Order> findPosOrders(@Param("orderType") String orderType,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        Pageable pageable);

        // thống kê đơn hàng thành công theo tháng / năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE status = N'Hoàn tất' AND MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Long countCanceledOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // thống kê Đếm tổng số đơn hàng trong tháng/năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE MONTH(create_date) = :month AND YEAR(create_date) = :year", nativeQuery = true)
        Long countTotalOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

        // Thống kê doanh thu trong năm (bao gồm cả đơn hàng Đã giao và dịch vụ đã thanh
        @Query(value = "SELECT SUM(revenue) AS total_revenue " +
                        "FROM ( " +
                        "    SELECT SUM(o.total_amount) AS revenue " +
                        "    FROM orders o " +
                        "    WHERE YEAR(o.create_date) = :year AND (o.status = N'Hoàn tất' ) "
                        +
                        "    UNION ALL " +
                        "    SELECT SUM(s.quoted_price) AS revenue " +
                        "    FROM service_orders s " +
                        "    WHERE YEAR(s.confirmed_at) = :year AND s.status = 'paid' " +
                        ") AS combined", nativeQuery = true)
        Double getTotalRevenueInYear(@Param("year") int year);

        // Thống kê doanh thu theo tháng trong năm (bao gồm cả đơn hàng Đã giao và dịch
        @Query(value = "SELECT m.month, COALESCE(SUM(derived.revenue), 0) AS total_revenue " +
                        "FROM (SELECT 1 AS month UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 " +
                        "      UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 " +
                        "      UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) m " +
                        "LEFT JOIN ( " +
                        "    SELECT MONTH(o.create_date) AS month, SUM(o.total_amount) AS revenue " +
                        "    FROM orders o " +
                        "    WHERE YEAR(o.create_date) = :year AND (o.status = N'Hoàn tất' ) "
                        +
                        "    GROUP BY MONTH(o.create_date) " +
                        "    UNION ALL " +
                        "    SELECT MONTH(s.confirmed_at) AS month, SUM(s.quoted_price) AS revenue " +
                        "    FROM service_orders s " +
                        "    WHERE YEAR(s.confirmed_at) = :year AND s.status = 'paid' " +
                        "    GROUP BY MONTH(s.confirmed_at) " +
                        ") derived ON m.month = derived.month " +
                        "GROUP BY m.month " +
                        "ORDER BY m.month", nativeQuery = true)
        List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);
        // @Query(value = "SELECT MONTH(o.create_date) AS month, SUM(o.total_amount) AS
        // revenue " +
        // "FROM orders o " +
        // "WHERE YEAR(o.create_date) = :year AND o.status = N'Đã giao' " +
        // "GROUP BY MONTH(o.create_date) " +
        // "ORDER BY MONTH(o.create_date)", nativeQuery = true)
        // List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);

        // Thống kê doanh thu theo ngày trong tháng/năm (bao gồm cả đơn hàng Đã giao và
        // dịch vụ đã thanh toán)
        @Query(value = "SELECT DAY(derived.create_date) AS day, COALESCE(SUM(derived.revenue), 0) AS total_revenue " +
                        "FROM ( " +
                        "    SELECT o.create_date AS create_date, SUM(o.total_amount) AS revenue " +
                        "    FROM orders o " +
                        "    WHERE YEAR(o.create_date) = :year AND MONTH(o.create_date) = :month AND (o.status = N'Hoàn tất' ) "
                        +
                        "    GROUP BY o.create_date " +
                        "    UNION ALL " +
                        "    SELECT s.confirmed_at AS create_date, SUM(s.quoted_price) AS revenue " +
                        "    FROM service_orders s " +
                        "    WHERE YEAR(s.confirmed_at) = :year AND MONTH(s.confirmed_at) = :month AND s.status = 'paid' "
                        +
                        "    GROUP BY s.confirmed_at " +
                        ") AS derived " +
                        "GROUP BY DAY(derived.create_date) " +
                        "ORDER BY DAY(derived.create_date)", nativeQuery = true)
        List<Object[]> getDailyRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);
        // @Query(value = "SELECT DAY(o.create_date) AS day, SUM(o.total_amount) AS
        // revenue " +
        // "FROM orders o " +
        // "WHERE YEAR(o.create_date) = :year AND MONTH(o.create_date) = :month AND
        // o.status = N'Đã giao' "
        // +
        // "GROUP BY DAY(o.create_date) " +
        // "ORDER BY DAY(o.create_date)", nativeQuery = true)
        // List<Object[]> getDailyRevenueByMonthAndYear(@Param("month") int month,
        // @Param("year") int year);

        // Đếm đơn giao trong năm
        @Query(value = "SELECT COUNT(*) FROM orders WHERE status = N'Hoàn tất' AND YEAR(create_date) = :year", nativeQuery = true)
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

        // Lọc đơn hàng online theo status, keyword (user.name, receiverName, receiverPhone, sdt) và tháng/năm

    @Query("SELECT o FROM Order o " +
           "WHERE o.orderType = 'Online' " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:keyword IS NULL OR " +
           "      LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "      LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "      LOWER(o.receiverPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "      LOWER(STR(o.id)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "      LOWER(o.sdt) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           ") " +
           "AND (:fromDate IS NULL OR o.deliveryDate >= :fromDate) " +
           "AND (:toDate IS NULL OR o.deliveryDate <= :toDate) " +
           "ORDER BY o.id DESC")
    Page<Order> findOrdersWithFilter(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

}




