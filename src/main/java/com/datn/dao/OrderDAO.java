package com.datn.dao;

import java.util.List;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Order;

public interface OrderDAO extends JpaRepository<Order, Long> {
  List<Order> findByUserIdOrderByIdDesc(int userId);

  List<Order> findByStatusOrderByIdDesc(String status);

  List<Order> findByStatusAndShipperId(String status, int shipperId);

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
  "AND CAST(create_date AS DATE) = :date", nativeQuery = true)
  Double getTotalCompletedAmountByShipperIdAndDateNative(@Param("shipperId")
  int shipperId,
  @Param("date") Date date);

  @Query(value = "SELECT * FROM orders o WHERE o.shipper_id = :shipperId AND o.status = N'Đã giao' AND CONVERT(date, o.create_date) = :date", nativeQuery = true)
  List<Order> getOrdersByShipperAndDate(@Param("shipperId") int shipperId, @Param("date") Date date);


}
