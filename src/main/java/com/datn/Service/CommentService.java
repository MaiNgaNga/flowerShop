package com.datn.Service;

import java.util.List;


public interface CommentService {
     List<Comment> getCommentsByProduct(Long productId);

     Comment saveComment(String content, Long productId);

     void deleteComment(Long id);
}
