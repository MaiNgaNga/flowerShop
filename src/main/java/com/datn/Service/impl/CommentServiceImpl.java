package com.datn.Service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.CommentService;
import com.datn.Service.ProductService;
import com.datn.dao.CommentDAO;
import com.datn.model.Comment;
import com.datn.model.Product;
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
