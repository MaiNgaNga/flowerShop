package com.datn.Service;

import java.time.LocalDate;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.Promotion;

public interface PromotionService {

    List<Promotion> findAll();
    Promotion findByID(Long id);
    Promotion create(Promotion entity);
    void update(Promotion entity);
    void deleteById(Long id);
    boolean existsById(Long id);
    List<Promotion> getPromotionsInCurrentMonth();
    Page<Promotion> findByAllPromotion(Pageable pageable);
    Page<Promotion> searchByDate(LocalDate fromDate, LocalDate toDate, Pageable pageable);
    Page<Promotion> searchByName(String name, Pageable pageable);
    
} 