package com.datn.Controller.admin;

import com.datn.Service.ServiceOrderService;
import com.datn.Service.ServiceRequestService;
import com.datn.dao.ServiceRequestDraftDAO;
import com.datn.model.ServiceOrder;
import com.datn.model.ServiceRequest;
import com.datn.model.ServiceRequestDraft;
import com.datn.model.enums.ServiceOrderStatus;
import com.datn.model.enums.ServiceRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/service-requests")
public class List_Order_Service {

    @Autowired
    private ServiceRequestService requestService;

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private ServiceRequestDraftDAO draftDAO;

    // Hiển thị cả 2 danh sách: yêu cầu và đơn hàng với phân trang
    @GetMapping
    public String listRequests(
            Model model,
            @RequestParam(value = "status", required = false) ServiceRequestStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "requestMonth", required = false) String requestMonth,
            @RequestParam(value = "orderStatus", required = false) ServiceOrderStatus orderStatus,
            @RequestParam(value = "orderKeyword", required = false) String orderKeyword,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "requestPage", defaultValue = "0") int requestPage,
            @RequestParam(value = "orderPage", defaultValue = "0") int orderPage) {

        // Tạo Pageable cho requests (10 items per page, sắp xếp theo ngày tạo mới nhất)
        Pageable requestPageable = PageRequest.of(requestPage, 10,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));

        // Tạo Pageable cho orders (10 items per page, sắp xếp theo ngày xác nhận mới
        // nhất)
        Pageable orderPageable = PageRequest.of(orderPage, 10,
                Sort.by(Sort.Direction.DESC, "confirmedAt").and(Sort.by(Sort.Direction.DESC, "id")));

        // Lấy danh sách yêu cầu với phân trang và lọc (bao gồm tất cả trạng thái)
        Page<ServiceRequest> requestsPage = requestService.findByFilters(status, keyword, requestMonth,
                requestPageable);
        List<ServiceRequest> requestList = requestsPage.getContent();

        // Lấy danh sách đơn hàng với phân trang và lọc
        Page<ServiceOrder> orderListPage = serviceOrderService.findByFilters(orderStatus, orderKeyword, month,
                orderPageable);

        // Đẩy dữ liệu sang view
        model.addAttribute("requests", requestList); // Yêu cầu chưa xác nhận
        model.addAttribute("orderList", orderListPage.getContent()); // Đơn hàng đã lọc

        // Thông tin phân trang cho requests (sử dụng dữ liệu từ Page object)
        model.addAttribute("requestCurrentPage", requestsPage.getNumber());
        model.addAttribute("requestTotalPages", requestsPage.getTotalPages());
        model.addAttribute("requestTotalElements", requestsPage.getTotalElements());
        model.addAttribute("requestHasNext", requestsPage.hasNext());
        model.addAttribute("requestHasPrevious", requestsPage.hasPrevious());

        // Thông tin phân trang cho orders
        model.addAttribute("orderCurrentPage", orderPage);
        model.addAttribute("orderTotalPages", orderListPage.getTotalPages());
        model.addAttribute("orderTotalElements", orderListPage.getTotalElements());
        model.addAttribute("orderHasNext", orderListPage.hasNext());
        model.addAttribute("orderHasPrevious", orderListPage.hasPrevious());

        // Dropdown và filter data
        model.addAttribute("statuses", ServiceRequestStatus.values());
        model.addAttribute("orderStatuses", ServiceOrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRequestMonth", requestMonth);
        model.addAttribute("selectedOrderStatus", orderStatus);
        model.addAttribute("orderKeyword", orderKeyword);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("view", "admin/list-order-service");

        return "admin/layout";
    }

    // Liên hệ - Lưu thông tin tạm thời
    @PostMapping("/{id}/contact")
    @ResponseBody
    public ResponseEntity<Map<String, String>> contactCustomer(
            @PathVariable("id") Long id,
            @RequestParam("quotedPrice") BigDecimal quotedPrice,
            @RequestParam("district") String district,
            @RequestParam("addressDetail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("executionTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate) {

        try {
            ServiceRequest request = requestService.findById(id);
            if (request == null || (request.getStatus() != ServiceRequestStatus.PENDING
                    && request.getStatus() != ServiceRequestStatus.CONTACTED)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể liên hệ yêu cầu này.");
                return ResponseEntity.badRequest().body(error);
            }

            // Lưu trạng thái cũ để tạo message phù hợp
            ServiceRequestStatus oldStatus = request.getStatus();

            // Cập nhật trạng thái yêu cầu
            request.setStatus(ServiceRequestStatus.CONTACTED);
            requestService.save(request);

            // Tạo/cập nhật bản nháp để lưu thông tin tạm thời
            ServiceRequestDraft draft = draftDAO.findByRequestId(id).orElse(new ServiceRequestDraft());
            draft.setRequest(request);
            draft.setQuotedPrice(quotedPrice);
            draft.setDistrict(district);
            draft.setAddressDetail(addressDetail);
            draft.setDescription(description);
            draft.setExecutionTime(executionDate);
            draft.setUpdatedAt(LocalDate.now());

            draftDAO.save(draft);

            String message = oldStatus == ServiceRequestStatus.PENDING ? "Đã liên hệ và lưu thông tin tạm thời."
                    : "Đã cập nhật thông tin liên hệ.";

            Map<String, String> response = new HashMap<>();
            response.put("success", message);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Cập nhật thông tin liên hệ
    @PostMapping("/{id}/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateContactInfo(
            @PathVariable("id") Long id,
            @RequestParam("quotedPrice") BigDecimal quotedPrice,
            @RequestParam("district") String district,
            @RequestParam("addressDetail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("executionTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate) {

        try {
            ServiceRequest request = requestService.findById(id);
            if (request == null || request.getStatus() != ServiceRequestStatus.CONTACTED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ có thể cập nhật thông tin cho yêu cầu đã liên hệ.");
                return ResponseEntity.badRequest().body(error);
            }

            // Cập nhật thông tin trong bản nháp
            ServiceRequestDraft draft = draftDAO.findByRequestId(id).orElse(null);
            if (draft == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không tìm thấy thông tin liên hệ để cập nhật.");
                return ResponseEntity.badRequest().body(error);
            }

            // Cập nhật thông tin mới
            draft.setQuotedPrice(quotedPrice);
            draft.setDistrict(district);
            draft.setAddressDetail(addressDetail);
            draft.setDescription(description);
            draft.setExecutionTime(executionDate);
            draft.setUpdatedAt(LocalDate.now());

            draftDAO.save(draft);

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã cập nhật thông tin liên hệ thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Hủy yêu cầu
    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelRequest(@PathVariable("id") Long id) {
        try {
            ServiceRequest request = requestService.findById(id);
            if (request == null || request.getStatus() == ServiceRequestStatus.CONFIRMED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể huỷ yêu cầu này.");
                return ResponseEntity.badRequest().body(error);
            }

            request.setStatus(ServiceRequestStatus.CANCELLED);
            requestService.save(request);

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã huỷ yêu cầu thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Khôi phục yêu cầu đã hủy
    @PostMapping("/{id}/restore")
    @ResponseBody
    public ResponseEntity<Map<String, String>> restoreRequest(@PathVariable("id") Long id) {
        try {
            ServiceRequest request = requestService.findById(id);
            if (request == null || request.getStatus() != ServiceRequestStatus.CANCELLED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ có thể khôi phục yêu cầu đã hủy.");
                return ResponseEntity.badRequest().body(error);
            }

            // Kiểm tra xem có thông tin draft không để quyết định trạng thái khôi phục
            ServiceRequestDraft draft = draftDAO.findByRequestId(id).orElse(null);

            if (draft != null) {
                // Nếu có thông tin đã liên hệ trước đó, khôi phục về CONTACTED
                request.setStatus(ServiceRequestStatus.CONTACTED);
            } else {
                // Nếu chưa có thông tin, khôi phục về PENDING
                request.setStatus(ServiceRequestStatus.PENDING);
            }

            requestService.save(request);

            Map<String, String> response = new HashMap<>();
            String statusText = draft != null ? "đã liên hệ" : "đang chờ";
            response.put("success", "Đã khôi phục yêu cầu về trạng thái " + statusText + " thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Xác nhận đơn
    @PostMapping("/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Map<String, String>> confirmRequest(
            @PathVariable("id") Long id,
            @RequestParam("quotedPrice") BigDecimal quotedPrice,
            @RequestParam("district") String district,
            @RequestParam("addressDetail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("executionTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate) {

        try {
            ServiceRequest request = requestService.findById(id);
            if (request == null || (request.getStatus() != ServiceRequestStatus.CONTACTED
                    && request.getStatus() != ServiceRequestStatus.PENDING)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể xác nhận yêu cầu này.");
                return ResponseEntity.badRequest().body(error);
            }

            // Cập nhật trạng thái yêu cầu
            request.setStatus(ServiceRequestStatus.CONFIRMED);
            requestService.save(request);

            // Tạo đơn hàng chính thức
            ServiceOrder order = new ServiceOrder();
            order.setRequest(request);
            order.setUser(request.getUser()); // Copy user từ ServiceRequest
            order.setQuotedPrice(quotedPrice);
            order.setDistrict(district);
            order.setAddressDetail(addressDetail);
            order.setDescription(description);
            order.setExecutionTime(executionDate);
            order.setConfirmedAt(LocalDate.now());
            order.setStatus(ServiceOrderStatus.UNPAID);

            serviceOrderService.save(order);

            // Xóa bản nháp sau khi tạo đơn hàng thành công
            draftDAO.findByRequestId(id).ifPresent(draft -> {
                draftDAO.delete(draft);
            });

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã tạo đơn hàng thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
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

    // Endpoint riêng cho đơn hàng dịch vụ
    @GetMapping("/order/{id}/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        ServiceOrder order = serviceOrderService.findById(id).orElse(null);

        if (order == null)
            return ResponseEntity.notFound().build();

        ServiceRequest req = order.getRequest();
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("orderId", order.getId());
        data.put("requestId", req.getId());
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

    // Endpoint để load dữ liệu bản nháp cho modal
    @GetMapping("/{id}/draft")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDraftData(@PathVariable Long id) {
        ServiceRequest req = requestService.findById(id);
        if (req == null) {
            return ResponseEntity.notFound().build();
        }

        ServiceRequestDraft draft = draftDAO.findByRequestId(id).orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", req.getId());
        data.put("serviceName", req.getService().getName());
        data.put("status", req.getStatus().name());

        if (draft != null) {
            data.put("quotedPrice", draft.getQuotedPrice());
            data.put("district", draft.getDistrict());
            data.put("addressDetail", draft.getAddressDetail());
            data.put("description", draft.getDescription());
            data.put("executionTime", draft.getExecutionTime());
        }

        return ResponseEntity.ok(data);
    }

    @PostMapping("/order/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long id) {
        try {
            ServiceOrder order = serviceOrderService.findById(id).orElse(null);
            if (order == null || order.getStatus() == ServiceOrderStatus.CANCELLED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể huỷ đơn hàng này.");
                return ResponseEntity.badRequest().body(error);
            }

            order.setStatus(ServiceOrderStatus.CANCELLED);
            serviceOrderService.save(order);

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã huỷ đơn hàng thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/order/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, String>> completeOrder(@PathVariable Long id) {
        try {
            ServiceOrder order = serviceOrderService.findById(id).orElse(null);
            if (order == null || order.getStatus() == ServiceOrderStatus.DONE
                    || order.getStatus() == ServiceOrderStatus.CANCELLED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể hoàn thành đơn hàng này.");
                return ResponseEntity.badRequest().body(error);
            }

            order.setStatus(ServiceOrderStatus.DONE);
            serviceOrderService.save(order);

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã hoàn thành đơn hàng thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/order/{id}/mark-paid")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markOrderAsPaid(@PathVariable Long id) {
        try {
            ServiceOrder order = serviceOrderService.findById(id).orElse(null);
            if (order == null || order.getStatus() != ServiceOrderStatus.UNPAID) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ có thể đánh dấu đã thanh toán cho đơn hàng chưa thanh toán.");
                return ResponseEntity.badRequest().body(error);
            }

            order.setStatus(ServiceOrderStatus.PAID);
            serviceOrderService.save(order);

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã đánh dấu đơn hàng đã thanh toán thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/order/{id}/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateOrder(
            @PathVariable Long id,
            @RequestParam("quotedPrice") BigDecimal quotedPrice,
            @RequestParam("district") String district,
            @RequestParam("addressDetail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("executionTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate) {

        try {
            ServiceOrder order = serviceOrderService.findById(id).orElse(null);
            if (order == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không tìm thấy đơn hàng.");
                return ResponseEntity.badRequest().body(error);
            }

            // Chỉ cho phép cập nhật đơn hàng chưa hoàn thành hoặc chưa hủy
            if (order.getStatus() == ServiceOrderStatus.DONE || order.getStatus() == ServiceOrderStatus.CANCELLED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể cập nhật đơn hàng đã hoàn thành hoặc đã hủy.");
                return ResponseEntity.badRequest().body(error);
            }

            // Cập nhật thông tin đơn hàng
            order.setQuotedPrice(quotedPrice);
            order.setDistrict(district);
            order.setAddressDetail(addressDetail);
            order.setDescription(description);
            order.setExecutionTime(executionDate);

            serviceOrderService.save(order);

            Map<String, String> response = new HashMap<>();
            response.put("success", "Đã cập nhật đơn hàng thành công.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // API endpoint để refresh dữ liệu tab đơn hàng (AJAX)
    @GetMapping("/orders/refresh")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> refreshOrdersData(
            @RequestParam(value = "orderStatus", required = false) ServiceOrderStatus orderStatus,
            @RequestParam(value = "orderKeyword", required = false) String orderKeyword,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "orderPage", defaultValue = "0") int orderPage) {

        try {
            // Tạo Pageable cho orders (10 items per page, sắp xếp theo ngày xác nhận mới nhất)
            Pageable orderPageable = PageRequest.of(orderPage, 10,
                    Sort.by(Sort.Direction.DESC, "confirmedAt").and(Sort.by(Sort.Direction.DESC, "id")));

            // Lấy danh sách đơn hàng với phân trang và lọc
            Page<ServiceOrder> orderListPage = serviceOrderService.findByFilters(orderStatus, orderKeyword, month,
                    orderPageable);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orderListPage.getContent());
            response.put("currentPage", orderListPage.getNumber());
            response.put("totalPages", orderListPage.getTotalPages());
            response.put("totalElements", orderListPage.getTotalElements());
            response.put("hasNext", orderListPage.hasNext());
            response.put("hasPrevious", orderListPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Có lỗi xảy ra khi tải dữ liệu: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

}
