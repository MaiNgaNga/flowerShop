package com.datn.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Promotion;

public interface PromotionDAO extends JpaRepository<Promotion, Long> {
    boolean existsByTitleAndIdNot(String title, Long id);

    @Query("SELECT p FROM Promotion p WHERE p.endDate >= :fromDate AND p.startDate <= :toDate")
    Page<Promotion> findPromotionsByDateRange(@Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate,
                                          Pageable pageable);

                                          
    @Query("SELECT p FROM Promotion p WHERE p.title like %:title%")
    Page<Promotion> findPromotionsByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE p.title = :voucher")
    Promotion findPromotionByName(@Param("voucher") String voucher);

} 