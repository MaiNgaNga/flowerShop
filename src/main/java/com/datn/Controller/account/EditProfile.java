package com.datn.Controller.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;


import org.springframework.web.bind.annotation.PostMapping;

import com.datn.Service.ProductCategoryService;
import com.datn.Service.UserService;
import com.datn.model.User;
import com.datn.utils.AuthService;

import jakarta.validation.Valid;

@Controller
public class EditProfile {

@Autowired
private AuthService authService;
@Autowired
private UserService userService;

@Autowired
private ProductCategoryService pro_ca_Service;

@GetMapping("/editProfile")
public String Edit(Model model){
    User user=authService.getUser();
    model.addAttribute("user", user);
    return showEditForm(model);
}

@PostMapping("/editProfile")
public String postMethodName(Model model,@Valid @ModelAttribute User user,Errors errors) {
    User u= userService.findByID(user.getId());
    user.setPassword(u.getPassword());

    if(errors.hasErrors()){
        return showEditForm(model);
    }

    try {
        userService.update(user);
        model.addAttribute("success", "Cập nhật thành công!");
        return showEditForm(model) ;

    } catch (IllegalArgumentException e) {
        model.addAttribute("error", e.getMessage());
        return showEditForm(model);        
    }
}

    public String showEditForm(Model model){
        model.addAttribute("productCategories", pro_ca_Service.findAll());
        model.addAttribute("view", "account/profile");
        return "layouts/layout";
    }
}
