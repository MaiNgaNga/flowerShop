package com.datn.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.datn.Service.CommentService;
import com.datn.Service.OrderService;
import com.datn.Service.ProductService;
import com.datn.dao.CommentDAO;
import com.datn.model.Comment;
import com.datn.model.Order;

import com.datn.model.User;
import com.datn.utils.AuthService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDAO commentDAO;
    @Autowired
    AuthService authService;
    @Autowired
    ProductService productService;

    @Autowired
    OrderService  orderService;

    // public List<Comment> getCommentsByProduct(Long productId) {
    //     return commentDAO.findByProduct_Id(productId);
    // }

    public List<Comment> getCommentsByOrder(Long orderId) {
        Order order = orderService.findByID(orderId);
        return commentDAO.findByOrder(order);
    }

    public Comment saveComment(String content, Long orderID, Integer rating) {
        User user = authService.getUser();
        Order order = orderService.findByID(orderID);
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setOrder(order);
        comment.setRating(rating);
        comment.setStatus("Inactive");
        comment.setCreatedAt(LocalDateTime.now());

        return commentDAO.save(comment);

    }

    // Xóa bình luận theo ID
    public void deleteComment(Long id) {
        commentDAO.deleteById(id);
    }

    @Override
    public List<Comment> getTop3Comments() {
        return commentDAO.findTop3ByOrderByCreatedAtDesc();
    }

    @Override
    public Page<Comment> findByStatusInactive(Pageable pageable) {
        return commentDAO.findByStatusInactive(pageable);
    }

    @Override
    public void updateComment(Long id, Comment comment) {
         commentDAO.save(comment);
    }

    @Override
    public Comment findById(Long id) {
        return commentDAO.findById(id).orElse(null);
    }

   
}
