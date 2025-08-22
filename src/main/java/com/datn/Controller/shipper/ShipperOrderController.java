package com.datn.Controller.shipper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.OrderService;
import com.datn.model.Order;
import com.datn.model.User;
import com.datn.utils.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Controller
@RequestMapping("/shipper")
public class ShipperOrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private AuthService authService;

    @GetMapping("/pending-orders")
    public String pendingOrders(Model model) {
        List<Order> confirmedOrders = orderService.getOrdersByStatus("Đã xác nhận");
        List<Order> deliveringOrders = orderService.getOrdersByStatus("Đang giao lại");

        List<Order> pendingOrders = new ArrayList<>();
        pendingOrders.addAll(confirmedOrders);
        pendingOrders.addAll(deliveringOrders);

        model.addAttribute("orders", pendingOrders);
        model.addAttribute("view", "shipper/pending-orders");
        return "shipper/layout";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            List<Order> orders = orderService.getOrdersByStatusAndShipper("Đang giao", shipper.getId());
            model.addAttribute("orders", orders);
        }
        model.addAttribute("view", "shipper/my-orders");
        return "shipper/layout";
    }

    @PostMapping("/receive")
    public String receiveOrder(@RequestParam("orderId") Long orderId) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            System.out.println("Shipper nhận đơn: " + orderId + " bởi " + shipper.getId());
            orderService.updateToDangGiao(orderId, shipper.getId());
        } else {
            System.out.println("Không thể nhận đơn vì không phải shipper. Role hiện tại: "
                    + (shipper != null ? shipper.getRole() : "null"));
        }
        return "redirect:/shipper/my-orders";
    }

    @GetMapping("/history")
    public String history(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        User shipper = authService.getUser();
        List<Order> historyOrders = new ArrayList<>();
        Double totalAmount = 0.0;
        org.springframework.data.domain.Page<Order> orderPage = null;

        if (shipper != null && shipper.getRole() == 2) {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                    size);
            if (date != null) {
                // Nếu lọc theo ngày, vẫn lấy toàn bộ không phân trang (có thể bổ sung sau)
                historyOrders = orderService.getOrdersByShipperAndDate(shipper.getId(), date);
                totalAmount = orderService.getTotalAmountByShipperAndDate(shipper.getId(), date);
                model.addAttribute("selectedDate", new SimpleDateFormat("yyyy-MM-dd").format(date));
                model.addAttribute("orders", historyOrders);
            } else if (month != null && year != null) {
                historyOrders = orderService.getOrdersByShipperAndMonthYear(shipper.getId(), month, year);
                totalAmount = orderService.getTotalAmountByShipperAndMonthYear(shipper.getId(), month, year);
                model.addAttribute("selectedMonth", month);
                model.addAttribute("selectedYear", year);
                model.addAttribute("orders", historyOrders);
            } else if (year != null) {
                historyOrders = orderService.getOrdersByShipperAndYear(shipper.getId(), year);
                totalAmount = orderService.getTotalAmountByShipperAndYear(shipper.getId(), year);
                model.addAttribute("selectedYear", year);
                model.addAttribute("orders", historyOrders);
            } else {
                orderPage = orderService.getOrdersByStatusAndShipper("Đã giao", shipper.getId(), pageable);
                totalAmount = orderService.getTotalCompletedOrdersAmount(shipper.getId());
                model.addAttribute("orders", orderPage.getContent());
                model.addAttribute("orderPage", orderPage);
            }
            model.addAttribute("total", totalAmount);
        }
        // Truyền danh sách năm cho dropdown
        List<Integer> years = orderService.getAvailableYearsForShipper(shipper != null ? shipper.getId() : null);
        model.addAttribute("years", years);
        model.addAttribute("view", "shipper/history");
        return "shipper/layout";
    }

    @PostMapping("/orders/complete/{orderId}")
    public String completeOrder(@PathVariable("orderId") Long orderId) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            orderService.updateToCompleted(orderId, shipper.getId());
        }
        return "redirect:/shipper/my-orders";
    }

    @GetMapping("/returned-orders")
    public String showReturnedOrders(Model model) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            List<Order> failedOrders = orderService.findFailedOrdersByShipper(shipper.getId());
            model.addAttribute("orders", failedOrders);
        }
        model.addAttribute("view", "shipper/returned-orders");
        return "shipper/layout";
    }

    @PostMapping("/orders/return/{orderId}")
    public String returnOrder(@PathVariable("orderId") Long orderId) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            orderService.updateStatus(orderId, "Đã xác nhận");
        }
        return "redirect:/shipper/my-orders";
    }

    @PostMapping("/orders/failed")
    public String failedDelivery(
            @RequestParam("orderId") Long orderId,
            @RequestParam("failureReason") String cancelReason,
            @RequestParam("failureDetails") String cancelDetails,
            RedirectAttributes redirectAttributes) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            orderService.cancelByShipper(orderId, shipper.getId(), cancelReason, cancelDetails);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái giao thất bại thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện thao tác này!");
        }
        return "redirect:/shipper/my-orders";
    }

    // @PostMapping("/orders/cancel")
    // public String cancelOrder(
    // @RequestParam("orderId") Long orderId,
    // @RequestParam("cancelReason") String cancelReason,
    // @RequestParam(value = "cancelDetails", required = false) String
    // cancelDetails,
    // RedirectAttributes redirectAttributes) {
    // User shipper = authService.getUser();
    // if (shipper != null && shipper.getRole() == 2) {
    // try {
    // orderService.cancelByShipper(orderId, shipper.getId(), cancelReason,
    // cancelDetails);
    // redirectAttributes.addFlashAttribute("message", "Hủy đơn hàng thành công!");
    // } catch (Exception e) {
    // redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy đơn hàng: " +
    // e.getMessage());
    // }
    // } else {
    // redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn
    // hàng!");
    // }
    // return "redirect:/shipper/my-orders";
    // ...existing code...
}