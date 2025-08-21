package com.datn.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.datn.model.User;

public interface UserDAO extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsBySdt(String sdt);
    User findByEmail(String email);

    User findBySdt(String sdt);

    @Query("SELECT u FROM User u WHERE u.name LIKE %?1%")
    Page<User> searchByName(String name, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = ?1")
    Page<User> findByRole(int role, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    // tìm ng dùng k phải là shipper
    @Query("SELECT u FROM User u  where u.role != 2")
    List<User> findAllNonShippers();

    @Query("SELECT u FROM User u WHERE u.rememberToken = ?1")
    Optional<User> findByRememberToken(String token);
    

}
