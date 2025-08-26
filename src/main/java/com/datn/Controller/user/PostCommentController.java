package com.datn.Controller.user;

import com.datn.Service.PostCommentService;
import com.datn.model.PostComment;
import com.datn.utils.AuthService;
import com.datn.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/post-comments")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private AuthService authService;

    // Xử lý gửi bình luận
    @PostMapping
    public String postComment(@RequestParam("postId") Long postId,
            @RequestParam("content") String content,
            RedirectAttributes redirectAttributes) {
        try {
            postCommentService.saveComment(content, postId);
        } catch (IllegalStateException e) {
            // Nếu chưa đăng nhập, bắt lỗi và chuyển hướng lại với thông báo
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/postDetail?id=" + postId;
    }

    // Xử lý xóa bình luận
    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable("id") Long id,
            @RequestParam("postId") Long postId,
            RedirectAttributes redirectAttributes) {
        // Lấy thông tin người dùng hiện tại
        User currentUser = authService.getUser();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để xóa bình luận.");
            return "redirect:/postDetail?id=" + postId;
        }

        // Tìm bình luận theo ID
        PostComment comment = postCommentService.findById(id);
        if (comment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bình luận không tồn tại.");
            return "redirect:/postDetail?id=" + postId;
        }

        // Kiểm tra xem người dùng hiện tại có phải là người tạo bình luận
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa bình luận này.");
            return "redirect:/postDetail?id=" + postId;
        }

        // Xóa bình luận
        postCommentService.deleteComment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được xóa thành công.");
        return "redirect:/postDetail?id=" + postId;
    }
}