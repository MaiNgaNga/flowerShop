package com.datn.Controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage = commentService.findByStatusInactive(pageable);

        model.addAttribute("commentPage", commentPage.getContent());
        model.addAttribute("totalPages", commentPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", commentPage.getTotalElements());

        model.addAttribute("hasNext", commentPage.hasNext());
        model.addAttribute("hasPrevious", commentPage.hasPrevious());
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
