package com.datn.Controller.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

    // Inject service xử lý đơn hàng

    @Autowired
    OrderService orderService;

    // Hiển thị danh sách đơn hàng theo trạng thái (mặc định là "Chờ xác nhận")
    @GetMapping
    public String orderAdmin(
        Model model,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String orderStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "8") int size
) {
    if (orderStatus != null && orderStatus.isBlank()) {
        orderStatus = null; // chuyển "" thành null
    }
    Pageable pageable = PageRequest.of(page, size);

    // Lấy danh sách đơn hàng theo filter từ repository (query with fromDate/toDate)
    Page<Order> orders = orderService.findOrdersWithFilter(orderStatus, keyword, fromDate, toDate, pageable);

    // Lấy tất cả danh mục sản phẩm
    List<ProductCategory> productCategories = pro_ca_dao.findAll();

    // Add vào model để Thymeleaf giữ trạng thái
    model.addAttribute("orders", orders);
    model.addAttribute("totalOrders", orders.getTotalElements());
    model.addAttribute("orderStatus", orderStatus);
    model.addAttribute("keyword", keyword);
    model.addAttribute("fromDate", fromDate);
    model.addAttribute("toDate", toDate);
    model.addAttribute("page", page);
    model.addAttribute("size", size);
    model.addAttribute("productCategories", productCategories);

    model.addAttribute("currentPage", orders.getNumber());
    model.addAttribute("totalPages", orders.getTotalPages());
    model.addAttribute("hasPrevious", orders.hasPrevious());
    model.addAttribute("hasNext", orders.hasNext());
    // View chính để layout include fragment
    model.addAttribute("view", "admin/order"); // fragment admin/order.html

    return "admin/layout"; // layout chung
}


@PostMapping("/update/{orderId}")
public String checkout(@PathVariable("orderId") Long orderId,
                       @RequestParam("status") String status,
                       RedirectAttributes redirectAttributes) {
    Order order = orderService.getOrderById(orderId);
    if (order == null) {
        redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
        return "redirect:/orderAdmin";
    }

    switch (status) {
        case "Chờ giao":
            if (order.getStatus().equals("Chờ xác nhận")) {
                orderService.updateStatus(orderId, "Chờ giao");
                redirectAttributes.addFlashAttribute("success", "Xác nhận đơn hàng "+ orderId+" thành công!");
            }
            break;
        case "Hủy đơn":
                orderService.updateStatus(orderId, "Đã hủy");
                redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng "+ orderId+" !");
            break;
        case "Hoàn tất":
            if (order.getStatus().equals("Chờ giao")) {
                orderService.updateStatus(orderId, "Hoàn tất");
                redirectAttributes.addFlashAttribute("success", "Đơn hàng "+ orderId+" đã được giao!");
            }
             if (order.getStatus().equals("Giao thất bại")) {
                orderService.updateStatus(orderId, "Hoàn tất");
                redirectAttributes.addFlashAttribute("success", "Đơn hàng "+ orderId+" đã hoàn tất!");
            }
            break;
        default:
            break;
    }

    String encodedStatus = URLEncoder.encode(order.getStatus(), StandardCharsets.UTF_8);
    return "redirect:/orderAdmin?orderStatus=" + encodedStatus;
}

@PostMapping("/recreate/{orderId}")
public String recreateOrder(@PathVariable("orderId") Long orderId, RedirectAttributes redirectAttributes) {
    try {
        orderService.recreateOrder(orderId);
        redirectAttributes.addFlashAttribute("success", "Tạo lại đơn hàng thành công!");
        String encodedStatus = URLEncoder.encode("Giao lại", StandardCharsets.UTF_8);
        return "redirect:/orderAdmin?orderStatus=" + encodedStatus;
    } catch (IllegalStateException e) {
        redirectAttributes.addFlashAttribute("toastError", e.getMessage());
        return "redirect:/orderAdmin?orderStatus=Giao thất bại";
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
        return "redirect:/orderAdmin?orderStatus=Giao thất bại";
    }
}
}