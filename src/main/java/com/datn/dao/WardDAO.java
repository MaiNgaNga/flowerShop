package com.datn.dao;



import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.datn.model.Ward;


@Repository
public interface WardDAO extends JpaRepository<Ward, Long> {
    List<Ward> findByZone_Id(Long zoneId);
      boolean existsByName(String name);

    // Kiểm tra tồn tại ward theo tên nhưng không tính ward có id truyền vào (dùng cho update)
    boolean existsByNameAndIdNot(String name, Long id);

    // Tìm kiếm ward theo tên (không phân biệt hoa thường) và phân trang
    @Query("SELECT w FROM Ward w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Ward> searchByName(@Param("keyword") String keyword, Pageable pageable);
    Page<Ward> findByZone_Id(Long zoneId, Pageable pageable);

}

