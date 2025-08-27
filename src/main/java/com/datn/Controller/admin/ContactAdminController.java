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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            contactPage = contactService.findByMonthAndYear(month, year, pageable);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
        } else if (status != null && "true".equals(status)) {
            contactPage = contactService.findbyStatus(true, pageable);
            model.addAttribute("status", status);
        } else if (status != null && "false".equals(status)) {
            contactPage = contactService.findbyStatus(false, pageable);
            model.addAttribute("status", status);
        } else {
            contactPage = contactService.findAll(pageable);
        }

        List<Integer> years = contactService.findDistinctYears();

        model.addAttribute("contacts", contactPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contactPage.getTotalPages());
        model.addAttribute("totalElements", contactPage.getTotalElements());
        model.addAttribute("hasPrevious", contactPage.hasPrevious());
        model.addAttribute("hasNext", contactPage.hasNext());
        model.addAttribute("years", years);
        model.addAttribute("view", "admin/contactCRUD");

        return "admin/layout";
    }

    @GetMapping("/markProcessed/{id}")
    public String markProcessed(@PathVariable("id") int id,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "page", defaultValue = "0") int page,
            RedirectAttributes rAttributes) {

        Contact contact = contactService.findById(id);
        if (contact != null) {
            contact.setStatus(true);
            contactService.updateContact(id, contact);
            rAttributes.addFlashAttribute("success", "Đã đánh dấu là đã xử lý.");
            System.out.println("Contact with ID " + id + " has been marked as processed.");
        } else {
            rAttributes.addFlashAttribute("error", "Không tìm thấy liên hệ.");
        }

        // Quay lại trang hiện tại và giữ trạng thái lọc
        String redirectUrl = "redirect:/Contact/index/admin?page=" + page;
        if (status != null)
            redirectUrl += "&status=" + status;
        if (year != null)
            redirectUrl += "&year=" + year;
        if (month != null)
            redirectUrl += "&month=" + month;
        return redirectUrl;
    }

    @GetMapping("/viewDetail/{id}")
    @ResponseBody
    public Contact viewDetail(@PathVariable("id") int id) {
        Contact contact = contactService.findById(id);
        if (contact == null) {
            return new Contact();
        }
        return contact;
    }
}