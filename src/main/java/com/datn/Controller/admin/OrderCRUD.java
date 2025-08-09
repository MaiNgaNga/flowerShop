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

    // Inject service xử lý đơn hàng

    @Autowired
    OrderService orderService;

    // Hiển thị danh sách đơn hàng theo trạng thái (mặc định là "Chưa xác nhận")
    @GetMapping
    public String index(Model model,
        @RequestParam(value = "orderStatus", required = false, defaultValue = "Chưa xác nhận") String orderStatus) {
        
        // Lấy tất cả danh mục sản phẩm để hiển thị trên giao diện
        List<ProductCategory> productCategories = pro_ca_dao.findAll();
        model.addAttribute("productCategories", productCategories);

        // Gửi trạng thái đang chọn về giao diện
        model.addAttribute("orderStatus", orderStatus);

        // Lấy danh sách đơn hàng theo trạng thái
        List<Order> orders = orderService.findByStatus(orderStatus);
        model.addAttribute("orders", orders);

        // Tổng số đơn hàng được tìm thấy
        model.addAttribute("totalOrders", orders.size());

        // Thiết lập view cho layout
        model.addAttribute("view", "admin/order");

        // Trả về layout chung
        return "admin/layout";
    }

    // Cập nhật trạng thái đơn hàng từ admin
    @PostMapping("/update/{orderId}")

    public String checkout(
        @PathVariable("orderId") Long orderId, 
        @RequestParam("status") String status,
        RedirectAttributes redirectAttributes) {

        // Lấy đơn hàng theo ID

        Order order = orderService.getOrderById(orderId);
        
        if (order != null) {
            // Dựa vào trạng thái đầu vào để cập nhật
            switch (status) {
                case "Chờ giao":
                    // Nếu đơn hàng đang ở trạng thái "Chưa xác nhận", cho phép cập nhật sang "Đã xác nhận"
                    if (order.getStatus().equals("Chưa xác nhận")) {
                        orderService.updateStatus(orderId, "Đã xác nhận");


                        // Thêm thông báo flash để hiển thị sau redirect

                        redirectAttributes.addFlashAttribute("toastSuccess", "Xác nhận đơn hàng thành công!");
                    }
                    break;
                case "Đã xác nhận":
                    // Nếu đơn hàng đã được xác nhận, cập nhật thành "Đã giao"
                    if (order.getStatus().equals("Đã xác nhận")) {
                        orderService.updateStatus(orderId, "Đã giao");
                        redirectAttributes.addFlashAttribute("toastSuccess", "Cập nhật trạng thái thành công!");
                    }
                    break;
                default:
                    // Trường hợp khác không xử lý
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

