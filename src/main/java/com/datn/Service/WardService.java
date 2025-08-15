package com.datn.Service;

import com.datn.model.Ward;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WardService {
    List<Ward> getAllWards();
    Ward findById(Long id);
    Ward create(Ward ward);
    void update(Ward ward);
    void deleteById(Long id);
    boolean existsById(Long id);
    Page<Ward> searchByName(String name, Pageable pageable);
    Page<Ward> findAllPaginatedByZoneId(Long zoneId, Pageable pageable);
}
