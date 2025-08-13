package com.datn.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.datn.model.Zone;

@Repository
public interface ZoneDAO extends JpaRepository<com.datn.model.Zone, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT z FROM Zone z WHERE LOWER(z.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Zone> searchByName(@Param("keyword") String keyword, Pageable pageable);
}

