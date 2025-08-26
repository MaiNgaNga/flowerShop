package com.datn.Controller.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage;

        if (title != null && !title.trim().isEmpty()) {
            promotionPage = promotionService.searchByName(title.trim(), pageable);
            model.addAttribute("title", title);
        } else if (fromDate != null && toDate != null) {
            promotionPage = promotionService.searchByDate(fromDate, toDate, pageable);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
        } else {
            promotionPage = promotionService.findByAllPromotion(pageable);
        }

        for (Promotion promo : promotionPage.getContent()) {
            if (promo.getEndDate() != null && promo.getEndDate().isBefore(LocalDate.now())) {
                promo.setStatus(false);
                promo.setUpdatedDate(LocalDateTime.now());
                promotionService.update(promo);
            }
        }

        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());
        model.addAttribute("hasPrevious", promotionPage.hasPrevious());
        model.addAttribute("hasNext", promotionPage.hasNext());
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("promotion") Promotion promotion, Errors errors,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            RedirectAttributes redirectAttributes) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage;

        if (title != null && !title.trim().isEmpty()) {
            promotionPage = promotionService.searchByName(title.trim(), pageable);
            model.addAttribute("title", title);
        } else if (fromDate != null && toDate != null) {
            promotionPage = promotionService.searchByDate(fromDate, toDate, pageable);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
        } else {
            promotionPage = promotionService.findByAllPromotion(pageable);
        }

        if (errors.hasErrors()) {
            model.addAttribute("promotions", promotionPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotionPage.getTotalPages());
            model.addAttribute("totalElements", promotionPage.getTotalElements());
            model.addAttribute("hasPrevious", promotionPage.hasPrevious());
            model.addAttribute("hasNext", promotionPage.hasNext());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        if (promotion.getEndDate() != null && promotion.getStartDate() != null
                && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            model.addAttribute("errorEndDate", "Ngày kết thúc phải sau ngày bắt đầu!");
            model.addAttribute("promotions", promotionPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotionPage.getTotalPages());
            model.addAttribute("totalElements", promotionPage.getTotalElements());
            model.addAttribute("hasPrevious", promotionPage.hasPrevious());
            model.addAttribute("hasNext", promotionPage.hasNext());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        String type = promotion.getDiscountType();
        Double value = promotion.getDiscountValue();
        if ("percent".equalsIgnoreCase(type)) {
            if (value <= 0 || value > 100) {
                model.addAttribute("errorDiscount", "Giá trị giảm giá phần trăm phải nằm trong khoảng 0 - 100!");
                model.addAttribute("promotions", promotionPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", promotionPage.getTotalPages());
                model.addAttribute("totalElements", promotionPage.getTotalElements());
                model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                model.addAttribute("hasNext", promotionPage.hasNext());
                model.addAttribute("view", "admin/promotionCRUD");
                return "admin/layout";
            }
        } else if ("amount".equalsIgnoreCase(type)) {
            if (value <= 0) {
                model.addAttribute("errorDiscount", "Giá trị giảm giá tiền tệ phải lớn hơn 0!");
                model.addAttribute("promotions", promotionPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", promotionPage.getTotalPages());
                model.addAttribute("totalElements", promotionPage.getTotalElements());
                model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                model.addAttribute("hasNext", promotionPage.hasNext());
                model.addAttribute("view", "admin/promotionCRUD");
                return "admin/layout";
            }
        } else {
            model.addAttribute("error", "Loại giảm giá không hợp lệ! Chỉ chấp nhận 'percent' hoặc 'amount'.");
            model.addAttribute("promotions", promotionPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotionPage.getTotalPages());
            model.addAttribute("totalElements", promotionPage.getTotalElements());
            model.addAttribute("hasPrevious", promotionPage.hasPrevious());
            model.addAttribute("hasNext", promotionPage.hasNext());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }

        try {
            promotion.setCreatedDate(LocalDateTime.now());
            promotion.setUpdatedDate(LocalDateTime.now());
            promotionService.create(promotion);
            redirectAttributes.addFlashAttribute("success", "Thêm khuyến mãi thành công!");
            return "redirect:/Promotion/index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotions", promotionPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotionPage.getTotalPages());
            model.addAttribute("totalElements", promotionPage.getTotalElements());
            model.addAttribute("hasPrevious", promotionPage.hasPrevious());
            model.addAttribute("hasNext", promotionPage.hasNext());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        Promotion promotion = promotionService.findByID(id);
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage;

        if (title != null && !title.trim().isEmpty()) {
            promotionPage = promotionService.searchByName(title.trim(), pageable);
            model.addAttribute("title", title);
        } else if (fromDate != null && toDate != null) {
            promotionPage = promotionService.searchByDate(fromDate, toDate, pageable);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
        } else {
            promotionPage = promotionService.findByAllPromotion(pageable);
        }

        for (Promotion promo : promotionPage.getContent()) {
            if (promo.getEndDate() != null && promo.getEndDate().isBefore(LocalDate.now())) {
                promo.setStatus(false);
                promo.setUpdatedDate(LocalDateTime.now());
                promotionService.update(promo);
            }
        }

        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());
        model.addAttribute("hasPrevious", promotionPage.hasPrevious());
        model.addAttribute("hasNext", promotionPage.hasNext());
        model.addAttribute("promotion", promotion);
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    @PostMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("promotion") Promotion promotion,
            Errors errors,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            RedirectAttributes redirectAttributes) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage;
        if (title != null && !title.trim().isEmpty()) {
            promotionPage = promotionService.searchByName(title.trim(), pageable);
            model.addAttribute("title", title);
        } else if (fromDate != null && toDate != null) {
            promotionPage = promotionService.searchByDate(fromDate, toDate, pageable);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
        } else {
            promotionPage = promotionService.findByAllPromotion(pageable);
        }

        try {
            // 1) Lấy bản ghi hiện tại
            Promotion existing = promotionService.findByID(promotion.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Không tìm thấy khuyến mãi để cập nhật");
            }

            // 2) Xác định có đổi ngày hay không
            boolean startChanged = false;
            boolean endChanged = false;

            // Chỉ coi là thay đổi nếu form gửi giá trị mới khác null
            if (promotion.getStartDate() != null) {
                startChanged = !promotion.getStartDate().isEqual(existing.getStartDate());
            }
            if (promotion.getEndDate() != null) {
                endChanged = !promotion.getEndDate().isEqual(existing.getEndDate());
            }

            boolean dateChanged = startChanged || endChanged;

            // 3) Kiểm tra lỗi validation
            org.springframework.validation.BindingResult br = (org.springframework.validation.BindingResult) errors;
            if (errors.hasErrors()) {
                // Chỉ kiểm tra các lỗi không liên quan đến ngày nếu ngày không thay đổi
                boolean hasNonDateErrors = br.getFieldErrors().stream()
                        .anyMatch(fe -> !("startDate".equals(fe.getField()) || "endDate".equals(fe.getField())))
                        || br.getGlobalErrors().stream()
                                .anyMatch(
                                        ge -> !ge.getCode().contains("startDate") && !ge.getCode().contains("endDate"));

                if (hasNonDateErrors || dateChanged) {
                    // Có lỗi không liên quan đến ngày hoặc ngày bị thay đổi -> trả về lỗi
                    model.addAttribute("promotions", promotionPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", promotionPage.getTotalPages());
                    model.addAttribute("totalElements", promotionPage.getTotalElements());
                    model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                    model.addAttribute("hasNext", promotionPage.hasNext());
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                }
                // Nếu chỉ có lỗi liên quan đến ngày và không thay đổi ngày -> bỏ qua lỗi
            }

            // 4) Xử lý giá trị ngày
            if (dateChanged) {
                // Nếu ngày bị thay đổi, validate quan hệ giữa ngày
                LocalDate finalStartDate = promotion.getStartDate() != null ? promotion.getStartDate()
                        : existing.getStartDate();
                LocalDate finalEndDate = promotion.getEndDate() != null ? promotion.getEndDate()
                        : existing.getEndDate();

                // Thêm kiểm tra: Ngày bắt đầu phải là hôm nay hoặc ngày mai trở đi
                LocalDate today = LocalDate.now();
                LocalDate tomorrow = today.plusDays(1);
                if (finalStartDate != null && (finalStartDate.isBefore(today) || finalStartDate.isAfter(tomorrow))) {
                    model.addAttribute("errorStartDate", "Ngày bắt đầu phải là ngày hôm nay hoặc tương lai!");
                    model.addAttribute("promotions", promotionPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", promotionPage.getTotalPages());
                    model.addAttribute("totalElements", promotionPage.getTotalElements());
                    model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                    model.addAttribute("hasNext", promotionPage.hasNext());
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                }

                if (finalStartDate != null && finalEndDate != null && finalEndDate.isBefore(finalStartDate)) {
                    model.addAttribute("errorEndDate", "Ngày kết thúc phải sau ngày bắt đầu!");
                    model.addAttribute("promotions", promotionPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", promotionPage.getTotalPages());
                    model.addAttribute("totalElements", promotionPage.getTotalElements());
                    model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                    model.addAttribute("hasNext", promotionPage.hasNext());
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                }

                promotion.setStartDate(finalStartDate);
                promotion.setEndDate(finalEndDate);
            } else {
                // Không thay đổi ngày, giữ nguyên giá trị cũ
                promotion.setStartDate(existing.getStartDate());
                promotion.setEndDate(existing.getEndDate());
            }

            // 5) Validate loại và giá trị giảm giá
            String type = promotion.getDiscountType();
            Double value = promotion.getDiscountValue();
            if ("percent".equalsIgnoreCase(type)) {
                if (value == null || value <= 0 || value > 100) {
                    model.addAttribute("errorDiscount", "Giá trị giảm giá phần trăm phải nằm trong khoảng 1 - 100!");
                    model.addAttribute("promotions", promotionPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", promotionPage.getTotalPages());
                    model.addAttribute("totalElements", promotionPage.getTotalElements());
                    model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                    model.addAttribute("hasNext", promotionPage.hasNext());
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                }
            } else if ("amount".equalsIgnoreCase(type)) {
                if (value == null || value <= 0) {
                    model.addAttribute("errorDiscount", "Giá trị giảm giá tiền tệ phải lớn hơn 0!");
                    model.addAttribute("promotions", promotionPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", promotionPage.getTotalPages());
                    model.addAttribute("totalElements", promotionPage.getTotalElements());
                    model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                    model.addAttribute("hasNext", promotionPage.hasNext());
                    model.addAttribute("view", "admin/promotionCRUD");
                    return "admin/layout";
                }
            } else {
                model.addAttribute("error", "Loại giảm giá không hợp lệ! Chỉ chấp nhận 'percent' hoặc 'amount'.");
                model.addAttribute("promotions", promotionPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", promotionPage.getTotalPages());
                model.addAttribute("totalElements", promotionPage.getTotalElements());
                model.addAttribute("hasPrevious", promotionPage.hasPrevious());
                model.addAttribute("hasNext", promotionPage.hasNext());
                model.addAttribute("view", "admin/promotionCRUD");
                return "admin/layout";
            }

            // 6) Giữ createdDate, set updatedDate và cập nhật
            promotion.setCreatedDate(existing.getCreatedDate());
            promotion.setUpdatedDate(LocalDateTime.now());

            promotionService.update(promotion);
            redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công!");

            return "redirect:/Promotion/edit/" + promotion.getId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("promotions", promotionPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotionPage.getTotalPages());
            model.addAttribute("totalElements", promotionPage.getTotalElements());
            model.addAttribute("hasPrevious", promotionPage.hasPrevious());
            model.addAttribute("hasNext", promotionPage.hasNext());
            model.addAttribute("view", "admin/promotionCRUD");
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model,
            @ModelAttribute("promotion") Promotion promotion,
            Errors errors,
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            promotionService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khuyến mãi thành công!");
            return "redirect:/Promotion/index";
        }catch(DataIntegrityViolationException e ){
            redirectAttributes.addFlashAttribute("error", "Không thể xóa khuyến mãi vì có liên kết với các thực thể khác!");
            return "redirect:/Promotion/edit/" + promotion.getId();

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
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage;

        if (fromDate == null || toDate == null) {
            model.addAttribute("error", "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");
            promotionPage = promotionService.findByAllPromotion(pageable);
        } else {
            promotionPage = promotionService.searchByDate(fromDate, toDate, pageable);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
        }

        for (Promotion promo : promotionPage.getContent()) {
            if (promo.getEndDate() != null && promo.getEndDate().isBefore(LocalDate.now())) {
                promo.setStatus(false);
                promo.setUpdatedDate(LocalDateTime.now());
                promotionService.update(promo);
            }
        }

        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());
        model.addAttribute("hasPrevious", promotionPage.hasPrevious());
        model.addAttribute("hasNext", promotionPage.hasNext());
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }

    @GetMapping("/searchByTitle")
    public String searchByTitle(
            @RequestParam("title") String title,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionService.searchByName(title.trim(), pageable);

        model.addAttribute("title", title);
        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalElements", promotionPage.getTotalElements());
        model.addAttribute("hasPrevious", promotionPage.hasPrevious());
        model.addAttribute("hasNext", promotionPage.hasNext());
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("view", "admin/promotionCRUD");
        return "admin/layout";
    }
}