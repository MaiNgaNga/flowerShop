package com.datn.Controller.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.ProductCategoryService;
import com.datn.Service.UserService;
import com.datn.utils.AuthService;




@Controller

public class ChangPassword {
    @Autowired 
    private AuthService authService;
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductCategoryService pro_ca_Service;

    private static final String PASSWORD_PATTERN = ".{4,}"; 
    boolean status=true ;
    @GetMapping("/changPassword")
    public String change(Model model) {
        return showChangPassForm(model);
    }

    @PostMapping("/changPassword")
    public String change(
            Model model, 
            @RequestParam String currentPassword, 
            @RequestParam String newPassword, 
            @RequestParam String confirmNewPassword) {
        
        if ( currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
            return showChangPassForm(model);
        }

        if (!newPassword.matches(PASSWORD_PATTERN)) {
            model.addAttribute("error", "Mật khẩu mới phải có ít nhất 4 kí tự!");
            return showChangPassForm(model);
        }

        if (newPassword.equals(currentPassword)) {
            model.addAttribute("error", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            return showChangPassForm(model);
        }

        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "Mật khẩu mới không trùng khớp!");
            return showChangPassForm(model);
        }

        if (!authService.getCurrentUser().getPassword().equalsIgnoreCase(currentPassword)) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng!");
            return showChangPassForm(model);
        }

        boolean success = userService.updatePassword(newPassword);
        if (success) {
            model.addAttribute("success", "Đổi mật khẩu thành công!");
        } else {
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }

        return showChangPassForm(model);
    }

public String showChangPassForm(Model model){
    model.addAttribute("productCategories", pro_ca_Service.findAll());

    model.addAttribute("status", status);
    model.addAttribute("view", "account/changPassword");
    return "layouts/layout";
}
}
