package com.datn.Service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.datn.Service.PromotionService;
import com.datn.dao.PromotionDAO;
import com.datn.model.ProductCategory;
import com.datn.model.Promotion;

@Service
public class PromotionServiceImpl implements PromotionService{
    @Autowired
    private PromotionDAO promotionDAO;

    @Override
    public List<Promotion> findAll() {
        return promotionDAO.findAll();
    }

    @Override
    public Promotion create(Promotion entity) {
        return promotionDAO.save(entity);
        
    }

    @Override
public void update(Promotion entity) {
    if (promotionDAO.existsById(entity.getId())) {

        // Kiểm tra trùng title với bản ghi khác
        boolean isDuplicate = promotionDAO.existsByTitleAndIdNot(entity.getTitle(), entity.getId());
        if (isDuplicate) {
            throw new IllegalArgumentException("Tên khuyến mãi này đã tồn tại!");
        }

        // Kiểm tra title rỗng
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được để trống!");
        }
        // Lưu vào DB
        promotionDAO.save(entity);
    } else {
        throw new IllegalArgumentException("Chưa chọn khuyến mãi để cập nhật!");
    }
}


    @Override
    public void deleteById(String id) {
        promotionDAO.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return promotionDAO.existsById(id);
    }

    @Override
    public Promotion findByID(String id) {
        return promotionDAO.findById(id).orElse(null);
    }

    @Override
    public List<Promotion> getPromotionsInCurrentMonth() {
       LocalDate now = LocalDate.now();
       LocalDate start= now.withDayOfMonth(1);
       LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

       return promotionDAO.findPromotionsByDateRange(start, end);
    }

    @Override
    public Page<Promotion> findByAllPromotion(Pageable pageable) {
        return promotionDAO.findAll(pageable);
    }

    @Override
    public Page<Promotion> searchByDate(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return promotionDAO.findPromotionsByDateRange(fromDate, toDate, pageable);
    }

    @Override
    public Page<Promotion> searchByName(String name, Pageable pageable) {
        return promotionDAO.findPromotionsByTitle(name, pageable);
    }

  
    
}
