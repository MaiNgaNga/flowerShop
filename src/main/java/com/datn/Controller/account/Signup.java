package com.datn.Controller.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.UserService;
import com.datn.model.Mail;
import com.datn.model.User;
import com.datn.utils.impl.SendMailService;

import jakarta.validation.Valid;

@Controller

public class Signup {
    @Autowired
    private UserService userService;

    @Autowired
    private SendMailService sendMailService;

    @GetMapping("/signup")
    public String signup(Model model) {
        User user = new User();
        user.setRole(0);
        model.addAttribute("User", user);

        return showSignupForm(model);
    }

    @PostMapping("/signup")
    public String signup(Model model, @Valid @ModelAttribute("User") User User, Errors errors,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            return showSignupForm(model);
        }
        try {
            userService.create(User);
            sendMailService.send(new Mail(User.getEmail(), "Hello", "Chào mừng đến trang web"));
            redirectAttributes.addFlashAttribute("success", "Đăng kí User thành công!");
            return "redirect:/signup";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return showSignupForm(model);
        }

    }

    public String showSignupForm(Model model) {
        return "account/signup";
    }

}
