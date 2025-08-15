package com.datn.Service;

import com.datn.model.Zone;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZoneService {
     List<Zone> findAll();

    Zone findById(Long id);

    Zone create(Zone entity);

    void update(Zone entity);

    void deleteById(Long id);

    boolean existsById(Long id);

    Page<Zone> searchByName(String keyword, Pageable pageable);

    Page<Zone> findAllPaginated(Pageable pageable);
}
