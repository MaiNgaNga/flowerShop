package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.Service.ContactService;
import com.datn.model.Contact;

@Controller
@RequestMapping("/Contact")
public class ContactAdminController {

    @Autowired
    private ContactService contactService;

    @ModelAttribute("contacts")
    public List<Contact> getAllContacts() {
        return contactService.findAll(Pageable.unpaged()).getContent();
    }

    @RequestMapping("/index/admin")
    public String index(Model model,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ModelAttribute("contact") Contact contact) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Contact> contactPage;

        if (year != null || month != null) {
            // Lọc theo tháng/năm
            contactPage = contactService.findByMonthAndYear(month, year, pageable);
        } else if (status != null && "true".equals(status)) {
            // Lọc theo trạng thái
            contactPage = contactService.findbyStatus(true, pageable);
        } else if (status != null && "false".equals(status)) {
            contactPage = contactService.findbyStatus(false, pageable);
        } else {
            // Không lọc gì hết
            contactPage = contactService.findAll(pageable);
        }

        // Lấy danh sách năm để show dropdown
        List<Integer> years = contactService.findDistinctYears();

        model.addAttribute("contacts", contactPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", contactPage.getTotalElements());
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("years", years);
        model.addAttribute("view", "admin/contactCRUD");

        return "admin/layout";
    }

    @GetMapping("/markProcessed/{id}")
    public String markProcessed(@PathVariable("id") int id,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Contact contact = contactService.findById(id);
        if (contact != null) {
            contact.setStatus(true);
            contactService.updateContact(id, contact);
            model.addAttribute("success", "Đã đánh dấu là đã xử lý.");
            model.addAttribute("view", "admin/contactCRUD");

        }
        // Quay lại trang hiện tại và giữ trạng thái lọc
        return "redirect:/Contact/index/admin?status=" + status + "&page=" + page;
    }

    // API lấy chi tiết liên hệ trả về JSON
    @GetMapping("/viewDetail/{id}")
    @ResponseBody
    public Contact viewDetail(@PathVariable("id") int id) {
        Contact contact = contactService.findById(id);
        if (contact == null) {
            // Nếu không tìm thấy thì trả object rỗng
            return new Contact();
        }
        return contact;
    }

}
