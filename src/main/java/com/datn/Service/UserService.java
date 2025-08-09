package com.datn.Service;

import java.util.List;
import java.util.Optional;


import com.datn.model.User;

public interface UserService {
    List<User> findAll();

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

}
