package com.datn.Controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private UserService UserService;

    @Autowired
    private ShipperService shipperService;
    
    @ModelAttribute("Users")
    public List<User> getAllUsers() {
        return UserService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("User", new User());
          model.addAttribute("isEdit", false);
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model, @Valid @ModelAttribute("User") User User,
            Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("Users", UserService.findAll());
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }
        if (UserService.findByEmail(User.getEmail()) != null) {
            model.addAttribute("error", "Email này đã tồn tại!");
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";            
        }
        User exitSDT = UserService.findBySdt(User.getSdt());
        if (exitSDT != null && exitSDT.getId() != User.getId()) {
            model.addAttribute("error", "Số điện thoại này đã tồn tại!");
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";
        }
        if (User.getPassword() == null || User.getPassword().length() < 4 ) {
            model.addAttribute("errorPass", "Mật khẩu phải có ít nhất 4 ký tự!");
            model.addAttribute("view", "admin/UserCRUD");
            return "admin/layout";   
        }
        try {   

            UserService.create(User);
            if (User.getRole() == 2) {

                Shipper shipper = new Shipper();
                shipper.setUser(User);
                shipper.setTotalsNumberOrder(0);
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

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") int id) {
        User User = UserService.findByID(id);
        model.addAttribute("User", User);
        model.addAttribute("isEdit", true);
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }

    @PostMapping("/update")
public String update(Model model, @Valid @ModelAttribute("User") User User,
        Errors errors, RedirectAttributes redirectAttributes) {
    if (errors.hasErrors()) {
        model.addAttribute("isEdit", true);
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }

    User exi = UserService.findByID(User.getId());
    if (User.getPassword() == null || User.getPassword().isBlank()) {
        User.setPassword(exi.getPassword());
    }

    User exitSDT = UserService.findBySdt(User.getSdt());
    if (exitSDT != null && exitSDT.getId() != User.getId()) {
        model.addAttribute("error", "Số điện thoại này đã tồn tại!");
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }

    try {
        Shipper exiShip = shipperService.findByUserId(User.getId());

        // Nếu trước đó là shipper, giờ chuyển vai trò khác => ngưng hoạt động shipper
        if (exi.getRole() == 2 && User.getRole() != 2 && exiShip != null) {
            exiShip.setStatus(false);
            shipperService.save(exiShip);
        }

        // Nếu chuyển từ vai trò khác thành shipper
        if (exi.getRole() != 2 && User.getRole() == 2) {
            if (exiShip != null) {
                // Đã có shipper → kích hoạt lại
                exiShip.setStatus(true);
                shipperService.save(exiShip);
            } else {
                // Chưa có → tạo mới shipper
                Shipper shipper = new Shipper();
                shipper.setUser(User);
                shipper.setTotalsNumberOrder(0);
                shipper.setVehicle("");
                shipper.setCccd("");
                shipper.setAddress("");
                shipper.setHistoryOrder("");
                shipper.setStatus(true);
                shipperService.save(shipper);
            }
        }

          // ✅ Nếu user có role = 2 (shipper) và bị chuyển sang trạng thái không hoạt động (status=false)
        if (User.getRole() == 2 && !User.getStatus() && exiShip != null) {
            exiShip.setStatus(false);
            shipperService.save(exiShip);
        }

        // ✅ Nếu user là shipper và chuyển từ off → on thì cũng bật shipper
        if (User.getRole() == 2 && User.getStatus() && exiShip != null) {
            exiShip.setStatus(true);
            shipperService.save(exiShip);
        }


        UserService.update(User);
        redirectAttributes.addFlashAttribute("success", "Cập nhật User thành công!");
        return "redirect:/User/edit/" + User.getId();

    } catch (IllegalArgumentException e) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("view", "admin/UserCRUD");
        return "admin/layout";
    }
}


    @RequestMapping("/delete/{id}")
    public String delete(Model model, @ModelAttribute("User") User User,
            Errors errors, @PathVariable("id") int id, RedirectAttributes redirectAttributes) {

        try {
            UserService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa User!");
            return "redirect:/User/index";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Category/edit/" + User.getId();
        }

    }

}
