package com.datn.Controller.user;

import com.datn.Service.PosService;
import com.datn.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import com.datn.Service.CategoryService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ColorService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.datn.Service.ProductService;
import com.datn.Service.QRCodeService;
import org.springframework.web.bind.annotation.RequestMapping;
import com.datn.dto.CartItemDTO;
import com.datn.model.Order;
import com.datn.model.OrderDetail;
import com.datn.dao.OrderDAO;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/pos")
public class PosController {
    @Autowired
    private PosService posService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ColorService colorService;
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping("")
    public String showPosPage(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize);
        double min = (minPrice != null) ? minPrice : 0;
        double max = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
        Page<Product> productPage = posService.filterProducts(color, type, keyword, min, max, pageable);

        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("color", color);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", min);
        model.addAttribute("maxPrice", max);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("colors", colorService.findAll());
        model.addAttribute("view", "pos");
        return "layouts/layout";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/cart/add")
    @ResponseBody
    public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        Optional<CartItemDTO> existing = cart.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + 1);
        } else {
            Product product = productService.findByID(productId);
            CartItemDTO item = new CartItemDTO();
            item.setProductId(productId);
            item.setName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(1);
            cart.add(item);
        }
        session.setAttribute("cart", cart);
        return cart;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/cart")
    @ResponseBody
    public List<CartItemDTO> getCart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        return cart;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/checkout")
    public String checkout(
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String customerName,
            HttpSession session,
            Model model) {

        try {
            System.out.println("=== POS CHECKOUT DEBUG START ===");
            System.out.println("Payment Method: " + paymentMethod);
            System.out.println("Customer Name: " + customerName);
            System.out.println("Session ID: " + session.getId());

            List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
            System.out.println("Cart size: " + (cart != null ? cart.size() : "null"));

            if (cart == null || cart.isEmpty()) {
                System.out.println("Cart is empty, redirecting...");
                return "redirect:/pos?error=empty_cart";
            }

            // Tạo đơn hàng
            Order order = new Order();
            com.datn.model.User user = (com.datn.model.User) session.getAttribute("user");
            System.out.println("User in session: " + (user != null ? user.getId() : "null"));

            if (user == null) {
                System.out.println("User not logged in, redirecting to login...");
                return "redirect:/login";
            }

            order.setUser(user);
            order.setCreateDate(new java.util.Date());
            order.setStatus("Chờ thanh toán");
            order.setOrderType("offline");

            // Thêm tên khách hàng nếu có
            if (customerName != null && !customerName.trim().isEmpty()) {
                order.setCustomerName(customerName.trim());
            }

            List<OrderDetail> details = new ArrayList<>();
            double total = 0;
            for (CartItemDTO dto : cart) {
                Product product = productService.findByID(dto.getProductId());
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(product);
                detail.setQuantity(dto.getQuantity());
                detail.setPrice(dto.getPrice());
                details.add(detail);
                total += dto.getPrice() * dto.getQuantity();
            }
            order.setOrderDetails(details);
            order.setTotalAmount(total);

            // Lưu đơn hàng
            Order savedOrder = orderDAO.save(order);
            String orderCode = "POS" + savedOrder.getId();

            // Update order với orderCode
            savedOrder.setOrderCode(orderCode);
            savedOrder = orderDAO.save(savedOrder);

            System.out.println("Order saved successfully. OrderCode: " + orderCode);
            System.out.println("Payment method check: " + paymentMethod);
            System.out.println("Checking if payment method is QR or Card...");

            // Xử lý theo phương thức thanh toán
            if ("qr_code".equalsIgnoreCase(paymentMethod) || "card".equalsIgnoreCase(paymentMethod)) {
                System.out.println("Processing QR/Card payment...");

                String qrCodePath = null;
                String bankInfo = "Vietcombank - STK: 1234567890 - CHU TK: Cua Hang Hoa";

                try {
                    System.out.println("Generating QR code for payment method: " + paymentMethod);
                    if ("card".equalsIgnoreCase(paymentMethod)) {
                        // Sinh QR cho thanh toán thẻ (VietQR)
                        qrCodePath = qrCodeService.generatePaymentQRCode(orderCode, total, "1234567890");
                        System.out.println("Generated payment QR path: " + qrCodePath);
                    } else if ("qr_code".equalsIgnoreCase(paymentMethod)) {
                        // Sinh QR cho chuyển khoản ngân hàng
                        qrCodePath = qrCodeService.generateBankTransferQR(orderCode, total, bankInfo);
                        System.out.println("Generated bank transfer QR path: " + qrCodePath);
                    }

                    if (qrCodePath != null) {
                        System.out.println("QR code generated successfully, setting up QR payment page...");
                        System.out.println("Setting qrCodePath to model: " + qrCodePath);

                        // DEBUG: Kiểm tra xem file QR có tồn tại không
                        String fullPath = "src/main/resources/static" + qrCodePath;
                        java.io.File qrFile = new java.io.File(fullPath);
                        System.out.println("QR file full path: " + fullPath);
                        System.out.println("QR file exists: " + qrFile.exists());
                        System.out.println("QR file absolute path: " + qrFile.getAbsolutePath());

                        model.addAttribute("qrCodePath", qrCodePath);
                        model.addAttribute("orderCode", orderCode);
                        model.addAttribute("totalAmount", total);
                        model.addAttribute("paymentMethod", paymentMethod);
                        model.addAttribute("bankInfo", bankInfo);
                        model.addAttribute("customerName", customerName);

                        // Create simple order details without circular reference
                        List<String> orderDetailsList = new ArrayList<>();
                        for (OrderDetail detail : details) {
                            orderDetailsList.add(detail.getProduct().getName() + " x" + detail.getQuantity());
                        }
                        model.addAttribute("orderDetailsList", orderDetailsList);

                        session.setAttribute("pendingOrder", orderCode);
                        session.removeAttribute("cart"); // Clear cart

                        model.addAttribute("view", "pos-payment-qr");
                        System.out.println("Returning QR payment layout...");
                        System.out.println("=== FINAL MODEL ATTRIBUTES ===");
                        System.out.println("qrCodePath: " + model.getAttribute("qrCodePath"));
                        System.out.println("orderCode: " + model.getAttribute("orderCode"));
                        System.out.println("view: " + model.getAttribute("view"));

                        return "layouts/qr-layout"; // Sử dụng layout riêng cho QR payment
                    } else {
                        System.err.println("QR code path is null - QR generation failed silently");
                        return "redirect:/pos?error=qr_generation_failed";
                    }
                } catch (Exception e) {
                    System.err.println("Error generating QR code: " + e.getMessage());
                    e.printStackTrace();
                    return "redirect:/pos?error=qr_generation_failed";
                }
            }

            // Thanh toán tiền mặt
            if ("cash".equalsIgnoreCase(paymentMethod)) {
                savedOrder.setStatus("Đã thanh toán");
                orderDAO.save(savedOrder);
            }

            session.removeAttribute("cart");
            return "redirect:/pos?success=payment_completed";

        } catch (Exception e) {
            System.err.println("=== CHECKOUT ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/pos?error=system_error";
        }
    }

    @GetMapping("/checkout")
    public String checkoutGet() {
        // Redirect GET requests to the main POS page
        return "redirect:/pos?error=invalid_request";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/cart/remove")
    @ResponseBody
    public List<CartItemDTO> removeFromCart(@RequestParam Long productId, HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        for (int i = 0; i < cart.size(); i++) {
            CartItemDTO item = cart.get(i);
            if (item.getProductId().equals(productId)) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    cart.remove(i);
                }
                break;
            }
        }
        session.setAttribute("cart", cart);
        return cart;
    }

    // Test methods
    @GetMapping("/test-qr")
    public String testQRDisplay(Model model) {
        // Simulate QR generation for testing
        String testQrPath = "/images/qr/test_qr.png";
        model.addAttribute("qrCodePath", testQrPath);
        model.addAttribute("totalAmount", 500000);
        model.addAttribute("orderCode", "TEST123");
        System.out.println("Test QR Page - QR Path: " + testQrPath);
        return "test-qr";
    }

    @GetMapping("/qr-image-test")
    @ResponseBody
    public String qrImageTest() {
        // Return HTML to test QR image display directly
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>QR Image Test</title>
                    <style>
                        body { font-family: Arial; padding: 20px; }
                        .qr-container { border: 2px solid #ccc; padding: 20px; text-align: center; }
                        .qr-image { max-width: 300px; border: 1px solid red; }
                    </style>
                </head>
                <body>
                    <h1>QR Image Direct Test</h1>
                    <div class="qr-container">
                        <h3>Test QR Image 1:</h3>
                        <img src="/images/qr/transfer_POS10094.png" alt="QR Code" class="qr-image"
                             onerror="this.innerHTML='<p style=color:red>ERROR: Cannot load QR image</p>'; this.style.display='none';">
                        <p>Path: /images/qr/transfer_POS10094.png</p>
                    </div>

                    <div class="qr-container" style="margin-top: 20px;">
                        <h3>Test QR Image 2 (if exists):</h3>
                        <img src="/images/qr/transfer_POS10095.png" alt="QR Code" class="qr-image"
                             onerror="this.innerHTML='<p style=color:red>ERROR: Cannot load QR image</p>'; this.style.display='none';">
                        <p>Path: /images/qr/transfer_POS10095.png</p>
                    </div>

                    <div style="margin-top: 20px;">
                        <h3>Direct Links:</h3>
                        <a href="/images/qr/transfer_POS10094.png" target="_blank">Open QR Image 1 directly</a><br>
                        <a href="/images/qr/transfer_POS10095.png" target="_blank">Open QR Image 2 directly</a>
                    </div>
                </body>
                </html>
                """;
    }

    @GetMapping("/direct-qr-test")
    @ResponseBody
    public ResponseEntity<String> directQRTest() {
        try {
            // Test tạo QR trực tiếp
            String qrPath = qrCodeService.generateBankTransferQR("TEST123", 100000.0, "Test Bank Info");

            // Tạo HTML response đơn giản
            String html = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head><title>Direct QR Test</title></head>
                    <body>
                        <h1>Direct QR Test</h1>
                        <p><strong>QR Path:</strong> %s</p>
                        <img src="%s" alt="QR Code" style="max-width: 300px; border: 2px solid red;" />
                        <p><a href="%s" target="_blank">Open QR directly</a></p>
                    </body>
                    </html>
                    """, qrPath, qrPath, qrPath);

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body("<h1>ERROR: " + e.getMessage() + "</h1>");
        }
    }

    // API endpoint để check payment status
    @PostMapping("/check-payment-status")
    @ResponseBody
    public Map<String, Object> checkPaymentStatus(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            String orderCode = requestBody.get("orderCode");
            System.out.println("Checking payment status for orderCode: " + orderCode);

            if (orderCode == null || orderCode.isEmpty()) {
                response.put("success", false);
                response.put("status", "missing_order_code");
                response.put("message", "Order code is required");
                return response;
            }

            // Kiểm tra trạng thái thanh toán trong database
            Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                response.put("success", true);
                response.put("status", order.getOrderStatus()); // "pending", "paid", "failed"
                response.put("message", "Payment status retrieved successfully");
                System.out.println("Order found. Status: " + order.getOrderStatus());
            } else {
                response.put("success", false);
                response.put("status", "not_found");
                response.put("message", "Order not found");
                System.out.println("Order not found for orderCode: " + orderCode);
            }
        } catch (Exception e) {
            System.out.println("Error checking payment status: " + e.getMessage());
            response.put("success", false);
            response.put("status", "error");
            response.put("message", "Error checking payment status");
        }
        return response;
    }
}
