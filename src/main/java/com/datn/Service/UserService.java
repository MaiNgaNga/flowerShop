package com.datn.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.datn.model.User;

public interface UserService {

    List<User> findAll();

    Page<User> findAllUserPage(Pageable pageable);

    User findByID(int id);

    User create(User entity);

    void update(User entity);

    void deleteById(int id);

    boolean existsById(int id);

    User findByEmail(String email);

    boolean updatePassword(String newPassword);

    List<User> findAllNonShipper();
    User findBySdt(String sdt);

    Optional<User> findByRememberToken(String token);

    Page<User> searchByName(String name, Pageable pageable);

    Page<User> findByRole(int role, Pageable pageable);

}
