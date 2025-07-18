package com.datn.Service;

import com.datn.model.PostComment;

import java.util.List;

public interface PostCommentService {
    List<PostComment> getCommentsByPostId(Long postId);

    PostComment saveComment(String content, Long postId);

    void deleteComment(Long id);
}
