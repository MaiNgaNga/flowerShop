package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.Service.ContactService;
import com.datn.Service.ProductCategoryService;
import com.datn.model.Contact;
import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
public class ContactPageController {

    @Autowired
    private ContactService contactService;
    @Autowired
    private ProductCategoryService pro_ca_Service;
    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    @GetMapping("/contact")
    public String contact(Model model, @ModelAttribute("contact") Contact contact) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("view", "contact");
        model.addAttribute("productCategories", pro_ca_Service.findAll());
        return "layouts/layout";
    }

    @PostMapping("/sendContact")
    @ResponseBody
    public ResponseEntity<String> createContact(@ModelAttribute Contact contact) {
        contact.setStatus(false);
        contactService.saveContact(contact);
        return ResponseEntity.ok("Gửi liên hệ thành công!");
    }


}
