package com.datn.Controller.admin;

import com.datn.Service.ServiceRequestService;
import com.datn.model.ServiceRequest;
import com.datn.model.enums.ServiceRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/service-requests")
public class List_Order_Service {

    @Autowired
    private ServiceRequestService requestService;

    // Hiển thị cả 2 danh sách: yêu cầu và đơn hàng
    @GetMapping
    public String listRequests(
            Model model,
            @RequestParam(value = "status", required = false) ServiceRequestStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ServiceRequest> allRequests = requestService.findAll();

        // Bộ lọc
        List<ServiceRequest> filteredRequests = allRequests.stream()
                .filter(req -> (status == null || req.getStatus() == status))
                .filter(req -> (keyword == null || keyword.isBlank() ||
                        req.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                        req.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                        req.getPhone().contains(keyword)))
                .filter(req -> (from == null || !req.getCreatedAt().toLocalDate().isBefore(from)))
                .filter(req -> (to == null || !req.getCreatedAt().toLocalDate().isAfter(to)))
                .collect(Collectors.toList());

        // Yêu cầu chưa chốt đơn (PENDING, CONTACTED)
        List<ServiceRequest> requestList = filteredRequests.stream()
                .filter(r -> r.getStatus() != ServiceRequestStatus.CONFIRMED)
                .collect(Collectors.toList());

        // Đơn hàng đã chốt
        List<ServiceRequest> confirmedOrders = filteredRequests.stream()
                .filter(r -> r.getStatus() == ServiceRequestStatus.CONFIRMED)
                .collect(Collectors.toList());

        model.addAttribute("requests", requestList);
        model.addAttribute("orders", confirmedOrders);
        model.addAttribute("statuses", ServiceRequestStatus.values());

        model.addAttribute("view", "admin/list-order-service");
        return "admin/layout";
    }

    // Báo giá (liên hệ)
    @PostMapping("/{id}/contact")
    public String contactCustomer(
            @PathVariable("id") Long id,
            @RequestParam("quotedPrice") BigDecimal quotedPrice,
            RedirectAttributes ra) {

        ServiceRequest request = requestService.findById(id);
        if (request == null || request.getStatus() != ServiceRequestStatus.PENDING) {
            ra.addFlashAttribute("error", "Không thể báo giá cho yêu cầu này.");
            return "redirect:/admin/service-requests";
        }

        request.setQuotedPrice(quotedPrice);
        request.setStatus(ServiceRequestStatus.CONTACTED);
        requestService.save(request);

        ra.addFlashAttribute("success", "Đã báo giá thành công!");
        return "redirect:/admin/service-requests";
    }

    // Hủy yêu cầu
    @PostMapping("/{id}/cancel")
    public String cancelRequest(@PathVariable("id") Long id, RedirectAttributes ra) {
        ServiceRequest request = requestService.findById(id);
        if (request == null || request.getStatus() == ServiceRequestStatus.CONFIRMED) {
            ra.addFlashAttribute("error", "Không thể huỷ yêu cầu này.");
            return "redirect:/admin/service-requests";
        }

        request.setStatus(ServiceRequestStatus.CANCELLED);
        requestService.save(request);

        ra.addFlashAttribute("success", "Đã huỷ yêu cầu.");
        return "redirect:/admin/service-requests";
    }

    // Xác nhận đơn
    @PostMapping("/{id}/confirm")
    public String confirmRequest(@PathVariable("id") Long id, RedirectAttributes ra) {
        ServiceRequest request = requestService.findById(id);
        if (request == null || request.getStatus() != ServiceRequestStatus.CONTACTED) {
            ra.addFlashAttribute("error", "Không thể xác nhận yêu cầu này.");
            return "redirect:/admin/service-requests";
        }

        request.setStatus(ServiceRequestStatus.CONFIRMED);
        requestService.save(request);

        ra.addFlashAttribute("success", "Đã xác nhận yêu cầu thành công.");
        return "redirect:/admin/service-requests";
    }
}
