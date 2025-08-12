package com.datn.dao;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.datn.model.Ward;


@Repository
public interface WardDAO extends JpaRepository<Ward, Long> {
}

