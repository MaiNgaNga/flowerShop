package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.datn.Service.ContactService;
import com.datn.model.Contact;

import jakarta.validation.Valid;

import com.datn.Service.ProductCategoryService;

@Controller
public class ContactPageController {

    @Autowired
    private ContactService contactService;


    @GetMapping("/contact")
    public String contact(Model model, @ModelAttribute("contact") Contact contact) {
        model.addAttribute("view", "contact");
        return "layouts/layout";
    }

    @PostMapping("/sendContact")
    public String createContact(Model model,@ModelAttribute("contact") Contact contact) {

        contactService.saveContact(contact);
        model.addAttribute("successMessage", "Cảm ơn bạn đã gửi thông tin đến chúng tôi <br/>. (Chúng tôi sẽ sớm liên hệ với bạn trong thời gian sớm nhất!)");
        model.addAttribute("view", "contact");
        return "layouts/layout";
    }

}
