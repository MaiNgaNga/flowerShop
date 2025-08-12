package com.datn.Service;

import com.datn.model.Zone;
import java.util.List;

public interface ZoneService {
    List<Zone> getAllZones();
    Zone updateZone(Long id, Zone zone);
    Zone createZone(Zone zone); // thêm mới
    void deleteZone(Long id);   // xóa
}
