package com.datn.Controller.admin;

import com.datn.Service.ServiceOrderService;
import com.datn.Service.ServiceRequestService;
import com.datn.model.ServiceOrder;
import com.datn.model.ServiceRequest;
import com.datn.model.enums.ServiceOrderStatus;
import com.datn.model.enums.ServiceRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/service-requests")
public class List_Order_Service {

    @Autowired
    private ServiceRequestService requestService;

    @Autowired
    private ServiceOrderService serviceOrderService;

    // Hiển thị cả 2 danh sách: yêu cầu và đơn hàng
    @GetMapping
    public String listRequests(
            Model model,
            @RequestParam(value = "status", required = false) ServiceRequestStatus status,
            @RequestParam(value = "keyword", required = false) String keyword) {

        // Lấy tất cả yêu cầu dịch vụ
        List<ServiceRequest> allRequests = requestService.findAll();

        // Lọc theo trạng thái và từ khóa (tên, email, số điện thoại)
        List<ServiceRequest> filteredRequests = allRequests.stream()
                .filter(req -> status == null || req.getStatus() == status)
                .filter(req -> {
                    if (keyword == null || keyword.isBlank())
                        return true;
                    String kw = keyword.toLowerCase();
                    return req.getFullName().toLowerCase().contains(kw)
                            || req.getEmail().toLowerCase().contains(kw)
                            || req.getPhone().contains(kw);
                })
                .collect(Collectors.toList());

        // Danh sách yêu cầu chưa chốt đơn (PENDING, CONTACTED)
        List<ServiceRequest> requestList = filteredRequests.stream()
                .filter(req -> req.getStatus() != ServiceRequestStatus.CONFIRMED)
                .collect(Collectors.toList());

        // Danh sách yêu cầu đã chốt đơn (CONFIRMED)
        List<ServiceRequest> confirmedOrders = filteredRequests.stream()
                .filter(req -> req.getStatus() == ServiceRequestStatus.CONFIRMED)
                .collect(Collectors.toList());

        // Lấy danh sách đơn hàng (nếu có)
        List<ServiceOrder> orderList = serviceOrderService.findAll();

        // Đẩy dữ liệu sang view
        model.addAttribute("requests", requestList); // Yêu cầu chưa xác nhận
        model.addAttribute("orders", confirmedOrders); // Yêu cầu đã xác nhận
        model.addAttribute("orderList", orderList); // Đơn hàng thật
        model.addAttribute("statuses", ServiceRequestStatus.values()); // Dùng cho dropdown filter
        model.addAttribute("selectedStatus", status); // Trạng thái đang lọc
        model.addAttribute("keyword", keyword); // Từ khóa đang lọc
        model.addAttribute("view", "admin/list-order-service");

        return "admin/layout";
    }

    // Liên hệ
    @PostMapping("/{id}/contact")
    public String contactCustomer(
            @PathVariable("id") Long id,
            RedirectAttributes ra) {

        ServiceRequest request = requestService.findById(id);
        if (request == null || request.getStatus() != ServiceRequestStatus.PENDING) {
            ra.addFlashAttribute("error", "Không thể liên hệ yêu cầu này.");
            return "redirect:/admin/service-requests";
        }

        request.setStatus(ServiceRequestStatus.CONTACTED);
        requestService.save(request);

        ra.addFlashAttribute("success", "Đã chuyển trạng thái sang 'Đã liên hệ'.");
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
    public String confirmRequest(
            @PathVariable("id") Long id,
            @RequestParam("quotedPrice") BigDecimal quotedPrice,
            @RequestParam("district") String district,
            @RequestParam("addressDetail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("executionTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate,
            RedirectAttributes ra) {

        ServiceRequest request = requestService.findById(id);
        if (request == null || request.getStatus() != ServiceRequestStatus.CONTACTED) {
            ra.addFlashAttribute("error", "Không thể xác nhận yêu cầu này.");
            return "redirect:/admin/service-requests";
        }

        // Cập nhật trạng thái yêu cầu
        request.setStatus(ServiceRequestStatus.CONFIRMED);
        requestService.save(request);

        // Tạo đơn hàng
        ServiceOrder order = new ServiceOrder();
        order.setRequest(request);
        order.setQuotedPrice(quotedPrice);
        order.setDistrict(district);
        order.setAddressDetail(addressDetail);
        order.setDescription(description);
        order.setExecutionTime(executionDate);
        order.setConfirmedAt(LocalDate.now());
        order.setStatus(ServiceOrderStatus.UNPAID);

        serviceOrderService.save(order);

        ra.addFlashAttribute("success", "Đã tạo đơn hàng và xác nhận yêu cầu thành công.");
        return "redirect:/admin/service-requests";
    }

    @GetMapping("/service-request/{id}/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRequestDetail(@PathVariable Long id) {
        ServiceRequest req = requestService.findById(id);
        ServiceOrder order = serviceOrderService.findByRequestId(id).orElse(null);

        if (req == null || order == null)
            return ResponseEntity.notFound().build();

        Map<String, Object> data = new HashMap<>();
        data.put("id", req.getId());
        data.put("fullName", req.getFullName());
        data.put("phone", req.getPhone());
        data.put("email", req.getEmail());
        data.put("serviceName", req.getService().getName());
        data.put("quotedPrice", order.getQuotedPrice());
        data.put("requestDate", req.getCreatedAt());
        data.put("performDate", order.getExecutionTime());
        data.put("province", order.getDistrict());
        data.put("address", order.getAddressDetail());
        data.put("note", order.getDescription());
        data.put("status", order.getStatus().getDisplayName());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/service-request/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes ra) {
        ServiceOrder order = serviceOrderService.findById(id).orElse(null);
        if (order == null || order.getStatus() == ServiceOrderStatus.CANCELLED) {
            ra.addFlashAttribute("error", "Không thể huỷ đơn hàng này.");
            return "redirect:/admin/service-requests";
        }

        order.setStatus(ServiceOrderStatus.CANCELLED);
        serviceOrderService.save(order);
        ra.addFlashAttribute("success", "Đã huỷ đơn hàng.");
        return "redirect:/admin/service-requests";
    }

}
