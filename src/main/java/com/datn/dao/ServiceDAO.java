package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.ServiceEntity;

public interface ServiceDAO extends JpaRepository<ServiceEntity, Long> {

    // Tìm kiếm theo tên
    @Query("SELECT s FROM ServiceEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ServiceEntity> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Lấy danh sách phân trang
    Page<ServiceEntity> findAll(Pageable pageable);

    // Lấy danh sách dịch vụ đang hoạt động (có phân trang)
    Page<ServiceEntity> findByAvailableTrue(Pageable pageable);

    // Lấy toàn bộ dịch vụ đang hoạt động (không phân trang – dùng cho form
    // <select>)
    List<ServiceEntity> findByAvailableTrue();

    // Lấy 1 dịch vụ mới nhất (theo ID giảm dần)
    List<ServiceEntity> findTop1ByOrderByIdDesc();

    // Lấy 1 dịch vụ mới nhất có trạng thái hoạt động (theo ID giảm dần)
    List<ServiceEntity> findTop1ByAvailableTrueOrderByIdDesc();

}
