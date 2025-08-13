package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.ZoneService;
import com.datn.model.Zone;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/Zone")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    // Load danh sách zone cho dropdown (nếu cần)
    @ModelAttribute("zones")
    public List<Zone> getAllZones() {
        return zoneService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "tab", defaultValue = "list") String tab) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Zone> zonePage;

        if (keyword != null && !keyword.isEmpty()) {
            zonePage = zoneService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            zonePage = zoneService.findAllPaginated(pageable);
        }

        model.addAttribute("zones", zonePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", zonePage.getTotalPages());

        model.addAttribute("zone", new Zone());
        model.addAttribute("view", "admin/zoneCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
                         @Valid @ModelAttribute("zone") Zone zone,
                         Errors errors,
                         @RequestParam(value = "tab", defaultValue = "edit") String tab,
                         RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/zoneCRUD");
            return "admin/layout";
        }
        try {
            zoneService.create(zone);
            redirectAttributes.addFlashAttribute("success", "Thêm zone thành công!");
            return "redirect:/Zone/index?tab=list";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/zoneCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model,
                       @PathVariable("id") Long id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "tab", defaultValue = "edit") String tab) {

        Zone zone = zoneService.findById(id);
        model.addAttribute("zone", zone);

        Pageable pageable = PageRequest.of(page, size);
        Page<Zone> zonePage;
        if (keyword != null && !keyword.isEmpty()) {
            zonePage = zoneService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            zonePage = zoneService.findAllPaginated(pageable);
        }

        model.addAttribute("zones", zonePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", zonePage.getTotalPages());

        model.addAttribute("view", "admin/zoneCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @RequestMapping("/update")
    public String update(Model model,
                         @Valid @ModelAttribute("zone") Zone zone,
                         Errors errors,
                         @RequestParam(value = "tab", defaultValue = "edit") String tab,
                         RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/zoneCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }

        try {
            zoneService.update(zone);
            redirectAttributes.addFlashAttribute("success", "Cập nhật zone thành công!");
            return "redirect:/Zone/edit/" + zone.getId() + "?tab=" + tab;

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/zoneCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model,
                         @ModelAttribute("zone") Zone zone,
                         Errors errors,
                         @PathVariable("id") Long id,
                         @RequestParam(value = "tab", defaultValue = "edit") String tab,
                         RedirectAttributes redirectAttributes) {

        try {
            zoneService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa zone!");
            return "redirect:/Zone/index?tab=list";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Zone/edit/" + zone.getId() + "?tab=" + tab;
        }
    }

}
