package com.datn.Controller.user;

// Các import cần thiết cho controller
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
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.Service.PromotionService;
import com.datn.model.CartItem;
import com.datn.model.Order;
import com.datn.model.OrderDetail;
import com.datn.model.OrderRequest;
import com.datn.model.Promotion;
import com.datn.model.User;
import com.datn.utils.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/order")
public class OrderController {

    // Inject các service cần thiết
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

    // Hiển thị trang đặt hàng (form)
    @GetMapping("/index")
    public String index(Model model ) {
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);

        OrderRequest orderRequest = new OrderRequest();
        model.addAttribute("orderRequest", orderRequest);
        return showForm(model);
    }

    // Áp dụng mã giảm giá
    @PostMapping("/apply-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(@RequestParam("voucher") String code, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = authService.getUser();
        double totalAmount = cartItemService.getTotalAmount(user.getId());
        Promotion promotion = promotionService.findPromotionByName(code.trim());

        // Kiểm tra tính hợp lệ của mã giảm giá
        if (promotion == null || promotion.getStatus() == false) {
            response.put("error", "Mã giảm giá không hợp lệ hoặc đã bị vô hiệu hóa.");
            return ResponseEntity.ok(response);
        }

        // Kiểm tra xem thử ngày hiện tại có nằm trong khoảng còn hạn sử dụng không
        LocalDate today = LocalDate.now();
        if (promotion.getStartDate().isAfter(today) || promotion.getEndDate().isBefore(today)) {
            response.put("error", "Mã giảm giá đã hết hạn.");
            return ResponseEntity.ok(response);
        }

        // Kiểm tra số lượng sử dụng, nếu <= 0 thì thông báo lỗi
        if (promotion.getUseCount() <= 0) {
            response.put("error", "Mã giảm giá đã hết lượt sử dụng.");
            return ResponseEntity.ok(response);
        }

        // Tính toán giảm giá
        double discountValue;
        double finalAmount;

        // Kiểm tra nếu là percent(phần trăm) thì lấy tổng đơn hàng * giá trị giảm giá và / 100
        if ("percent".equalsIgnoreCase(promotion.getDiscountType())) {
            discountValue = totalAmount * promotion.getDiscountValue() / 100;
        } else {
            discountValue = promotion.getDiscountValue(); // cố định
        }

        finalAmount = Math.max(totalAmount - discountValue, 0); // tránh tổng tiền âm

        // Gửi dữ liệu phản hồi về client
        response.put("success", "Áp dụng mã thành công! Giảm " + (int) discountValue + " VNĐ");
        session.setAttribute("finalAmount", finalAmount);
        session.setAttribute("promotionId", promotion.getId());

        response.put("discountValue", discountValue);
        response.put("formattedOriginalTotal", String.format("%,.0f", totalAmount));
        response.put("formattedDiscount", String.format("%,.0f", discountValue));
        response.put("formattedTotal", String.format("%,.0f", finalAmount));

        return ResponseEntity.ok(response);
    }

    // Xử lý đặt hàng khi người dùng submit form
    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("orderRequest") OrderRequest orderRequest, BindingResult result, Model model, HttpSession session) {
        // Nếu có lỗi validate thì hiển thị lại form
        if (result.hasErrors()) {
            return showForm(model);
        }

        User user = authService.getUser();
        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(user.getId());
        if (cartItems.isEmpty()) {
            model.addAttribute("message", "Giỏ hàng trống, không thể đặt hàng.");
            return showForm(model);
        }

    Order order = new Order();
    order.setUser(user);
    order.setCreateDate(new Date());
    order.setSdt(orderRequest.getSdt());
    order.setAddress(orderRequest.getAddress());
    order.setStatus("Chưa xác nhận");
    order.setDeliveryDate(orderRequest.getDeliveryDate());
    order.setDescription(orderRequest.getDescription());
     // Kiểm tra xem có mã giảm không
    Double finalAmount = (Double) session.getAttribute("finalAmount");
    if (finalAmount == null) {
        // Nếu không có, lấy giá gốc
        finalAmount = cartItemService.getTotalAmount(user.getId());
    }
    order.setTotalAmount(finalAmount);


        List<OrderDetail> orderDetails = new ArrayList<>();
        Order savedOrder = orderService.saveOrder(order, orderDetails);

        // Tạo chi tiết đơn hàng từ giỏ hàng
        for (CartItem cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(cartItem.getProduct());
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(cartItem.getProduct().getPrice());
            orderDetails.add(detail);
        }

        // Lưu lại chi tiết đơn hàng
        orderService.saveOrder(savedOrder, orderDetails);

        // Xóa giỏ hàng sau khi đặt hàng
        cartItemService.clearCartByUserId(user.getId());

        model.addAttribute("success", "Đặt hàng thành công");

        // Nếu có mã giảm giá, cập nhật số lượt sử dụng
        Long promotionId = (Long) session.getAttribute("promotionId");
        if (promotionId != null) {
            Promotion appliedPromotion = promotionService.findByID(promotionId);
            if (appliedPromotion != null) {
                order.setPromotion(appliedPromotion);

                int currentCount = appliedPromotion.getUseCount();
                if (currentCount > 0) {
                    promotionService.updateUseCount(promotionId, currentCount - 1);
                }
            }
        }

        // Xóa session sau khi dùng xong
        session.removeAttribute("finalAmount");
        session.removeAttribute("totalAmount");
        session.removeAttribute("appliedPromotion");

        return showForm(model);
    }

    // Hiển thị lại form đặt hàng với thông tin cần thiết
    public String showForm(Model model ) {
        User user = authService.getUser();
        model.addAttribute("user", user);

        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(user.getId());
        model.addAttribute("cartItems", cartItems);

        model.addAttribute("totalAmount", cartItemService.getTotalAmount(user.getId()));

        model.addAttribute("productCategories", pro_ca_service.findAll());
        model.addAttribute("view", "order");
        return "layouts/layout";
    }
}