package com.datn.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.ServiceEntity;

public interface ServiceDAO extends JpaRepository<ServiceEntity, Long> {

    // Tìm kiếm theo tên (đã sửa đúng entity name)
    @Query("SELECT s FROM ServiceEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ServiceEntity> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Lấy danh sách phân trang
    Page<ServiceEntity> findAll(Pageable pageable);
}
