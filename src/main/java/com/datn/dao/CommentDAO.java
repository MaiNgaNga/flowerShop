package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.datn.model.Comment;
import com.datn.model.Order;

public interface CommentDAO extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.order = ?1")
    List<Comment> findByOrder(Order order);

    // lấy 3 comment với status Active
    @Query("SELECT c FROM Comment c WHERE c.status = 'Active' ORDER BY c.createdAt DESC,c.rating DESC LIMIT 3")
    List<Comment> findTop3ByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Comment c WHERE c.status = 'Inactive'")
    Page<Comment> findByStatusInactive(Pageable pageable);

}
