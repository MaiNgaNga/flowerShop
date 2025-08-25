package com.datn.Service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.datn.Service.PromotionService;
import com.datn.dao.PromotionDAO;
import com.datn.model.Promotion;

@Service
public class PromotionServiceImpl implements PromotionService {
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
    public void deleteById(Long id) {
        promotionDAO.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return promotionDAO.existsById(id);
    }

    @Override
    public Promotion findByID(Long id) {
        return promotionDAO.findById(id).orElse(null);
    }

    @Override
    public Page<Promotion> findByAllPromotion(Pageable pageable) {
        return promotionDAO.findAllPromotions(pageable);
    }

    @Override
    public Page<Promotion> searchByDate(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return promotionDAO.findPromotionsByDateRange(fromDate, toDate, pageable);
    }

    @Override
    public Page<Promotion> searchByName(String name, Pageable pageable) {
        return promotionDAO.findPromotionsByTitle(name, pageable);
    }

    @Override
    public Promotion findPromotionByName(String voucherCode) {
        return promotionDAO.findPromotionByName(voucherCode);
    }

    @Transactional
    @Override
    public void updateUseCount(Long id, int useCount) {
        promotionDAO.updateUseCount(id, useCount);
    }

    @Override
    public List<Promotion> findValidPromotion() {
        return promotionDAO.findValidPromotions();
    }

}
