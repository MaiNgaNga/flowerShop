package com.datn.dao;

import com.datn.model.PostComment;
import com.datn.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentDAO extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostOrderByCreatedAtDesc(Post post);

    List<PostComment> findByPost_IdOrderByCreatedAtDesc(Long postId);

    List<PostComment> findByPostId(Long postId);

}   
