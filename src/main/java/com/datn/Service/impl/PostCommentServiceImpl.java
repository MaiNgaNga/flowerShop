package com.datn.Service.impl;

import com.datn.Service.PostCommentService;
import com.datn.Service.PostService;
import com.datn.dao.PostCommentDAO;
import com.datn.model.Post;
import com.datn.model.PostComment;
import com.datn.model.User;
import com.datn.utils.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostCommentServiceImpl implements PostCommentService {

    @Autowired
    private PostCommentDAO postCommentDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private PostService postService;

    @Override
    public List<PostComment> getCommentsByPostId(Long postId) {
        return postCommentDAO.findByPost_IdOrderByCreatedAtDesc(postId);
    }

    @Override
    public PostComment saveComment(String content, Long postId) {
        User user = authService.getUser();
        if (user == null) {
            throw new IllegalStateException("Bạn cần đăng nhập để bình luận.");
        }

        Post post = postService.findById(postId);
        PostComment comment = new PostComment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return postCommentDAO.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        postCommentDAO.deleteById(id);
    }

    @Override
    public List<PostComment> findByPostId(Long postId) {
        return postCommentDAO.findByPostId(postId);
    }
}
