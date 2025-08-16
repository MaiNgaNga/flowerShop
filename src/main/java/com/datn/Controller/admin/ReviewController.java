package com.datn.Controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.CommentService;
import com.datn.model.Comment;

@RequestMapping("/admin/reviews")
@Controller
public class ReviewController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("inactiveReview", commentService.findByStatusInactive());

        model.addAttribute("view", "admin/review");
        return "admin/layout";
    }

    @GetMapping("/duyetReview/{id}")
    public String markProcessed(@PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Comment comment = commentService.findById(id);
        if (comment != null) {
            comment.setStatus("Active");
            commentService.updateComment(comment.getId(), comment);
            model.addAttribute("success", "Đã phê duyệt.");
            model.addAttribute("view", "admin/review");

        }
        // Quay lại trang hiện tại và giữ trạng thái lọc
        return "redirect:/admin/reviews/index?status=" + status + "&page=" + page;
    }
}
