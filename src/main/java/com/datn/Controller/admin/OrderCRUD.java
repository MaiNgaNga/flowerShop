package com.datn.Controller.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.OrderService;
import com.datn.dao.ProductCategoryDAO;
import com.datn.model.Order;
import com.datn.model.ProductCategory;

@Controller
@RequestMapping("/orderAdmin")
public class OrderCRUD {

    @Autowired
    ProductCategoryDAO pro_ca_dao;

    @Autowired
    OrderService orderService;

    @GetMapping
    public String index(Model model,
            @RequestParam(value = "orderStatus", required = false, defaultValue = "Chưa xác nhận") String orderStatus) {
        List<ProductCategory> productCategories = pro_ca_dao.findAll();
        model.addAttribute("productCategories", productCategories);
        model.addAttribute("orderStatus", orderStatus);
        List<Order> orders = orderService.findByStatus(orderStatus);
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("view", "admin/order");
        return "admin/layout";
    }

    @PostMapping("/update/{orderId}")
    public String checkout(@PathVariable("orderId") Long orderId, @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            switch (status) {
                case "Chờ giao":
                    if (order.getStatus().equals("Chưa xác nhận")) {
                        orderService.updateStatus(orderId, "Đã xác nhận");
                        redirectAttributes.addFlashAttribute("toastSuccess", "Xác nhận đơn hàng thành công!");
                    }
                    break;
                case "Đã xác nhận":
                    if (order.getStatus().equals("Đã xác nhận")) {
                        orderService.updateStatus(orderId, "Đã giao");
                        redirectAttributes.addFlashAttribute("toastSuccess", "Cập nhật trạng thái thành công!");
                    }
                    break;
                default:
                    break;
            }
        }
        String encodedStatus = URLEncoder.encode(order.getStatus(), StandardCharsets.UTF_8);
        return "redirect:/orderAdmin?orderStatus=" + encodedStatus;
    }

    @PostMapping("/recreate/{orderId}")
    public String recreateOrder(@PathVariable("orderId") Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderService.recreateOrder(orderId);
            redirectAttributes.addFlashAttribute("toastSuccess", "Tạo lại đơn hàng thành công!");
            String encodedStatus = URLEncoder.encode("Đang giao lại", StandardCharsets.UTF_8);
            return "redirect:/orderAdmin?orderStatus=" + encodedStatus;
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("toastSuccess", e.getMessage());
            String encodedStatus = URLEncoder.encode("Đã hủy", StandardCharsets.UTF_8);
            return "redirect:/orderAdmin?orderStatus=" + encodedStatus;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastSuccess", "Đơn hàng không tồn tại!");
            String encodedStatus = URLEncoder.encode("Đã hủy", StandardCharsets.UTF_8);
            return "redirect:/orderAdmin?orderStatus=" + encodedStatus;
        }
    }
}