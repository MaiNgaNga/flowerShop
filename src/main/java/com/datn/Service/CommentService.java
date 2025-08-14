package com.datn.Service;

import java.util.List;

import com.datn.model.Comment;

public interface CommentService {


    List<Comment> getCommentsByOrder(Long orderId);

    Comment saveComment(String content, Long orderID, Integer rating);

    void deleteComment(Long id);

    void updateComment(Long id, Comment comment);

    ///find by status
    List<Comment> findByStatusInactive();
    // Thêm phương thức để lấy 3 bình luận mới nhất
    List<Comment> getTop3Comments();

    Comment findById(Long id);

}
