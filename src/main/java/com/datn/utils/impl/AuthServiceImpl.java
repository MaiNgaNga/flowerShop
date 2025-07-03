package com.datn.utils.impl;

import java.util.List;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.dao.UserDAO;

import com.datn.model.User;
import com.datn.utils.AuthService;
import com.datn.utils.SessionService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SessionService session;
    @Autowired
    private UserDAO userRepository;

    @Override
    public User getUser() {
        User user = (User) session.get("user");
        return (user != null) ? user : null;
    }

    @Override
    public List<String> getRole() {
        List<String> roles = session.get("roles");
        return (roles != null) ? roles : List.of();
    }

    @Override
    public boolean isAuthenticated() {
        return getUser() != null;
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        List<String> userRoles = getRole();
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    @Override
    public boolean login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            session.set("user", user);
            // thêm dòng sau:
            session.set("roles", List.of(String.valueOf(user.getRole()))); // giả sử role là Integer
            session.set("userId", user.getId());
            return true;
        }
        return false;
    }

    public void logout() {
        session.remove("user");
    }

    public User getCurrentUser() {
        return session.get("user");
    }

}
