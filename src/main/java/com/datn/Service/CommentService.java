package com.datn.Service;

import java.util.List;

import com.datn.model.Comment;

public interface CommentService {
List<Comment> getCommentsByProduct(Long productId);

Comment saveComment(String content, Long productId, Integer rating);

void deleteComment(Long id);

List<Comment> getTop3Comments();
}
