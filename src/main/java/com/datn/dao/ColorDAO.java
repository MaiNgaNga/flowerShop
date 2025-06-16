package main.java.com.datn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.Assignment.model.Color;

public interface ColorDAO extends JpaRepository<Color,Integer> {

}
