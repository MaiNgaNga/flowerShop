package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.datn.model.Shipper;

public interface ShipperService {

    List<Shipper> findAll();

    Shipper findById(int id);
    Shipper save(Shipper shipper);
    void deleteById(int id);
    void deleteByUserId( int userId);
    boolean existsById(int id);
    Shipper update(Shipper shipper);
    Page<Shipper> findByAllShippers(Pageable pageable);
    Page<Shipper> searchByName(String name, Pageable pageable);
    Page<Shipper> searchByStatus(Boolean status, Pageable pageable);
    boolean existsByUserId(Integer userId);
    Shipper findByUserId(@Param("userId") Integer userId);
    
} 