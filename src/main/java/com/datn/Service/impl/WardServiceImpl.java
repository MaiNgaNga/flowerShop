package com.datn.Service.impl;

import com.datn.Service.WardService;
import com.datn.dao.WardDAO;
import com.datn.model.Ward;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WardServiceImpl implements WardService {

    @Autowired
    private WardDAO wardDAO;

    @Override
    public List<Ward> getAllWards() {
        return wardDAO.findAll();
    }
 
    @Override
    public Ward findById(Long id) {
        Optional<Ward> ward = wardDAO.findById(id);
        return ward.orElse(null);
    }

    @Override
    public Ward create(Ward entity) {
        if (wardDAO.existsByName(entity.getName())) {
            throw new IllegalArgumentException("Tên xã/phường này đã tồn tại!");
        }
        return wardDAO.save(entity);
    }

    @Override
    public void update(Ward entity) {
        if (entity.getId() == null || !wardDAO.existsById(entity.getId())) {
            throw new IllegalArgumentException("Chưa chọn xã/phường để cập nhật!");
        }
        if (wardDAO.existsByNameAndIdNot(entity.getName(), entity.getId())) {
            throw new IllegalArgumentException("Tên xã/phường này đã tồn tại!");
        }
        if (entity.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên xã/phường không được để trống!");
        }
        wardDAO.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        if (!wardDAO.existsById(id)) {
            throw new IllegalArgumentException("Không xác định được xã/phường cần xóa");
        }

       

        wardDAO.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return wardDAO.existsById(id);
    }

    @Override
    public Page<Ward> searchByName(String name, Pageable pageable) {
        return wardDAO.searchByName(name, pageable);
    }

   @Override
    public Page<Ward> findAllPaginatedByZoneId(Long zoneId, Pageable pageable) {
    return wardDAO.findByZone_Id(zoneId, pageable);
}

   
}
