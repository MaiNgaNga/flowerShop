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
            @RequestParam(required = false) String rememberMe,
            Model model, HttpServletResponse response) {

        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Chưa nhập đầy đủ thông tin!");
            return "account/login";
        }

        if (!authService.login(username, password)) {
            model.addAttribute("error", "Tài khoản hoặc mật khẩu không chính xác!");
            return "account/login";
        }

        var user = authService.getUser();
        if (user == null) {
            model.addAttribute("error", "Không tìm thấy thông tin người dùng!");
            return "account/login";
        }

        if (!user.getStatus()) {
            model.addAttribute("error", "Tài khoản của bạn chưa được kích hoạt!");
            return "account/login";
        }

        if ("on".equals(rememberMe)) {
            customRememberMeService.createRememberMeToken(user, response);
        }

        return switch (user.getRole()) {
            case 0 -> "redirect:/home";
            case 1 -> "redirect:/statistical";
            case 2 -> "redirect:/shipper/pending-orders";
            case 3 -> "redirect:/Product/index";
            default -> "redirect:/home";
        };
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        var user = authService.getUser();
        customRememberMeService.clearRememberMe(user, response);
        authService.logout();
        return "redirect:/home";
    }

}
