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

import com.datn.Service.WardService;
import com.datn.Service.ZoneService;
import com.datn.model.Ward;
import com.datn.model.Zone;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/Ward")
public class WardController {

    @Autowired
    private WardService wardService;

    @Autowired
    private ZoneService zoneService;

    @ModelAttribute("zones")
    public List<Zone> getAllZones() {
        return zoneService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model,
            @RequestParam(value = "zoneId", required = false) Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tab", defaultValue = "list") String tab) {

        if (zoneId == null) {
            List<Zone> zones = zoneService.findAll();
            if (!zones.isEmpty()) {
                zoneId = zones.get(0).getId();
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Ward> wardPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            wardPage = wardService.searchByName(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else {
            wardPage = wardService.findAllPaginatedByZoneId(zoneId, pageable);
        }

        model.addAttribute("wards", wardPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", wardPage.getTotalPages());
        model.addAttribute("totalElements", wardPage.getTotalElements());
        model.addAttribute("hasPrevious", wardPage.hasPrevious());
        model.addAttribute("hasNext", wardPage.hasNext());
        model.addAttribute("zoneId", zoneId);
        model.addAttribute("ward", new Ward());
        model.addAttribute("view", "admin/wardCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("ward") Ward ward,
            Errors errors,
            @RequestParam(value = "zoneId", required = false) Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        if (zoneId == null) {
            List<Zone> zones = zoneService.findAll();
            if (!zones.isEmpty()) {
                zoneId = zones.get(0).getId();
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Ward> wardPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            wardPage = wardService.searchByName(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else {
            wardPage = wardService.findAllPaginatedByZoneId(zoneId, pageable);
        }

        if (errors.hasErrors()) {
            model.addAttribute("wards", wardPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", wardPage.getTotalPages());
            model.addAttribute("totalElements", wardPage.getTotalElements());
            model.addAttribute("hasPrevious", wardPage.hasPrevious());
            model.addAttribute("hasNext", wardPage.hasNext());
            model.addAttribute("zoneId", zoneId);
            model.addAttribute("view", "admin/wardCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }

        try {
            wardService.create(ward);
            redirectAttributes.addFlashAttribute("success", "Thêm xã/phường thành công!");
            return "redirect:/Ward/index?tab=list&zoneId=" + zoneId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("wards", wardPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", wardPage.getTotalPages());
            model.addAttribute("totalElements", wardPage.getTotalElements());
            model.addAttribute("hasPrevious", wardPage.hasPrevious());
            model.addAttribute("hasNext", wardPage.hasNext());
            model.addAttribute("zoneId", zoneId);
            model.addAttribute("view", "admin/wardCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model,
            @PathVariable("id") Long id,
            @RequestParam(value = "zoneId", required = false) Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tab", defaultValue = "edit") String tab) {

        if (zoneId == null) {
            List<Zone> zones = zoneService.findAll();
            if (!zones.isEmpty()) {
                zoneId = zones.get(0).getId();
            }
        }

        Ward ward = wardService.findById(id);
        Pageable pageable = PageRequest.of(page, size);
        Page<Ward> wardPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            wardPage = wardService.searchByName(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else {
            wardPage = wardService.findAllPaginatedByZoneId(zoneId, pageable);
        }

        model.addAttribute("ward", ward);
        model.addAttribute("wards", wardPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", wardPage.getTotalPages());
        model.addAttribute("totalElements", wardPage.getTotalElements());
        model.addAttribute("hasPrevious", wardPage.hasPrevious());
        model.addAttribute("hasNext", wardPage.hasNext());
        model.addAttribute("zoneId", zoneId);
        model.addAttribute("view", "admin/wardCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @RequestMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("ward") Ward ward,
            Errors errors,
            @RequestParam(value = "zoneId", required = false) Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        if (zoneId == null) {
            List<Zone> zones = zoneService.findAll();
            if (!zones.isEmpty()) {
                zoneId = zones.get(0).getId();
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Ward> wardPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            wardPage = wardService.searchByName(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else {
            wardPage = wardService.findAllPaginatedByZoneId(zoneId, pageable);
        }

        if (errors.hasErrors()) {
            model.addAttribute("wards", wardPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", wardPage.getTotalPages());
            model.addAttribute("totalElements", wardPage.getTotalElements());
            model.addAttribute("hasPrevious", wardPage.hasPrevious());
            model.addAttribute("hasNext", wardPage.hasNext());
            model.addAttribute("zoneId", zoneId);
            model.addAttribute("view", "admin/wardCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }

        try {
            wardService.update(ward);
            redirectAttributes.addFlashAttribute("success", "Cập nhật xã/phường thành công!");
            return "redirect:/Ward/edit/" + ward.getId() + "?page=" + page + "&size=" + size
                    + (keyword != null ? "&keyword=" + keyword : "") + "&zoneId=" + zoneId + "&tab=" + tab;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("wards", wardPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", wardPage.getTotalPages());
            model.addAttribute("totalElements", wardPage.getTotalElements());
            model.addAttribute("hasPrevious", wardPage.hasPrevious());
            model.addAttribute("hasNext", wardPage.hasNext());
            model.addAttribute("zoneId", zoneId);
            model.addAttribute("view", "admin/wardCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model,
            @ModelAttribute("ward") Ward ward,
            Errors errors,
            @PathVariable("id") Long id,
            @RequestParam(value = "zoneId", required = false) Long zoneId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        if (zoneId == null) {
            List<Zone> zones = zoneService.findAll();
            if (!zones.isEmpty()) {
                zoneId = zones.get(0).getId();
            }
        }

        try {
            wardService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa xã/phường!");
            return "redirect:/Ward/index?tab=list&zoneId=" + zoneId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Ward/edit/" + id + "?page=" + page + "&size=" + size
                    + (keyword != null ? "&keyword=" + keyword : "") + "&zoneId=" + zoneId + "&tab=" + tab;
        }
    }
}