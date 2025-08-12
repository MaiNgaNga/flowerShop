package com.datn.Service.impl;

import com.datn.Service.ZoneService;
import com.datn.dao.ZoneDAO;
import com.datn.model.Zone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    private ZoneDAO zoneRepository;

    @Override
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    @Override
    public Zone updateZone(Long id, Zone zoneDetails) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));

        zone.setName(zoneDetails.getName());
        zone.setShipFee(zoneDetails.getShipFee());

        return zoneRepository.save(zone);
    }
     @Override
    public Zone createZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    @Override
    
    public void deleteZone(Long id) {
    Zone zone = zoneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));

    if (zone.getWards() != null && !zone.getWards().isEmpty()) {
        throw new RuntimeException("Vui lòng xóa các xã/phường khỏi zone này trước khi xóa zone.");
    }

    zoneRepository.deleteById(id);
    }

}
