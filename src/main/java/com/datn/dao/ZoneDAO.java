package com.datn.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneDAO extends JpaRepository<com.datn.model.Zone, Long> {
}

