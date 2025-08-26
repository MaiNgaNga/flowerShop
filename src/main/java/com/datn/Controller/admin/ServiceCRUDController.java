package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.ServiceService;
import com.datn.model.ServiceEntity;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/Service")
public class ServiceCRUDController {

    @Autowired
    private ServiceService serviceService;

    @ModelAttribute("services")
    public List<ServiceEntity> getAllServices() {
        return serviceService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tab", defaultValue = "list") String tab) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceEntity> servicePage;

        // Xử lý lọc theo trạng thái và tìm kiếm
        if ((keyword != null && !keyword.isEmpty()) && (status != null && !status.isEmpty())) {
            // Lọc cả keyword và status
            Boolean available = Boolean.parseBoolean(status);
            servicePage = serviceService.searchByNameAndStatus(keyword, available, pageable);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
        } else if (keyword != null && !keyword.isEmpty()) {
            // Chỉ lọc theo keyword
            servicePage = serviceService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (status != null && !status.isEmpty()) {
            // Chỉ lọc theo status
            Boolean available = Boolean.parseBoolean(status);
            servicePage = serviceService.findByStatus(available, pageable);
            model.addAttribute("status", status);
        } else {
            // Không lọc gì, hiển thị tất cả
            servicePage = serviceService.findByAllService(pageable);
        }

        model.addAttribute("services", servicePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicePage.getTotalPages());
        model.addAttribute("totalElements", servicePage.getTotalElements());
        model.addAttribute("hasPrevious", servicePage.hasPrevious());
        model.addAttribute("hasNext", servicePage.hasNext());
        model.addAttribute("service", new ServiceEntity());
        model.addAttribute("view", "admin/ServiceCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("service") ServiceEntity service, Errors errors,
            @RequestParam("image1") MultipartFile image1,
            @RequestParam("image2") MultipartFile image2,
            @RequestParam("image3") MultipartFile image3,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {

        System.out.println("da vao create");

        // Check validate từ entity (tên, mô tả,...)
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/ServiceCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }

        // Kiểm tra ảnh chính có rỗng không
        if (image1 == null || image1.isEmpty()) {
            model.addAttribute("error", "Ảnh chính không được để trống!");
            model.addAttribute("view", "admin/ServiceCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }

        try {
            serviceService.create(service, image1, image2, image3);
            redirectAttributes.addFlashAttribute("success", "Thêm dịch vụ thành công!");
            return "redirect:/Service/index?tab=" + tab;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/ServiceCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "tab", defaultValue = "edit") String tab) {

        ServiceEntity service = serviceService.findByID(id);
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceEntity> servicePage;

        // Áp dụng cùng logic filter như trong index
        if ((keyword != null && !keyword.isEmpty()) && (status != null && !status.isEmpty())) {
            Boolean available = Boolean.parseBoolean(status);
            servicePage = serviceService.searchByNameAndStatus(keyword, available, pageable);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
        } else if (keyword != null && !keyword.isEmpty()) {
            servicePage = serviceService.searchByName(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (status != null && !status.isEmpty()) {
            Boolean available = Boolean.parseBoolean(status);
            servicePage = serviceService.findByStatus(available, pageable);
            model.addAttribute("status", status);
        } else {
            servicePage = serviceService.findByAllService(pageable);
        }

        model.addAttribute("services", servicePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicePage.getTotalPages());
        model.addAttribute("totalElements", servicePage.getTotalElements());
        model.addAttribute("hasPrevious", servicePage.hasPrevious());
        model.addAttribute("hasNext", servicePage.hasNext());
        model.addAttribute("service", service);
        model.addAttribute("view", "admin/ServiceCRUD");
        model.addAttribute("activeTab", tab);
        return "admin/layout";
    }

    @PostMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("service") ServiceEntity service, Errors errors,
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3,
            @RequestParam(value = "oldImages", required = false) String[] oldImages,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/ServiceCRUD");
            return "admin/layout";
        }
        try {
            serviceService.update(service, image1, image2, image3, oldImages);
            redirectAttributes.addFlashAttribute("success", "Cập nhật dịch vụ thành công!");
            return "redirect:/Service/index?tab=" + tab;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/ServiceCRUD");
            model.addAttribute("activeTab", tab);
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(@ModelAttribute("service") ServiceEntity service,
            @PathVariable("id") long id,
            @RequestParam(value = "tab", defaultValue = "edit") String tab,
            RedirectAttributes redirectAttributes) {
        try {
            serviceService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa dịch vụ!");
            return "redirect:/Service/index?tab=" + tab;
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xóa dịch vụ vì có liên kết với các thực thể khác!");
            return "redirect:/Service/index?tab=" + tab;

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Service/edit/" + service.getId() + "?tab=" + tab;
        }
    }
}