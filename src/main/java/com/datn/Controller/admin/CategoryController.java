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

import com.datn.Service.CategoryService;
import com.datn.model.Category;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/Category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;

    @ModelAttribute("categorys")
    public List<Category> getAllCategories() {
        return categoryService.findAll(); 
    }

    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("view", "admin/categoryCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model, @Valid @ModelAttribute("category") Category category,
                         Errors errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/categoryCRUD");
            return "admin/layout";
        }
        try {
            categoryService.create(category);
            redirectAttributes.addFlashAttribute("success", "Thêm loại hoa thành công!");
            return "redirect:/Category/index";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/categoryCRUD");
            return "admin/layout";        }
    }


   @GetMapping("/edit/{id}")
   public String edit(Model model, @PathVariable("id") int id) {
      Category category= categoryService.findByID(id);
      model.addAttribute("category", category);
      model.addAttribute("view", "admin/categoryCRUD");
        return "admin/layout";
   }
     
    @RequestMapping("/update")
      public String update(Model model,@Valid @ModelAttribute("category") Category category,
      Errors errors, RedirectAttributes redirectAttributes) {
        
        try {
            categoryService.update(category);
            redirectAttributes.addFlashAttribute("success", "Cập nhật loại hoa thành công!");
            return "redirect:/Category/edit/"+category.getId();

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/categoryCRUD");
            return "admin/layout";        
        }
         
      }
    @RequestMapping("/delete/{id}")
      public String delete(Model model,  @ModelAttribute("category") Category category,
      Errors errors, @PathVariable("id") int id,RedirectAttributes redirectAttributes) {
        
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa loại hoa!");
            return "redirect:/Category/index";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",e.getMessage());
            return "redirect:/Category/edit/"+category.getId();
       
        } 
       
      }

    
}
