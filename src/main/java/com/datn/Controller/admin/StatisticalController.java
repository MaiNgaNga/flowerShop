package com.datn.Controller.admin;

import com.datn.Service.OrderService;
import com.datn.Service.ProductService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class StatisticalController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticalController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/statistical")
    public String index(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            Model model) {

        // Mặc định lấy tháng/năm hiện tại nếu không được truyền từ giao diện
        LocalDate now = LocalDate.now();
        int currentMonth = (month != null) ? month : now.getMonthValue();
        int currentYear = (year != null) ? year : now.getYear();

        // Lấy số lượng đơn hủy trong tháng
        Long canceledOrders = orderService.countCancelledOrdersByMonthAndYear(currentMonth, currentYear);

        // Lấy tổng số đơn hàng trong tháng
        Long totalOrders = orderService.getTotalOrdersInMonth(currentMonth, currentYear);

        // Lấy tổng số đơn hàng trong năm
        Long totalOrdersInYear = orderService.getTotalOrdersInYear(currentYear);

        // Lấy số lượng đơn đã giao trong năm
        Long deliveredOrdersInYear = orderService.countDeliveredOrdersByYear(currentYear);

        // Lấy tổng doanh thu trong năm
        Double totalRevenue = orderService.getTotalRevenueInYear(currentYear);

        // Lấy tổng doanh thu trong tháng theo năm
        Double monthlyRevenue = orderService.getMonthlyRevenueByYear(currentYear).getOrDefault(currentMonth, 0.0);

        // Lấy tổng số đơn dịch vụ trong tháng với trạng thái PAID
        Long totalOrdersByMonth = orderService.countTotalOrdersByMonthAndYear1(currentMonth, currentYear);

        // Lấy tổng số đơn dịch vụ trong năm với trạng thái PAID
        Long totalOrdersInYear1 = orderService.countPaidOrdersByYear(currentYear);

        // Đưa dữ liệu ra view
        model.addAttribute("canceledOrders", canceledOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalOrdersInYear", totalOrdersInYear);
        model.addAttribute("deliveredOrdersInYear", deliveredOrdersInYear);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);
        model.addAttribute("totalOrdersByMonth", totalOrdersByMonth);
        model.addAttribute("totalOrdersInYear1", totalOrdersInYear1);
        model.addAttribute("view", "admin/statistical");

        return "admin/layout";
    }

    // API: Doanh thu theo tháng trong năm
    @GetMapping("/api/revenue-by-month")
    @ResponseBody
    public Map<Integer, Double> getMonthlyRevenue(
            @RequestParam(name = "year", required = false) Integer year) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        return orderService.getMonthlyRevenueByYear(currentYear);
    }

    // API: Doanh thu theo ngày trong tháng/năm
    @GetMapping("/api/revenue-by-day")
    @ResponseBody
    public Map<Integer, Double> getDailyRevenue(
            @RequestParam(name = "month", required = true) int month,
            @RequestParam(name = "year", required = false) Integer year) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        return orderService.getDailyRevenueByMonthAndYear(month, currentYear);
    }

    // API: Top 6 sản phẩm bán chạy theo năm
    @GetMapping("/api/top-products")
    @ResponseBody
    public List<Map<String, Object>> getTopProductsByYear(
            @RequestParam(name = "year", required = false) Integer year) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        return productService.getTop6SellingProductsByYear(currentYear);
    }

    // API: Top 6 danh mục bán chạy theo tháng/năm
    @GetMapping("/api/top-productsMonth")
    @ResponseBody
    public List<Map<String, Object>> getTopProductsByYearAndMonth(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month) {
        logger.info("Lấy top danh mục cho năm: {}, tháng: {}", year, month);
        List<Map<String, Object>> result = productService.getTop6SellingProductsByYearAndMonth(year, month);
        logger.info("Kết quả top danh mục: {}", result);
        return result;
    }

}