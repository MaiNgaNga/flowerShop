package com.datn.Controller.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.utils.AuthService;


@Controller
public class Login {
    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String login(Model model) {
        return "account/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Chưa nhập đầy đủ thông tin!");
            return "account/login";
        }
        if (authService.login(username, password)) {
            int role = authService.getUser().getRole();
            if (role == 2) {
                return "redirect:/shipper/pending-orders";
            }
            return "redirect:/";
        } else {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
            return "account/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        authService.logout();
        return "redirect:/";
    }

}
