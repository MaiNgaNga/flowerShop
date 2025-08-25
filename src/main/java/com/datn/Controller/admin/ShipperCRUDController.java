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

import com.datn.Service.ShipperService;
import com.datn.Service.UserService;
import com.datn.model.Shipper;
import com.datn.model.User;
import com.datn.validator.CCCDValidator;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/Shipper")
public class ShipperCRUDController {

    @Autowired
    private ShipperService shipperService;

    @Autowired
    private UserService userService;

    @ModelAttribute("users")
    public List<User> getAllUsers() {
        return userService.findAllNonShipper();
    }

    @ModelAttribute("shippers")
    public List<Shipper> getAllShippers() {
        return shipperService.findAll();
    }

    @GetMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Shipper> shipperPage = shipperService.findByAllShippers(pageable);

        Shipper shipper = new Shipper();
        shipper.setUser(new User()); // Khởi tạo user để tránh NullPointerException
        model.addAttribute("shipper", shipper);
        model.addAttribute("currentPage", shipperPage.getNumber());
        model.addAttribute("totalPages", shipperPage.getTotalPages());
        model.addAttribute("totalElements", shipperPage.getTotalElements());
        model.addAttribute("hasPrevious", shipperPage.hasPrevious());
        model.addAttribute("hasNext", shipperPage.hasNext());
        model.addAttribute("shippers", shipperPage.getContent());
        model.addAttribute("view", "admin/shipperCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("shipper") Shipper shipper,
            Errors errors,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        if (shipper.getUser() == null) {
            model.addAttribute("error", "Vui lòng chọn người dùng cho shipper!");
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        if (shipper.getUser().getId() == 0) {
            model.addAttribute("error", "Vui lòng chọn người dùng hợp lệ!");
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        if (shipperService.existsByUserId(shipper.getUser().getId())) {
            model.addAttribute("error", "Người dùng này đã là shipper!");
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        User user = userService.findByID(shipper.getUser().getId());
        if (user != null) {
            user.setRole(2);
            userService.update(user);
        }

        shipperService.save(shipper);
        redirectAttributes.addFlashAttribute("success", "Thêm shipper thành công!");
        return "redirect:/Shipper/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Shipper shipper = shipperService.findById(id);
        Pageable pageable = PageRequest.of(page, 10);
        Page<Shipper> shipperPage = shipperService.findByAllShippers(pageable);

        model.addAttribute("shipper", shipper);
        model.addAttribute("shippers", shipperPage.getContent());
        model.addAttribute("currentPage", shipperPage.getNumber());
        model.addAttribute("totalPages", shipperPage.getTotalPages());
        model.addAttribute("totalElements", shipperPage.getTotalElements());
        model.addAttribute("hasPrevious", shipperPage.hasPrevious());
        model.addAttribute("hasNext", shipperPage.hasNext());
        model.addAttribute("view", "admin/shipperCRUD");
        return "admin/layout";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("shipper") Shipper shipper,
            Errors errors,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("shippers", shipperService.findAll());
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        Shipper existing = shipperService.findById(shipper.getId());
        if (existing == null) {
            model.addAttribute("error", "Không tìm thấy shipper để cập nhật");
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        if (!CCCDValidator.isValidCCCD(shipper.getCccd())) {
            model.addAttribute("errorCCCD", "CCCD không hợp lệ! Phải gồm 12 chữ số và mã tỉnh hợp lệ.");
            model.addAttribute("view", "admin/shipperCRUD");
            return "admin/layout";
        }

        if (shipper.getStatus() == null) {
            shipper.setStatus(existing.getStatus());
        } else {
            existing.setStatus(shipper.getStatus());
        }
        shipper.setUser(existing.getUser());
        shipperService.update(shipper);
        redirectAttributes.addFlashAttribute("success", "Cập nhật shipper thành công!");
        return "redirect:/Shipper/edit/" + shipper.getId();
    }

    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            Shipper shipper = shipperService.findById(id);
            if (shipper != null) {
                User user = userService.findByID(shipper.getUser().getId());
                if (user != null) {
                    user.setRole(0);
                    userService.update(user);
                }
                shipperService.save(shipper);
                redirectAttributes.addFlashAttribute("success", "Đã gỡ shipper thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy shipper để xóa.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/Shipper/index";
    }

    @GetMapping("/searchByName")
    public String searchByName(@RequestParam("name") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Shipper> result = shipperService.searchByName(name, pageable);

        model.addAttribute("name", name);
        model.addAttribute("shippers", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("shipper", new Shipper());
        model.addAttribute("view", "admin/shipperCRUD");
        return "admin/layout";
    }

    @GetMapping("/searchByStatus")
    public String searchByStatus(@RequestParam("status") Boolean status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Shipper> shipperPage = shipperService.searchByStatus(status, pageable);

        model.addAttribute("status", status);
        model.addAttribute("shippers", shipperPage.getContent());
        model.addAttribute("currentPage", shipperPage.getNumber());
        model.addAttribute("totalPages", shipperPage.getTotalPages());
        model.addAttribute("totalElements", shipperPage.getTotalElements());
        model.addAttribute("hasPrevious", shipperPage.hasPrevious());
        model.addAttribute("hasNext", shipperPage.hasNext());
        model.addAttribute("shipper", new Shipper());
        model.addAttribute("view", "admin/shipperCRUD");
        return "admin/layout";
    }
}