package com.datn.Controller.user;

import com.datn.Service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/post-comments")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

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

    // (Tùy chọn) Xóa bình luận thẳng
    @PostMapping("/delete")
    public String deleteComment(@RequestParam("id") Long id,
            @RequestParam("postId") Long postId) {
        postCommentService.deleteComment(id);
        return "redirect:/postDetail?id=" + postId;
    }
}
