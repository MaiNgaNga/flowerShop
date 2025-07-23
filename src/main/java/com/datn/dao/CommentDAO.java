package com.datn.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.Comment;

public interface CommentDAO extends JpaRepository<Comment, Long> {
List<Comment> findByProduct_Id(Long productId);

}
