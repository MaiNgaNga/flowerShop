package com.datn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datn.model.User;

public interface UserDAO extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);

    User findByEmail(String email);

}
