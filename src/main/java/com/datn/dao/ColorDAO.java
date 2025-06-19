package com.datn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.Color;

public interface ColorDAO extends JpaRepository<Color, Integer> {

}
