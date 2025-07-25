package com.datn.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.datn.model.Comment;

public interface CommentDAO extends JpaRepository<Comment, Long> {
    List<Comment> findByProduct_Id(Long productId);

    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC LIMIT 3") 
                                                                        
    List<Comment> findTop3ByOrderByCreatedAtDesc();

}
