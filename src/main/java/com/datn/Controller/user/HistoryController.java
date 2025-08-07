package com.datn.Controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.datn.Service.OrderDetailService;
import com.datn.Service.OrderService;
import com.datn.dao.ProductCategoryDAO;
import com.datn.model.ProductCategory;
import com.datn.model.User;
import com.datn.utils.AuthService;

@Controller
public class HistoryController {

    // Inject DAO để lấy danh sách danh mục sản phẩm
    @Autowired
    ProductCategoryDAO pro_ca_dao;

    // Inject service quản lý đơn hàng
    @Autowired
    OrderService orderService;

    // Inject service xác thực người dùng
    @Autowired
    AuthService authService;

    // Inject service chi tiết đơn hàng
    @Autowired
    OrderDetailService orderDetailService;

    // Hiển thị trang lịch sử đơn hàng của người dùng
    @GetMapping("/history")
    public String getHistory(Model model) {
        return showForm(model); // Gọi hàm hiển thị trang lịch sử đơn hàng
    }

    // Hủy đơn hàng theo orderId
    @GetMapping("/history/delete/{orderId}")
    public String deleteOrder(Model model, @PathVariable("orderId") Long orderId) {
        orderService.updateStatus(orderId, "Đã hủy"); // Cập nhật trạng thái đơn hàng thành "Đã hủy"
        model.addAttribute("message", "Đã hủy đơn hàng"); // Gửi thông báo thành công về view
        return showForm(model); // Hiển thị lại danh sách đơn hàng sau khi hủy
    }

    // Hàm hiển thị form với dữ liệu cần thiết
    public String showForm(Model model) {
        User user = authService.getUser(); // Lấy thông tin người dùng hiện tại
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId())); // Lấy danh sách đơn hàng của người dùng

        List<ProductCategory> productCategories = pro_ca_dao.findAll(); // Lấy danh sách danh mục sản phẩm
        model.addAttribute("productCategories", productCategories);

        model.addAttribute("view", "history"); // Đặt biến view để render layout phù hợp

        return "layouts/layout"; // Trả về layout tổng
    }

}
