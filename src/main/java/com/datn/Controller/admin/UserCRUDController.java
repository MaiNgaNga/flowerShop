// package com.datn.Controller.admin;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.validation.Errors;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import jakarta.validation.Valid;
// import com.datn.model.User;
// import com.datn.Service.UserService;

// @Controller
// @RequestMapping("/User")
// public class UserCRUDController {

// @Autowired
// private UserService UserService;

// @ModelAttribute("Users")
// public List<User> getAllUsers() {
// return UserService.findAll();
// }

// @RequestMapping("/index")
// public String index(Model model) {
// model.addAttribute("User", new User());
// model.addAttribute("view", "admin/UserCRUD");
// return "admin/layout";
// }

// @PostMapping("/create")
// public String create(Model model, @Valid @ModelAttribute("User") User User,
// Errors errors, RedirectAttributes redirectAttributes) {
// if (errors.hasErrors()) {
// model.addAttribute("view", "admin/UserCRUD");
// return "admin/layout";
// }
// try {
// UserService.create(User);
// redirectAttributes.addFlashAttribute("success", "Thêm User thành công!");
// return "redirect:/User/index";
// } catch (IllegalArgumentException e) {
// model.addAttribute("error", e.getMessage());
// model.addAttribute("view", "admin/UserCRUD");
// return "admin/layout";
// }
// }

// @GetMapping("/edit/{id}")
// public String edit(Model model, @PathVariable("id") int id) {
// User User = UserService.findByID(id);
// model.addAttribute("User", User);
// model.addAttribute("view", "admin/UserCRUD");
// return "admin/layout";
// }

// @RequestMapping("/update")
// public String update(Model model, @Valid @ModelAttribute("User") User User,
// Errors errors, RedirectAttributes redirectAttributes) {
// if (errors.hasErrors()) {
// model.addAttribute("view", "admin/UserCRUD");
// return "admin/layout";
// }
// try {
// UserService.update(User);
// redirectAttributes.addFlashAttribute("success", "Cập nhật User thành công!");
// return "redirect:/User/edit/" + User.getId();

// } catch (IllegalArgumentException e) {

// model.addAttribute("error", e.getMessage());
// model.addAttribute("view", "admin/UserCRUD");
// return "admin/layout";
// }

// }

// @RequestMapping("/delete/{id}")
// public String delete(Model model, @ModelAttribute("User") User User,
// Errors errors, @PathVariable("id") int id, RedirectAttributes
// redirectAttributes) {

// try {
// UserService.deleteById(id);
// redirectAttributes.addFlashAttribute("success", "Đã xóa User!");
// return "redirect:/User/index";

// } catch (IllegalArgumentException e) {

// redirectAttributes.addFlashAttribute("error", e.getMessage());
// return "redirect:/Category/edit/" + User.getId();
// }

// }

// }
