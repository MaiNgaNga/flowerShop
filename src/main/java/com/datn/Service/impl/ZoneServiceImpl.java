package com.datn.Service.impl;

import com.datn.Service.ZoneService;
import com.datn.dao.WardDAO;
import com.datn.dao.ZoneDAO;
import com.datn.model.Zone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ZoneServiceImpl implements ZoneService {

   @Autowired
    private ZoneDAO dao;

    @Autowired
    private WardDAO wardDAO;

    @Override
    public List<Zone> findAll() {
        return dao.findAll();
    }

    @Override
    public Zone findById(Long id) {
        Optional<Zone> zone = dao.findById(id);
        return zone.orElse(null);
    }

    @Override
    public Zone create(Zone entity) {
        if (dao.existsByName(entity.getName())) {
            throw new IllegalArgumentException("Tên zone này đã tồn tại!");
        }
        return dao.save(entity);
    }

    @Override
    public void update(Zone entity) {
        if (dao.existsById(entity.getId())) {
            if (dao.existsByNameAndIdNot(entity.getName(), entity.getId())) {
                throw new IllegalArgumentException("Zone này đã tồn tại!");
            }
            if (entity.getName().isEmpty()) {
                throw new IllegalArgumentException("Tên zone không được để trống!");
            }
            dao.save(entity);
        } else {
            throw new IllegalArgumentException("Chưa chọn zone để cập nhật!");
        }
    }

    @Override
    public void deleteById(Long id) {
        if (dao.existsById(id)) {
            
            if (!wardDAO.findByZone_Id(id).isEmpty()) {
                throw new IllegalArgumentException("Zone bao gồm xã/phường, hãy đổi xã/phường qua zone khác trước khi xóa!");
            }
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không xác định được zone cần xóa");
        }
    }

    @Override
    public boolean existsById(Long id) {
        return dao.existsById(id);
    }

    @Override
    public Page<Zone> searchByName(String name, Pageable pageable) {
        return dao.searchByName(name, pageable);
    }

    @Override
    public Page<Zone> findAllPaginated(Pageable pageable) {
        return dao.findAll(pageable);
    }

}
