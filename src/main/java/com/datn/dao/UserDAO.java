package com.datn.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.datn.model.User;

public interface UserDAO extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);
    User findByEmail(String email);

    User findBySdt(String sdt);

    // tìm ng dùng k phải là shipper
    @Query("SELECT u FROM User u  where u.role != 2")
    List<User> findAllNonShippers();

    @Query("SELECT u FROM User u WHERE u.rememberToken = ?1")
    Optional<User> findByRememberToken(String token);
    

}
