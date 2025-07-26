package com.datn.Controller.shipper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.OrderService;
import com.datn.model.Order;
import com.datn.model.User;
import com.datn.utils.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;


import java.text.SimpleDateFormat;
import java.util.List;
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
        List<Order> pendingOrders = orderService.getOrdersByStatus("Đã xác nhận");
        model.addAttribute("orders", pendingOrders);
        model.addAttribute("view", "shipper/pending-orders");
        return "shipper/layout";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            // Lấy đơn hàng đang giao của shipper
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

    // Mapping("/history")
    // tring history(Model model) {
    // shipper = authService.getUser();
    //
    // shipper != null && shipper.getRole() == 2) {
    // // Gọi phương thức để lấy các đơn hàng cho shipper
    // List<Order> historyOrders = orderService.getOrdersByStatusAndShipper("Đã
    // giao", shipper.getId());
    // Double total = orderService.getTotalCompletedOrdersAmount(shipper.getId());
    // em.out.println("Completed orders: " + historyOrders.size());
    //
    // System.out.println("Completed total amount: " + total);
    // for (Order o : historyOrders) {
    // System.out.println("Order ID: " + o.getId() + ", TotalAmount: " +
    // o.getTotalAmount());
    // }

    // // Thêm vào model để hiển thị trên giao diện
    // model.addAttribute("orders", historyOrders);
    // model.addAttribute("total", total);
    // }
    //

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
            // Cập nhật trạng thái đơn hàng là "Đã giao"
            orderService.updateToCompleted(orderId, shipper.getId());
        }
        return "redirect:/shipper/my-orders"; // Sau khi cập nhật, chuyển đến trang đơn hàng của shipper
    }

    @GetMapping("/returned-orders")
    public String showReturnedOrders(Model model) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            List<Order> returnedOrders = orderService.findReturnedOrdersByShipper(shipper.getId());
            model.addAttribute("orders", returnedOrders);

        }
        model.addAttribute("view", "shipper/returned-orders");
        return "shipper/layout";
    }

    @PostMapping("/orders/return/{orderId}")
    public String returnOrder(@PathVariable("orderId") Long orderId) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            // Cập nhật trạng thái đơn hàng về "chờ giao"
            orderService.updateToReturned(orderId, shipper.getId());
        }
        // Sau khi hoàn hàng, trả về danh sách đơn hàng mới
        return "redirect:/shipper/returned-orders"; // Tái nạp lại danh sách đơn hàng
    }

    @PostMapping("/orders/cancel/{orderId}")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        User shipper = authService.getUser();
        if (shipper != null && shipper.getRole() == 2) {
            orderService.cancelByShipper(orderId, shipper.getId());
        }
        return "redirect:/shipper/my-orders";
    }

}
