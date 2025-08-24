package com.datn.Controller.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.CustomRememberMeService;
import com.datn.utils.AuthService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class Login {
    @Autowired
    private AuthService authService;

    @Autowired
    private CustomRememberMeService customRememberMeService;

    @GetMapping("/login")
    public String login(Model model) {
        return "account/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
            @RequestParam(required = false) String rememberMe, Model model, HttpServletResponse response) {
        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Chưa nhập đầy đủ thông tin!");
            return "account/login";
        }
        if (authService.login(username, password)) {

            var user = authService.getUser();
            if ("on".equals(rememberMe)) {
                customRememberMeService.createRememberMeToken(user, response);
            }
            int role = authService.getUser().getRole();
            return switch (role) {
                case 0 -> "redirect:/home";
                case 1 -> "redirect:/statistical";
                case 2 -> "redirect:/shipper/pending-orders";
                case 3 -> "redirect:/pos";
                default -> "redirect:/home";
            };
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            return "account/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        var user = authService.getUser();
        customRememberMeService.clearRememberMe(user, response);
        authService.logout();
        return "redirect:/home";
    }

}
