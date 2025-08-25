package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.datn.model.Shipper;
import com.datn.model.User;
import com.datn.Service.ShipperService;
import com.datn.Service.UserService;

@Controller
@RequestMapping("/User")
public class UserCRUDController {

    @Autowired
    private UserService userService;

    @Autowired
    private ShipperService shipperService;

    // Lấy toàn bộ danh sách user và gán vào thuộc tính "Users"
    @ModelAttribute("Users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // Trang hiển thị form thêm user
    @RequestMapping("/index")
    public String index(Model model,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "tab", defaultValue = "edit") String tab) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;
        if (role != null) {
            if (role == 0) {
                userPage = userService.findByRole(0, pageable);
            } else if (role == 1) {
                userPage = userService.findByRole(1, pageable);
            } else if (role == 2) {
                userPage = userService.findByRole(2, pageable);
            } else if (role == 3) {
                userPage = userService.findByRole(3, pageable);
            } else {
                userPage = userService.findAllUserPage(pageable);
            }
        } else {
            userPage = userService.findAllUserPage(pageable);
        }

        model.addAttribute("Users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("hasPrevious", userPage.hasPrevious());
        model.addAttribute("hasNext", userPage.hasNext());
        model.addAttribute("size", size);
        model.addAttribute("User", new User());
        model.addAttribute("isEdit", false);
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }

    // Xử lý khi người dùng submit form tạo user mới
    @PostMapping("/create")
    public String create(Model model, @Valid @ModelAttribute("User") User user,
            Errors errors, RedirectAttributes redirectAttributes) {

        // Nếu có lỗi validation thì quay lại form
        if (errors.hasErrors()) {
            model.addAttribute("Users", userService.findAll());
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }

        // Kiểm tra độ dài mật khẩu
        if (user.getPassword() == null || user.getPassword().length() < 4) {
            model.addAttribute("errorPass", "Mật khẩu phải có ít nhất 4 ký tự!");
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }

        try {
            // Tạo user
            userService.create(user);

            // Nếu user có role là shipper (2) → tạo mới shipper
            if (user.getRole() == 2) {
                Shipper shipper = new Shipper();
                shipper.setUser(user);
                shipper.setTotalsNumberOrder(0);
                shipper.setStatus(true);
                shipper.setVehicle("");
                shipper.setCccd("");
                shipper.setAddress("");
                shipper.setHistoryOrder("");
                shipperService.save(shipper);
            }

            redirectAttributes.addFlashAttribute("success", "Thêm User thành công!");
            return "redirect:/User/index";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }
    }

    // Truy cập vào trang chỉnh sửa user
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") int id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        User user = userService.findByID(id);
        Page<User> userPage = userService.findAllUserPage(pageable);

        model.addAttribute("User", user);
        model.addAttribute("Users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("hasPrevious", userPage.hasPrevious());
        model.addAttribute("hasNext", userPage.hasNext());
        model.addAttribute("size", size);
        model.addAttribute("isEdit", true);
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }

    // Xử lý cập nhật user
    @PostMapping("/update")
    public String update(Model model, @Valid @ModelAttribute("User") User user,
            Errors errors, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }

        User exi = userService.findByID(user.getId());

        // Nếu không nhập password thì giữ nguyên password cũ
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(exi.getPassword());
        }

        // Kiểm tra trùng số điện thoại
        User exitSDT = userService.findBySdt(user.getSdt());
        if (exitSDT != null && exitSDT.getId() != user.getId()) {
            model.addAttribute("error", "Số điện thoại này đã tồn tại!");
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }

        try {
            Shipper exiShip = shipperService.findByUserId(user.getId());

            // Nếu user từng là shipper → đổi role → ngừng hoạt động shipper
            if (exi.getRole() == 2 && user.getRole() != 2 && exiShip != null) {
                exiShip.setStatus(false);
                shipperService.save(exiShip);
            }

            // Nếu user từng không là shipper → chuyển sang shipper
            if (exi.getRole() != 2 && user.getRole() == 2) {
                if (exiShip != null) {
                    exiShip.setStatus(true); // kích hoạt lại shipper
                    shipperService.save(exiShip);
                } else {
                    // tạo mới shipper
                    Shipper shipper = new Shipper();
                    shipper.setUser(user);
                    shipper.setTotalsNumberOrder(0);
                    shipper.setVehicle("");
                    shipper.setCccd("");
                    shipper.setAddress("");
                    shipper.setHistoryOrder("");
                    shipper.setStatus(true);
                    shipperService.save(shipper);
                }
            }

            // Nếu user đang là shipper mà bị set trạng thái không hoạt động
            if (user.getRole() == 2 && !user.getStatus() && exiShip != null) {
                exiShip.setStatus(false);
                shipperService.save(exiShip);
            }

            // Nếu user là shipper và được bật lại
            if (user.getRole() == 2 && user.getStatus() && exiShip != null) {
                exiShip.setStatus(true);
                shipperService.save(exiShip);
            }

            userService.update(user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật User thành công!");
            return "redirect:/User/edit/" + user.getId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }
    }

    // Xử lý xóa user
    @RequestMapping("/delete/{id}")
    public String delete(Model model, @ModelAttribute("User") User user,
            Errors errors, @PathVariable("id") int id, RedirectAttributes redirectAttributes) {

        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa User!");
            return "redirect:/User/index";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/User/index";
        }
    }

    // Tìm kiếm user theo tên
    @GetMapping("/searchByName")
    public String searchByName(
            @RequestParam("name") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> result = userService.searchByName(name, pageable);

        model.addAttribute("name", name);
        model.addAttribute("Users", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("hasPrevious", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("size", size);
        model.addAttribute("User", new User());
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }
}