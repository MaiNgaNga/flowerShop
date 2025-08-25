package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Comment;

public interface CommentService {


    List<Comment> getCommentsByOrder(Long orderId);

    Comment saveComment(String content, Long orderID, Integer rating);

    void deleteComment(Long id);

    void updateComment(Long id, Comment comment);
    // Thêm phương thức để lấy 3 bình luận mới nhất
    List<Comment> getTop3Comments();

    Page<Comment> findByStatusInactive(Pageable pageable);

    Comment findById(Long id);

}
