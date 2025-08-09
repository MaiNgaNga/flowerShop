package com.datn.Intercepter;

import java.io.IOException;
import java.util.List;

import org.apache.tomcat.util.net.AbstractEndpoint.Handler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;

import com.datn.Service.CustomRememberMeService;
import com.datn.utils.AuthService;
import com.datn.utils.SessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RememberMeFilter extends OncePerRequestFilter {

    private final CustomRememberMeService rememberMeService;
    private final AuthService authService;
    private final SessionService sessionService;

    public RememberMeFilter(CustomRememberMeService rememberMeService, AuthService authService, SessionService sessionService) {
        this.rememberMeService = rememberMeService;
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Nếu chưa có user trong session
        if (sessionService.get("user") == null) {
            rememberMeService.autoLoginFromRememberMe(request).ifPresent(user -> {
                sessionService.set("user", user);
                sessionService.set("roles", List.of(String.valueOf(user.getRole())));
                sessionService.set("userId", user.getId());
            });
        }

        filterChain.doFilter(request, response);
    }
}
