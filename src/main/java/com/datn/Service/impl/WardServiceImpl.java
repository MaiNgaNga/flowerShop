package com.datn.Service.impl;

import com.datn.Service.WardService;
import com.datn.dao.WardDAO;
import com.datn.model.Ward;
import com.datn.model.Zone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WardServiceImpl implements WardService {

    @Autowired
    private WardDAO wardDAO;

    @Override
    public List<Ward> getAllWards() {
        return wardDAO.findAll();
    }

    @Override
    public Ward updateWard(Long id, Ward wardDetails) {
        Ward ward = wardDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Ward not found with id: " + id));

        ward.setZone(wardDetails.getZone()); // Cho phép đổi zone

        return wardDAO.save(ward);
    }
    @Override
     public Double getShipFeeByWardId(Long wardId) {
        Ward ward = wardDAO.findById(wardId)
                      .orElseThrow(() -> new RuntimeException("Ward not found"));
        Zone zone = ward.getZone();
        if(zone == null) {
            throw new RuntimeException("Zone not found for ward");
        }
        return zone.getShipFee();
    }
}
