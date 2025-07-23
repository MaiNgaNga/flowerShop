package com.datn.Controller.user;

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

    @RequestMapping("/detail")
    public String index(Model model, @RequestParam(value = "id", required = false) Long id,
            RedirectAttributes redirectAttributes) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("comments", commentService.getCommentsByProduct(id));
        model.addAttribute("product", productService.findByID(id));
        model.addAttribute("productCategories", pro_ca_Service.findAll());
        model.addAttribute("productSimilar",
                productService.findProductByCategory(productService.findByID(id).getCategory().getId()));
        if (productService.findByID(id) == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/ProductUser?id=1";
        }
        model.addAttribute("view", "detail");
        return "layouts/layout";
    }

    @PostMapping("/detail/comment")
    public String comment(Model model, @RequestParam("comment") String content,
            @RequestParam("productId") Long productId,
            RedirectAttributes redirectAttributes) {
        try {
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nội dung bình luận không được để trống.");
            } else if (productService.findByID(productId) == null) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            } else {
                commentService.saveComment(content, productId);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
        }
        return "redirect:/detail?id=" + productId;
    }

}
