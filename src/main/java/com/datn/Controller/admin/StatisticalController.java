package com.datn.Controller.admin;

import com.datn.Service.OrderService;
import com.datn.Service.ProductService;
import com.datn.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class StatisticalController {

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

        // Lấy tổng doanh thu trong năm
        Double totalRevenue = orderService.getTotalRevenueInYear(currentYear);

        // Đưa dữ liệu ra view
        model.addAttribute("canceledOrders", canceledOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);
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

    @GetMapping("/api/top-products")
    @ResponseBody
    public List<Map<String, Object>> getTopProductsByYear(
            @RequestParam(name = "year", required = false) Integer year) {
        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        return productService.getTop6SellingProductsByYear(currentYear);
    }

}
