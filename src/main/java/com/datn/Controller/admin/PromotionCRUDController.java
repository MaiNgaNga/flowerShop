package com.datn.Controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Gán danh sách tất cả khuyến mãi vào model với key là 'promotions'
    @ModelAttribute("promotions")
    public Iterable<Promotion> getAllPromotions() {
        return promotionService.findAll();
    }

    // Hiển thị trang danh sách khuyến mãi (kèm theo form thêm mới)
    @GetMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionService.findByAllPromotion(pageable);

        // ✅ Kiểm tra và cập nhật status nếu hết hạn
        for (Promotion promo : promotionPage.getContent()) {
            if (promo.getEndDate() != null && promo.getEndDate().isBefore(LocalDate.now())) {
                promo.setStatus(false); // hết hạn
                promo.setUpdatedDate(LocalDateTime.now()); // updatedDate thì để LocalDateTime vẫn ok
                promotionService.update(promo);
            }

        }

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("promotions", promotionPage.getContent());

        Promotion promotion = new Promotion();
        // Nếu là thêm mới, set ngày hiện tại
        promotion.setCreatedDate(LocalDateTime.now());
        promotion.setUpdatedDate(LocalDateTime.now());
        model.addAttribute("promotion", promotion);

        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    // Xử lý thêm mới khuyến mãi
    @PostMapping("/create")
    public String create(Model model, @Valid @ModelAttribute("promotion") Promotion promotion, Errors errors,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra lỗi ràng buộc từ form
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
        if (promotion.getEndDate() != null && promotion.getStartDate() != null
                && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            model.addAttribute("errorEndDate", "Ngày kết thúc phải trước ngày bắt đầu!");
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        // Kiểm tra loại giảm giá và giá trị giảm giá hợp lệ
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
            // Gán ngày tạo hiện tại
            promotion.setCreatedDate(LocalDateTime.now());
            // Lưu khuyến mãi vào DB
            promotionService.create(promotion);
            // Gửi thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Thêm khuyến mãi thành công!");
            return "redirect:/Promotion/index";
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi nếu xảy ra ngoại lệ
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }
    }

    // Truy cập form chỉnh sửa khuyến mãi theo ID
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Promotion promotion = promotionService.findByID(id);
        Pageable pageable = PageRequest.of(page, 10);
        Page<Promotion> result = promotionDAO.findAll(pageable);

        // ✅ Kiểm tra và cập nhật status nếu hết hạn
        for (Promotion promo : result.getContent()) {
            if (promo.getEndDate() != null && promo.getEndDate().isBefore(LocalDate.now())) {
                promo.setStatus(false); // hết hạn
                promo.setUpdatedDate(LocalDateTime.now()); // updatedDate thì để LocalDateTime vẫn ok
                promotionService.update(promo);
            }
        }

        model.addAttribute("promotion", promotion);
        model.addAttribute("promotions", result.getContent());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    // Xử lý cập nhật khuyến mãi
    @PostMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("promotion") Promotion promotion,
            Errors errors,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra lỗi form
        if (errors.hasErrors()) {
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        // Kiểm tra logic ngày
        if (promotion.getEndDate() != null && promotion.getStartDate() != null
                && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            model.addAttribute("errorEndDate", "Ngày kết thúc phải trước ngày bắt đầu!");
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        // Kiểm tra loại và giá trị giảm giá hợp lệ
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
            // Lấy bản ghi hiện tại từ DB
            Promotion existing = promotionService.findByID(promotion.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy khuyến mãi để cập nhật");
            }

            // Giữ nguyên ngày tạo cũ
            promotion.setCreatedDate(existing.getCreatedDate());

            // Cập nhật ngày hiện tại cho updatedDate
            promotion.setUpdatedDate(LocalDateTime.now());

            // Cập nhật DB
            promotionService.update(promotion);

            // Gửi thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công!");
            return "redirect:/Promotion/edit/" + promotion.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }
    }

    // Xử lý xóa khuyến mãi theo ID
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

    // Tìm kiếm khuyến mãi theo khoảng ngày
    @GetMapping("/search")
    public String search(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // Nếu thiếu ngày => báo lỗi + load lại trang
        if (fromDate == null || toDate == null) {
            model.addAttribute("error", "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");

            Pageable pageable = PageRequest.of(page, 10);
            Page<Promotion> result = promotionService.findByAllPromotion(pageable);

            model.addAttribute("promotions", result.getContent());
            model.addAttribute("currentPage", result.getNumber());
            model.addAttribute("totalPages", result.getTotalPages());
            model.addAttribute("promotion", new Promotion());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        // Đủ ngày thì lọc theo ngày
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

    // Tìm kiếm khuyến mãi theo tiêu đề
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
