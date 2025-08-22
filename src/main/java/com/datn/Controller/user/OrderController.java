package com.datn.Controller.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.datn.Service.CartItemService;
import com.datn.Service.OrderService;
import com.datn.Service.PayOSService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.Service.PromotionService;
import com.datn.Service.WardService;
import com.datn.dao.ZoneDAO;
import com.datn.model.CartItem;
import com.datn.model.Order;
import com.datn.model.OrderDetail;
import com.datn.model.OrderRequest;
import com.datn.model.Promotion;
import com.datn.model.User;
import com.datn.model.Ward;
import com.datn.model.Zone;
import com.datn.utils.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/order")

public class OrderController {

    @Autowired
    ProductCategoryService pro_ca_service;
    @Autowired
    ProductService productService;
    @Autowired
    AuthService authService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private PromotionService promotionService;
    @Autowired
    private WardService wardService;
    @Autowired
    private ZoneDAO zoneDAO;
    @Autowired
    private PayOSService payOSService;

    @GetMapping("/index")
    public String index(Model model) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId(); // Sửa lại nếu getter id khác
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        OrderRequest orderRequest = new OrderRequest();
        model.addAttribute("orderRequest", orderRequest);
        return showForm(model);
    }

    @PostMapping("/apply-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(@RequestParam("voucher") String code, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = authService.getUser();
        double totalAmount = cartItemService.getTotalAmount(user.getId());
        Promotion promotion = promotionService.findPromotionByName(code.trim());

        if (promotion == null || promotion.getStatus() == false) {
            response.put("error", "Mã giảm giá không hợp lệ hoặc đã bị vô hiệu hóa.");
            return ResponseEntity.ok(response);
        }

        LocalDate today = LocalDate.now();
        if (promotion.getStartDate().isAfter(today) || promotion.getEndDate().isBefore(today)) {
            response.put("error", "Mã giảm giá đã hết hạn.");
            return ResponseEntity.ok(response);
        }
        if (promotion.getUseCount() <= 0) {
            response.put("error", "Mã giảm giá đã hết lượt sử dụng.");
            return ResponseEntity.ok(response);

        }

        double discountValue;
        double finalAmount;

        if ("percent".equalsIgnoreCase(promotion.getDiscountType())) {
            discountValue = totalAmount * promotion.getDiscountValue() / 100;
        } else {
            discountValue = promotion.getDiscountValue(); // fixed value
        }

        finalAmount = Math.max(totalAmount - discountValue, 0); // tránh âm

        response.put("success", "Áp dụng mã thành công! Giảm " + (int) discountValue + " VNĐ");
        session.setAttribute("finalAmount", finalAmount);
        session.setAttribute("promotionId", promotion.getId());

        response.put("discountValue", discountValue);
        response.put("formattedOriginalTotal", String.format("%,.0f", totalAmount));
        System.out.println("Total Amount: " + totalAmount);
        response.put("formattedDiscount", String.format("%,.0f", discountValue));
        System.out.println("Discount Value: " + discountValue);
        response.put("formattedTotal", String.format("%,.0f", finalAmount));
        System.out.println("Final Amount: " + finalAmount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("orderRequest") OrderRequest orderRequest, BindingResult result,
            Model model, HttpSession session, @RequestParam(value = "wardID", required = false) Long wardId,
            @RequestParam(value = "specific", required = false) String specific,
            @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod) {
        
        // Debug logging
        System.out.println("Checkout called with wardID: " + wardId + ", paymentMethod: " + paymentMethod);
        
        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            model.addAttribute("selectedWardId", wardId);
            model.addAttribute("specific", specific);
            return showForm(model);
        }

        // Kiểm tra wardID có tồn tại không (cần thiết cho việc đặt hàng mới)
        if (wardId == null) {
            System.out.println("WardID is null - returning error");
            model.addAttribute("error", "Vui lòng chọn xã/phường.");
            return showForm(model);
        }

        User user = authService.getUser();
        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(user.getId());
        if (cartItems.isEmpty()) {
            model.addAttribute("message", "Giỏ hàng trống, không thể đặt hàng.");
            return showForm(model);
        }

        // Lấy thông tin phí vận chuyển từ ward
        Ward selectedWard = wardService.findById(wardId);
        Double shippingFee = 0.0;
        if (selectedWard != null && selectedWard.getZone() != null) {
            shippingFee = selectedWard.getZone().getShipFee();
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreateDate(new Date());
        order.setSdt(orderRequest.getSdt());
        order.setAddress(orderRequest.getAddress());
        order.setStatus("Chưa xác nhận");
        order.setPaymentMethod(paymentMethod);
        order.setShipFee(shippingFee);

        // Kiểm tra xem có mã giảm không
        Double finalAmount = (Double) session.getAttribute("finalAmount");
        if (finalAmount == null) {
            // Nếu không có, lấy giá gốc
            finalAmount = cartItemService.getTotalAmount(user.getId());
        }
        
        // Cộng phí vận chuyển vào tổng tiền
        finalAmount += shippingFee;
        order.setTotalAmount(finalAmount);

        // Tạo order code unique
        String orderCode = String.valueOf(System.currentTimeMillis());
        order.setOrderCode(orderCode);

        // Set payment status based on payment method
        if ("PAYOS".equals(paymentMethod)) {
            order.setPaymentStatus("PENDING");
        } else {
            order.setPaymentStatus("COD");
        }

        List<OrderDetail> orderDetails = new ArrayList<>();

        Order savedOrder = orderService.saveOrder(order, orderDetails);

        for (CartItem cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(cartItem.getProduct());
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(cartItem.getProduct().getPrice());
            orderDetails.add(detail);
        }

        orderService.saveOrder(savedOrder, orderDetails);

        // Xử lý promotion nếu có
        Long promotionId = (Long) session.getAttribute("promotionId");
        if (promotionId != null) {
            Promotion appliedPromotion = promotionService.findByID(promotionId);
            if (appliedPromotion != null) {
                savedOrder.setPromotion(appliedPromotion);

                int currentCount = appliedPromotion.getUseCount();
                if (currentCount > 0) {
                    promotionService.updateUseCount(promotionId, currentCount - 1);
                }
            }
        }

        // Xử lý thanh toán
        if ("PAYOS".equals(paymentMethod)) {
            try {
                // Tạo link thanh toán PayOS
                String paymentUrl = payOSService.createPaymentLink(savedOrder);
                savedOrder.setPaymentUrl(paymentUrl);
                orderService.saveOrder(savedOrder, orderDetails);

                // Clear cart và session
                cartItemService.clearCartByUserId(user.getId());
                session.removeAttribute("finalAmount");
                session.removeAttribute("totalAmount");
                session.removeAttribute("appliedPromotion");
                session.removeAttribute("promotionId");

                // Redirect đến trang thanh toán PayOS
                return "redirect:" + paymentUrl;

            } catch (Exception e) {
                model.addAttribute("error", "Lỗi tạo link thanh toán: " + e.getMessage());
                return showForm(model);
            }
        } else {
            // Thanh toán COD
            cartItemService.clearCartByUserId(user.getId());
            model.addAttribute("success", "Đặt hàng thành công! Bạn sẽ thanh toán khi nhận hàng.");
        }

        session.removeAttribute("finalAmount");
        session.removeAttribute("totalAmount");
        session.removeAttribute("appliedPromotion");
        session.removeAttribute("promotionId");

        return showForm(model);
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam(value = "orderCode", required = false) String orderCode,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            if (orderCode != null) {
                // Tìm đơn hàng theo orderCode
                List<Order> orders = orderService.getAllOrders();
                Order order = orders.stream()
                        .filter(o -> orderCode.equals(o.getOrderCode()))
                        .findFirst()
                        .orElse(null);

                if (order != null) {
                    // Cập nhật trạng thái thành công (vì PayOS chỉ redirect về success khi thanh
                    // toán thành công)
                    order.setPaymentStatus("PAID");
                    order.setStatus("Đã thanh toán");
                    order.setTransactionId(orderCode);
                    orderService.saveOrder(order, order.getOrderDetails());

                    redirectAttributes.addFlashAttribute("success",
                            "Thanh toán thành công! Đơn hàng của bạn đã được xác nhận.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đơn hàng.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xác nhận thanh toán: " + e.getMessage());
        }

        return "redirect:/order/index";
    }

    @GetMapping("/payment-cancel")
    public String paymentCancel(@RequestParam(value = "orderCode", required = false) String orderCode,
            RedirectAttributes redirectAttributes) {
        try {
            if (orderCode != null) {
                // Tìm đơn hàng theo orderCode
                List<Order> orders = orderService.getAllOrders();
                Order order = orders.stream()
                        .filter(o -> orderCode.equals(o.getOrderCode()))
                        .findFirst()
                        .orElse(null);

                if (order != null) {
                    order.setPaymentStatus("CANCELLED");
                    order.setStatus("Đã hủy");
                    orderService.saveOrder(order, order.getOrderDetails());
                }
            }
        } catch (Exception e) {
            // Log error
            System.err.println("Lỗi hủy thanh toán: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("error",
                "Thanh toán đã bị hủy. Bạn có thể thử lại hoặc chọn thanh toán khi nhận hàng.");
        return "redirect:/order/index";
    }

    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<String> handleWebhook(@RequestParam String webhookBody,
            @RequestParam String signature) {
        try {
            if (payOSService.verifyWebhook(webhookBody, signature)) {
                // Xử lý webhook từ PayOS
                // payOSService.handlePaymentResult(paymentData);
                return ResponseEntity.ok("OK");
            } else {
                return ResponseEntity.badRequest().body("Invalid signature");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }

    public String showForm(Model model) {
        User user = authService.getUser();
        model.addAttribute("user", user);
        List<Ward> wards = wardService.getAllWards();
        model.addAttribute("wards", wards);
        List<Zone> zones = zoneDAO.findAll();
        model.addAttribute("zones", zones);
        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(user.getId());
        model.addAttribute("cartItems", cartItems);

        model.addAttribute("totalAmount", cartItemService.getTotalAmount(user.getId()));

        model.addAttribute("productCategories", pro_ca_service.findAll());
        model.addAttribute("view", "order");
        return "layouts/layout";
    }

}