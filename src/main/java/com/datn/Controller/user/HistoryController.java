package com.datn.Controller.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.CommentService;
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

    //Comment 
    @Autowired
    CommentService commentService;

    // Hiển thị trang lịch sử đơn hàng của người dùng
    @GetMapping("/history")
    public String getHistory(Model model, @RequestParam(value = "id", required = false) Long id) {
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

    // Xử lý khi người dùng gửi bình luận và đánh giá sao
    @PostMapping("/history/comment")
    public String comment(Model model,
                          @RequestParam("comment") String content,
                          @RequestParam("orderId") Long orderId,
                          @RequestParam("rating") Integer rating,
                          RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra nội dung bình luận rỗng
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nội dung bình luận không được để trống.");
            }
            // Kiểm tra sản phẩm có tồn tại hay không
            else if (orderService.findByID(orderId) == null) {
                redirectAttributes.addFlashAttribute("error", "đơn hàng k tồn tại");
            }
            // Nếu hợp lệ thì lưu bình luận
            else {
                
                commentService.saveComment(content, orderId, rating);
                redirectAttributes.addFlashAttribute("message", "Cảm ơn bạn đã đánh giá sản phẩm.");
            }
        } catch (Exception e) {
            // Nếu có lỗi trong quá trình xử lý thì thông báo lỗi
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        // Sau khi xử lý xong thì chuyển hướng lại trang lịch sử mua hàng
        return "redirect:/history?id=" + orderId;
    }

    @GetMapping("/history/comment/delete/{id}")
    public String deleteComment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa bình luận thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return "redirect:/history";
    }

}
