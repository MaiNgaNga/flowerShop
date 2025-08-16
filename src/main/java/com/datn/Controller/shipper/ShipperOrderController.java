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
    public String history(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            Model model) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            List<Order> historyOrders;
            Double totalAmount;

            if (date != null) {
                historyOrders = orderService.getOrdersByShipperAndDate(shipper.getId(), date);
                totalAmount = orderService.getTotalAmountByShipperAndDate(shipper.getId(), date);
                model.addAttribute("selectedDate", new SimpleDateFormat("yyyy-MM-dd").format(date));

                System.out.println("History orders: " + historyOrders);
                System.out.println("Total amount: " + totalAmount);
            } else {
                historyOrders = orderService.getOrdersByStatusAndShipper("Đã giao", shipper.getId());
                totalAmount = orderService.getTotalCompletedOrdersAmount(shipper.getId());
            }

            model.addAttribute("orders", historyOrders);
            model.addAttribute("total", totalAmount);
        }
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
            @RequestParam("failureReason") String failureReason,
            @RequestParam("failureDetails") String failureDetails,
            RedirectAttributes redirectAttributes) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            // Set trạng thái đơn hàng là 'Giao thất bại' và lưu lý do
            orderService.updateStatus(orderId, "Giao thất bại");
            // Nếu muốn lưu lý do chi tiết, cần bổ sung vào model Order và Service
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