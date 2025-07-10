package com.datn.Controller.admin;

import java.lang.ProcessBuilder.Redirect;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.datn.Service.PromotionService;
import com.datn.dao.PromotionDAO;
import com.datn.model.Promotion;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/Promotion")
public class PromotionCRUDController {

    @Autowired
    private PromotionService promotionService;
    @Autowired
    private PromotionDAO promotionDAO;

    @ModelAttribute("promotions")
    public Iterable<Promotion> getAllPromotions() {
        return promotionService.findAll();
    }

    @GetMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionService.findByAllPromotion(pageable);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("promotions", promotionPage.getContent());

        Promotion promotion = new Promotion();
        // Nếu là thêm mới, set ngày hiện tại
        promotion.setCreatedDate(LocalDateTime.now());
        promotion.setUpdatedDate(LocalDateTime.now());
        // ... các dòng khác ...
        model.addAttribute("promotion", promotion);

        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model, @Valid @ModelAttribute("promotion") Promotion promotion, Errors errors,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        if (promotion.getEndDate() != null && promotion.getStartDate() != null && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            model.addAttribute("errorEndDate", "Ngày kết thúc phải trước ngày bắt đầu!");
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";   
        }

        String type = promotion.getDiscountType();
        Double value = promotion.getDiscountValue();
        if ("percent".equalsIgnoreCase(type)) {
            if (value <= 0 || value > 100) {
                model.addAttribute("errorDiscount", "Giá trị giảm giá phần trăm phải nằm trong khoảng 0 - 100!");
                model.addAttribute("view", "admin/promotionCRUD");
                return "admin/layout";                
            }
            
        }else if ("amount".equalsIgnoreCase(type)) {
            if (value <= 0) {
                model.addAttribute("errorDiscount", "Giá trị giảm giá tiền tệ phải lớn hơn 0!");
                model.addAttribute("view", "admin/promotionCRUD");
                return "admin/layout";                
            }
        } else {
            model.addAttribute("error", "Loại giảm giá không hợp lệ! Chỉ chấp nhận 'percent' hoặc 'amount'.");
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";     
        }
        
        try {
            // gán ngày hiện tại cho createDate
            promotion.setCreatedDate(LocalDateTime.now());
            promotionService.create(promotion);
            redirectAttributes.addFlashAttribute("success", "Thêm khuyến mãi thành công!");
            return "redirect:/Promotion/index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

    }

    @GetMapping("/edit/{id}")

    public String edit(@PathVariable("id") Long id,
                   @RequestParam(value = "page", defaultValue = "0") int page,
                   Model model
                   ) {
    Promotion promotion = promotionService.findByID(id);
    Pageable pageable = PageRequest.of(page, 10);
    Page<Promotion> result = promotionDAO.findAll(pageable);
    model.addAttribute("promotion", promotion);
    model.addAttribute("promotions", result.getContent());
    model.addAttribute("currentPage", result.getNumber());
    model.addAttribute("totalPages", result.getTotalPages());
    model.addAttribute("view", "admin/promotionCRUD");
    return "admin/layout";
  }


    @PostMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("promotion") Promotion promotion,
            Errors errors,
            RedirectAttributes redirectAttributes) {
                if (errors.hasErrors()) {
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                 }
                if (promotion.getEndDate() != null && promotion.getStartDate() != null && promotion.getEndDate().isBefore(promotion.getStartDate())) {
                    model.addAttribute("errorEndDate", "Ngày kết thúc phải trước ngày bắt đầu!");
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                }
                String type = promotion.getDiscountType();
                Double value = promotion.getDiscountValue();
                if ("percent".equalsIgnoreCase(type)) {
                    if (value <= 0 || value > 100) {
                        model.addAttribute("errorDiscount", "Giá trị giảm giá phần trăm phải nằm trong khoảng 0 - 100!");
                        model.addAttribute("view", "admin/promotionCRUD");
                        return "admin/layout";                
                    }
                    
                } else if ("amount".equalsIgnoreCase(type)) {
                    if (value <= 0) {
                        model.addAttribute("errorDiscount", "Giá trị giảm giá tiền tệ phải lớn hơn 0!");
                        model.addAttribute("view", "admin/promotionCRUD");
                        return "admin/layout";                
                    }
                } else {
                    model.addAttribute("error", "Loại giảm giá không hợp lệ! Chỉ chấp nhận 'percent' hoặc 'amount'.");
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";     
                }
                try {
                    // Lấy bản ghi cũ từ DB
                    Promotion existing = promotionService.findByID(promotion.getId());
                    if (existing == null) {
                        throw new IllegalArgumentException("Không tìm thấy khuyến mãi để cập nhật");
                    }
                    // Giữ lại ngày tạo gốc
                    promotion.setCreatedDate(existing.getCreatedDate());

                    // Cập nhật ngày hiện tại cho updatedDate
                    promotion.setUpdatedDate(LocalDateTime.now());

                    promotionService.update(promotion);

                    redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công!");
                    return "redirect:/Promotion/edit/" + promotion.getId();
                    } catch (IllegalArgumentException e) {
                        model.addAttribute("error", e.getMessage());
                        model.addAttribute("view", "admin/promotionCRUD");
                        return "admin/layout";
                    }


    @PostMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("promotion") Promotion promotion,
            Errors errors,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy bản ghi cũ từ DB
            Promotion existing = promotionService.findByID(promotion.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy khuyến mãi để cập nhật");
            }

            // Giữ lại ngày tạo gốc
            promotion.setCreatedDate(existing.getCreatedDate());

            // Cập nhật ngày hiện tại cho updatedDate
            promotion.setUpdatedDate(LocalDateTime.now());

            promotionService.update(promotion);

            redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công!");
            return "redirect:/Promotion/edit/" + promotion.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @ModelAttribute("promotion") Promotion promotion,

    Errors errors, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        try {
            promotionService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khuyến mãi thành công!");
            return "redirect:/Promotion/index";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Promotion/edit/" + promotion.getId();
        }
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // Nếu thiếu ngày => thông báo lỗi + quay lại trang hiện tại
        if (fromDate == null || toDate == null) {
            model.addAttribute("error", "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");

            // Load danh sách khuyến mãi như mặc định
            Pageable pageable = PageRequest.of(page, 10);
            Page<Promotion> result = promotionService.findByAllPromotion(pageable);

            model.addAttribute("promotions", result.getContent());
            model.addAttribute("currentPage", result.getNumber());
            model.addAttribute("totalPages", result.getTotalPages());
            model.addAttribute("promotion", new Promotion());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        // Nếu đã chọn đủ ngày => thực hiện lọc
        Pageable pageable = PageRequest.of(page, 10);
        Page<Promotion> result = promotionService.searchByDate(fromDate, toDate, pageable);

        model.addAttribute("promotions", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    @GetMapping("/searchByTitle")
    public String searchByTitle(
            @RequestParam("title") String title,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Promotion> result = promotionService.searchByName(title, pageable);

        model.addAttribute("title", title);
        model.addAttribute("promotions", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }
}
