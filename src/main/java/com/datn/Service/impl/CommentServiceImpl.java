package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.Assignment.Service.CommentService;
import poly.edu.Assignment.Service.ProductService;
import poly.edu.Assignment.dao.CommentDAO;
import poly.edu.Assignment.model.Comment;
import poly.edu.Assignment.model.Product;
import poly.edu.Assignment.model.User;
import poly.edu.Assignment.utils.AuthService;

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

    public List<Comment> getCommentsByProduct(Long productId) {
        return commentDAO.findByProduct_Id(productId);
    }

    public Comment saveComment(String content, Long productId) {
        User user = authService.getUser();
        Product product = productService.findByID(productId);
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setProduct(product);
        comment.setCreatedAt(LocalDateTime.now());

        return commentDAO.save(comment);
    }

    // Xóa bình luận theo ID
    public void deleteComment(Long id) {
        commentDAO.deleteById(id);
    }

}
