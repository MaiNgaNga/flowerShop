package com.datn.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.datn.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
public class CustomRememberMeService {

    private UserService userService;

    public CustomRememberMeService(UserService userService) {
        this.userService = userService;
    }

    public void createRememberMeToken(User user, HttpServletResponse response) {
    String token = UUID.randomUUID().toString();
    LocalDateTime expiry = LocalDateTime.now().plusDays(7);

    user.setRememberToken(token);
    user.setRememberTokenExpiry(expiry);

    // Lưu user lại vào DB
    userService.update(user);

    Cookie cookie = new Cookie("REMEMBER_ME", token);
    cookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    response.addCookie(cookie);
}


    public Optional<User> autoLoginFromRememberMe(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();

        for (Cookie cookie : request.getCookies()) {
            if ("REMEMBER_ME".equals(cookie.getName())) {
                String token = cookie.getValue();
                Optional<User> userOpt = userService.findByRememberToken(token);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getRememberTokenExpiry() != null &&
                        user.getRememberTokenExpiry().isAfter(LocalDateTime.now())) {
                        return Optional.of(user);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Transactional
    public void clearRememberMe(User user, HttpServletResponse response) {
        // Lấy lại user từ DB để đảm bảo nó trong persistence context
        User persistentUser = userService.findByID(user.getId());
        if (persistentUser != null) {
            persistentUser.setRememberToken(null);
            persistentUser.setRememberTokenExpiry(null);
            userService.update(persistentUser);
        }

        Cookie cookie = new Cookie("REMEMBER_ME", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }


}
