package com.datn.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.datn.model.Shipper;

public interface ShipperDAO extends JpaRepository<Shipper, Integer> {

    @Query("SELECT s FROM Shipper s WHERE s.id = ?1")
    Shipper findById(int id);

    @Query("SELECT s FROM Shipper s ")
    List<Shipper> findAll();

    @Query("SELECT s FROM Shipper s")
    Page<Shipper> findAllActive(Pageable pageable);

    @Query("SELECT s FROM Shipper s WHERE s.user.name LIKE %:name%")
    Page<Shipper> searchByName(@Param("name") String name, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Shipper s WHERE s.user.id = :userId")
    void deleteByUserId(int userId);

    @Query("SELECT s FROM Shipper s WHERE s.user.id = :userId")
    Shipper findByUserId(@Param("userId") Integer userId);

    @Query("SELECT s FROM Shipper s WHERE s.user.status = ?1")
    Page<Shipper> findByStatus(Boolean status, Pageable pageable);

    // ShipperRepository
    @Query("SELECT COUNT(s) > 0 FROM Shipper s WHERE s.user.id = :userId and s.user.role = 2")
    boolean existsByUserId(@Param("userId") Integer userId);

}
