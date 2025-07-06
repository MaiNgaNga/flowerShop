package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Post;

public interface PostDAO extends JpaRepository<Post, Long> {

    // Lấy tất cả bài viết đã publish (status = true)
    List<Post> findByStatusTrue();

    // Lấy tất cả bài viết nháp (status = false)
    List<Post> findByStatusFalse();

    // Lấy bài viết mới nhất (giới hạn theo số lượng)
    @Query(value = "SELECT TOP 5 * FROM posts ORDER BY last_updated_at DESC", nativeQuery = true)
    List<Post> findLatestPosts();

    // Tìm bài viết theo tiêu đề (gần đúng, không phân biệt hoa thường)
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // Lấy bài viết phân trang
    Page<Post> findAll(Pageable pageable);

    // Lấy các bài viết theo status với phân trang
    Page<Post> findByStatus(boolean status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id <> :excludeId ORDER BY p.postDate DESC")
    List<Post> findTopRelatedPosts(@Param("excludeId") Long excludeId, Pageable pageable);

    List<Post> findTop12ByStatusTrueOrderByPostDateDesc();

}
