package com.datn.utils;

import java.util.List;

import com.datn.model.User;

public interface AuthService {
    User getUser();

    List<String> getRole();

    boolean isAuthenticated();

    boolean hasAnyRoles(String... roles);

    boolean login(String email, String password);

    void logout();

    User getCurrentUser();

}
