package com.datn.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Order;
import com.datn.model.OrderDetail;

public interface OrderService {
    Page<Order> findOrdersWithFilter(String status, String keyword, LocalDate fromDate, LocalDate toDate, Pageable pageable);
  Page<Order> getOrdersByStatusAndShipper(String status, int shipperId,
      Pageable pageable);

  List<Order> getOrdersByShipperAndMonthYear(int shipperId, int month, int year);

  Double getTotalAmountByShipperAndMonthYear(int shipperId, int month, int year);

  List<Order> getOrdersByShipperAndYear(int shipperId, int year);

  Double getTotalAmountByShipperAndYear(int shipperId, int year);

  List<Integer> getAvailableYearsForShipper(Integer shipperId);

  Order saveOrder(Order order, List<OrderDetail> orderDetails);

  Order findByID(Long id);

 Page<Order> findAllOrders(Pageable pageable);


  // Order saveOrder(Order order, List<OrderDetail> orderDetails);

  Order getOrderById(Long id);

  List<Order> getAllOrders();

  void deleteOrder(Long id);

  Order updateStatus(Long orderId, String status);


  List<Order> getOrdersByUser(int id);

  List<Order> findByStatus(String status);

  Double sumTotalAmountWhereStatusLike(String status);

  Double sumTotalAmountForCurrentMonth(String status);

  Double getAverageOrderValue(String status);

  Long countOrdersThisMonth(String status);

  Long getCountOrder(String status);

  List<Order> getOrdersByStatus(String status);

  Order updateToDangGiao(Long orderId, int shipperId);

  List<Order> getOrdersByStatusAndShipper(String status, int shipperId);

  List<Order> getHistoryOrders(int shipperId);

  List<Order> getOrdersByStatusAndShipper(List<String> statuses, int shipperId);

  void updateToCompleted(Long orderId, int shipperId);

  Order updateToReturned(Long orderId, int shipperId);

  List<Order> findReturnedOrdersByShipper(int shipperId);

  List<Order> findFailedOrdersByShipper(int shipperId);

  Order cancelByShipper(Long orderId, int shipperId, String cancelReason, String cancelDetails);
  // Order cancelByShipper(Long orderId, int shipperId, String cancelReason,
  // String cancelDetails);

  Double getTotalCompletedOrdersAmount(int shipperId);

  List<Order> getOrdersByShipperAndDate(int shipperId, Date date);

  Double getTotalAmountByShipperAndDate(int shipperId, Date date);

  Long countCancelledOrdersByMonthAndYear(int month, int year);

  Long getTotalOrdersInMonth(int month, int year);

  Double getTotalRevenueInYear(int year);

  Map<Integer, Double> getMonthlyRevenueByYear(int year);

  List<Order> getAllOfflineOrders();

  Page<Order> getPosOrdersByType(String orderType, LocalDate fromDate, LocalDate toDate, Pageable pageable);

  // Tìm kiếm đơn hàng POS theo mã đơn hàng, có phân trang, lọc ngày, loại đơn
  Page<Order> searchPosOrdersByOrderCode(String orderType, String orderCode, LocalDate fromDate, LocalDate toDate,
      Pageable pageable);

  Order recreateOrder(Long canceledOrderId); // Thêm phương thức tạo lại đơn hàng

  Long getTotalOrdersInYear(int year);

  Long countDeliveredOrdersByYear(int year);

  // Page<Order> getPosOrdersByType(String orderType, LocalDate fromDate,
  // LocalDate toDate, Pageable pageable);

  // // Tìm kiếm đơn hàng POS theo mã đơn hàng, có phân trang, lọc ngày, loại đơn
  // Page<Order> searchPosOrdersByOrderCode(String orderType, String orderCode,
  // LocalDate fromDate, LocalDate toDate,
  // Pageable pageable);

  // Thống kê số lượng đơn đặt dịch vụ trong tháng/năm với trạng thái PAID
  Long countTotalOrdersByMonthAndYear1(int month, int year);

  // Đếm số đơn dịch vụ theo năm với trạng thái PAID
  Long countPaidOrdersByYear(int year);

  // Service interface: Thống kê doanh thu theo ngày trong tháng/năm (chỉ lấy đơn
  // hàng 'Hoàn tất')
  Map<Integer, Double> getDailyRevenueByMonthAndYear(int month, int year);

 // 📦 Đơn cần giao hôm nay
     long getOrdersToDeliverToday() ;

    // 📅 Đơn sắp giao trong 3 ngày tới
     long getOrdersNext3Days();

    // 🚚 Đơn giao thất bại
     long getFailedOrders() ;
    // ✅ Đơn đã hoàn tất hôm nay
     long getCompletedOrdersToday() ;

     long newOrders();
}