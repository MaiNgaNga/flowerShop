package com.datn.Controller.user;

// Các import cần thiết cho controller
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.CommentService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
public class DetailController {

    // Inject các service cần thiết
    @Autowired
    ProductCategoryService pro_ca_Service;

    @Autowired
    ProductService productService;

    @Autowired
    CommentService commentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    // Hiển thị trang chi tiết sản phẩm
    @RequestMapping("/detail")
    public String index(Model model,
                        @RequestParam(value = "id", required = false) Long id,
                        RedirectAttributes redirectAttributes) {
        int cartCount = 0;

        // Lấy người dùng hiện tại (nếu đã đăng nhập)
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            // Đếm số lượng sản phẩm trong giỏ hàng
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);

        // Lấy danh sách bình luận cho sản phẩm
        // model.addAttribute("comments", commentService.getCommentsByProduct(id));

        // Lấy thông tin sản phẩm theo ID
        model.addAttribute("product", productService.findByID(id));

        // Lấy tất cả danh mục sản phẩm
        model.addAttribute("productCategories", pro_ca_Service.findAll());

        // Gợi ý các sản phẩm tương tự dựa theo category của sản phẩm hiện tại
        model.addAttribute("productSimilar",
                productService.findProductByCategory(productService.findByID(id).getCategory().getId()));

        // Nếu sản phẩm không tồn tại, thông báo lỗi và chuyển hướng về trang sản phẩm
        if (productService.findByID(id) == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/ProductUser?id=1";
        }

        // Trả về view "detail" trong layout chung
        model.addAttribute("view", "detail");
        return "layouts/layout";
    }

    // Xử lý khi người dùng gửi bình luận và đánh giá sao
    // @PostMapping("/detail/comment")
    // public String comment(Model model,
    //                       @RequestParam("comment") String content,
    //                       @RequestParam("orderId") Long orderId,
    //                       @RequestParam("rating") Integer rating,
    //                       RedirectAttributes redirectAttributes) {
    //     try {
    //         // Kiểm tra nội dung bình luận rỗng
    //         if (content == null || content.trim().isEmpty()) {
    //             redirectAttributes.addFlashAttribute("error", "Nội dung bình luận không được để trống.");
    //         }
    //         // Kiểm tra sản phẩm có tồn tại hay không
    //         else if (productService.findByID(orderId) == null) {
    //             redirectAttributes.addFlashAttribute("error", "đơn hàng k tồn tại");
    //         }
    //         // Nếu hợp lệ thì lưu bình luận
    //         else {
                
    //             commentService.saveComment(content, orderId, rating);
    //         }
    //     } catch (Exception e) {
    //         // Nếu có lỗi trong quá trình xử lý thì thông báo lỗi
    //         redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
    //         e.printStackTrace();
    //         System.out.println(e.getMessage());
    //     }

    //     // Sau khi xử lý xong thì chuyển hướng lại trang lịch sử mua hàng
    //     return "redirect:/history?id=" + orderId;
    // }
}
