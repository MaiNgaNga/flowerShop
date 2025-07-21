package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.datn.Service.ContactService;
import com.datn.dao.ContactDAO;
import com.datn.model.Contact;

@Controller
@RequestMapping("/Contact")
public class ContactAdminController {
    
    @Autowired
    private ContactService contactService;

    @ModelAttribute("contacts")
    public List<Contact> getAllContacts() {
        return contactService.getAllContacts();
    }
    
    @RequestMapping("/index/admin")
    public String index(Model model, @ModelAttribute("contact") Contact contact,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "10") int size,
    @RequestParam("status") boolean status
    ) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Contact> contactPage;

        contactPage = contactService.findAllUnprocessed(pageable);
        model.addAttribute("contacts", contactPage.getContent());

        model.addAttribute("view", "admin/contactCRUD");

        return "admin/layout";
    }

    @RequestMapping("/processed")
    public String processed(Model model) {
        model.addAttribute("view", "admin/contactCRUD");
        return "admin/layout";
    }

}
