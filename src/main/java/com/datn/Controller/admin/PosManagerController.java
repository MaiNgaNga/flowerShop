package com.datn.Controller.admin;

import com.datn.model.Order;
import com.datn.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/pos")
public class PosManagerController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/index")

    public String listOrders(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        LocalDate from = (fromDate != null && !fromDate.isEmpty()) ? LocalDate.parse(fromDate) : null;
        LocalDate to = (toDate != null && !toDate.isEmpty()) ? LocalDate.parse(toDate) : null;

        Page<Order> orders;
        if (orderCode != null && !orderCode.isEmpty()) {
            orders = orderService.searchPosOrdersByOrderCode("Offline", orderCode, from, to, PageRequest.of(page, size));
        } else {
            orders = orderService.getPosOrdersByType("Offline", from, to, PageRequest.of(page, size));
        }
        model.addAttribute("orders", orders);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("orderCode", orderCode);

        // Bổ sung biến phân trang cho đồng bộ giao diện
        model.addAttribute("currentPage", orders.getNumber());
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("size", orders.getSize());
        model.addAttribute("tab", "list");

        // Nếu có orderId, truyền chi tiết đơn hàng
        if (orderId != null) {
            Order orderDetail = orderService.getOrderById(orderId);
            model.addAttribute("orderDetail", orderDetail);
        } else {
            model.addAttribute("orderDetail", null);
        }

        model.addAttribute("view", "admin/posCRUD");
        return "admin/layout";
    }

}