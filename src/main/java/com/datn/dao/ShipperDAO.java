package com.datn.dao;

import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.model.Shipper;

public interface ShipperDAO extends JpaRepository<Shipper, Integer>{

    @Query("SELECT s FROM Shipper s WHERE s.id = ?1")
    Shipper findById(int id);

    @Query("SELECT s FROM Shipper s WHERE s.isDeleted = false")
    List<Shipper> findAll();

    @Query("SELECT s FROM Shipper s WHERE s.isDeleted = false and s.user.role = 2")
    Page<Shipper> findAllActive(Pageable pageable);

    @Query("SELECT s FROM Shipper s WHERE s.user.name LIKE %:name% and s.isDeleted = false and s.user.role = 2")
    Page<Shipper> searchByName(@Param("name") String name, Pageable pageable);



    @Query("SELECT s FROM Shipper s WHERE s.status = ?1 and s.isDeleted = false and s.user.role = 2")
    Page<Shipper> findByStatus(Boolean status, Pageable pageable);

   // ShipperRepository
    @Query("SELECT COUNT(s) > 0 FROM Shipper s WHERE s.user.id = :userId and s.isDeleted = false and s.user.role = 2")
    boolean existsByUserId(@Param("userId") Integer userId);



}
