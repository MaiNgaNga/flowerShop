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
import com.datn.utils.AuthService;
import com.datn.model.User;
import java.util.Date;

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

    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    /**
     * Trang POS chính: hiển thị danh sách sản phẩm, bộ lọc, giỏ hàng, phân trang.
     * - Endpoint: GET /pos
     * - Nhận các tham số filter, phân trang qua query string.
     * - Trả về view layouts/layout với dữ liệu sản phẩm, bộ lọc, giỏ hàng.
     */
    @GetMapping("")
    public String showPosPage(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String error) {
        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize);
        double min = (minPrice != null) ? minPrice : 0;
        double max = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Product> productPage = posService.filterProducts(color, type, category, searchKeyword, min, max, pageable);
        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);
        if (success != null && success.equals("payment_completed")) {
            model.addAttribute("successMessage", "Thanh toán thành công!");
        }

        if (error != null && error.equals("empty_cart")) {
            model.addAttribute("errorMessage", "Giỏ hàng trống! Vui lòng chọn sản phẩm trước khi thanh toán.");
        }
        if ("qr_generation_failed".equals(error)) {
            model.addAttribute("errorMessage", "Không thể tạo mã QR thanh toán. Vui lòng thử lại hoặc liên hệ hỗ trợ.");
        }

        model.addAttribute("productCategories", productCategoryService.findAll());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("color", color);
        model.addAttribute("type", type);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", min);
        model.addAttribute("maxPrice", max);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("colors", colorService.findAll());
        model.addAttribute("view", "pos");
        return "layouts/layout";
    }

    @SuppressWarnings("unchecked")
    /**
     * API thêm sản phẩm vào giỏ hàng POS (lưu trên session).
     * - Endpoint: POST /pos/cart/add
     * - Tham số: productId (Long)
     * - Trả về: danh sách sản phẩm trong giỏ (List<CartItemDTO>)
     * - Nếu sản phẩm đã có trong giỏ thì tăng số lượng, chưa có thì thêm mới.
     */
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
            // Xử lý giá giảm
            double price = product.getPrice();
            Double priceAfterDiscount = null;
            java.time.LocalDate today = java.time.LocalDate.now();
            if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0
                    && product.getDiscountStart() != null && product.getDiscountEnd() != null
                    && !today.isBefore(product.getDiscountStart()) && !today.isAfter(product.getDiscountEnd())) {
                priceAfterDiscount = product.getPriceAfterDiscount();
                price = priceAfterDiscount;
            }
            item.setPrice(price);
            item.setPriceAfterDiscount(priceAfterDiscount);
            item.setQuantity(1);
            cart.add(item);
        }
        session.setAttribute("cart", cart);
        return cart;
    }

    @SuppressWarnings("unchecked")
    /**
     * API lấy danh sách sản phẩm trong giỏ hàng POS (session).
     * - Endpoint: GET /pos/cart
     * - Trả về: danh sách sản phẩm trong giỏ (List<CartItemDTO>)
     */
    @GetMapping("/cart")
    @ResponseBody
    public List<CartItemDTO> getCart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        return cart;
    }

    @SuppressWarnings("unchecked")
    /**
     * API xử lý thanh toán đơn hàng POS.
     * - Endpoint: POST /pos/checkout
     * - Tham số: paymentMethod (String: cash, qr_code, card)
     * - Lấy giỏ hàng từ session, tạo đơn hàng, sinh mã QR nếu cần.
     * - Trả về: redirect tới trang POS hoặc trang QR code thanh toán.
     * - Nếu giỏ hàng rỗng hoặc chưa đăng nhập sẽ redirect về trang phù hợp.
     */
    @PostMapping("/checkout")
    public String checkout(
            @RequestParam String paymentMethod,
            HttpSession session,
            Model model) {

        try {
            List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");

            if (cart == null || cart.isEmpty()) {
                return "redirect:/pos?error=empty_cart";
            }

            Order order = new Order();
            User user = (User) session.getAttribute("user");

            if (user == null) {
                return "redirect:/login";
            }

            order.setUser(user);
            order.setCreateDate(new Date());
            order.setStatus("Chờ thanh toán");
            order.setOrderType("offline");

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

            // Xử lý theo phương thức thanh toán
            if ("qr_code".equalsIgnoreCase(paymentMethod) || "card".equalsIgnoreCase(paymentMethod)) {
                String qrCodePath = null;
                try {
                    if ("card".equalsIgnoreCase(paymentMethod)) {
                        qrCodePath = qrCodeService.generatePaymentQRCode(orderCode, total, "1234567890");
                    } else if ("qr_code".equalsIgnoreCase(paymentMethod)) {
                        String bankAccount = "19039778212018";
                        String bankCode = "TCB";
                        String accountName = "BUI ANH THIEN";
                        java.io.File qrDir = new java.io.File("target/classes/static/images/qr/");
                        if (!qrDir.exists())
                            qrDir.mkdirs();
                        qrCodePath = qrCodeService.generateBankTransferQR(orderCode, total, bankAccount, bankCode,
                                accountName);
                    }
                    if (qrCodePath != null) {
                        session.setAttribute("pendingOrder", orderCode);
                        session.removeAttribute("cart");
                        // Lưu đường dẫn QR vào session để GET dùng lại
                        session.setAttribute("qrCodePath", qrCodePath);
                        return "redirect:/pos/payment-qr?orderCode=" + orderCode;
                    } else {
                        return "redirect:/pos?error=qr_generation_failed";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "redirect:/pos?error=qr_generation_failed";
                }
            }

            // Thanh toán tiền mặt
            if ("cash".equalsIgnoreCase(paymentMethod)) {
                savedOrder.setStatus("Đã thanh toán");
                orderDAO.save(savedOrder);
                session.removeAttribute("cart");
                // Hiển thị bill để in
                return "redirect:/pos/bill?orderCode=" + orderCode;
            }

            session.removeAttribute("cart");
            return "redirect:/pos?success=payment_completed";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/pos?error=system_error";
        }
    }

    @GetMapping("/bill")
    public String showBill(@RequestParam String orderCode, Model model) {
        Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/pos";
        }
        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", order.getOrderDetails());
        model.addAttribute("totalAmount", order.getTotalAmount());
        model.addAttribute("orderCode", order.getOrderCode());
        return "bill";
    }

    /**
     * Trang hiển thị QR code thanh toán (GET, an toàn reload, không lặp đơn hàng)
     * - Endpoint: GET /pos/payment-qr?orderCode=...
     * - Nếu đơn hàng không còn hoặc đã bị xóa, thông báo và chuyển về POS
     */
    @GetMapping("/payment-qr")
    public String showPaymentQR(@RequestParam String orderCode, Model model, HttpSession session) {
        Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Đơn hàng đã bị xóa hoặc không tồn tại. Vui lòng tạo lại đơn mới!");
            return "redirect:/pos";
        }
        Order order = orderOpt.get();
        // Nếu trạng thái không phải 'Chờ thanh toán', không cho xem QR nữa
        if (!"Chờ thanh toán".equals(order.getStatus())) {
            model.addAttribute("errorMessage", "Đơn hàng đã được xử lý hoặc không còn hiệu lực. Vui lòng tạo đơn mới!");
            return "redirect:/pos";
        }
        // Lấy lại đường dẫn QR từ session hoặc DB
        String qrCodePath = (String) session.getAttribute("qrCodePath");
        if (qrCodePath == null) {
            model.addAttribute("errorMessage", "Không tìm thấy mã QR cho đơn hàng này. Vui lòng tạo lại đơn mới!");
            return "redirect:/pos";
        }
        model.addAttribute("qrCodePath", qrCodePath);
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("totalAmount", order.getTotalAmount());
        model.addAttribute("paymentMethod", order.getOrderType());
        model.addAttribute("orderDetails", order.getOrderDetails());
        model.addAttribute("view", "pos-payment-qr");
        return "layouts/qr-layout";
    }

    /**
     * API huỷ đơn hàng chờ thanh toán POS.
     * - Endpoint: POST /pos/cancel-order
     * - Tham số: orderCode (String)
     * - Nếu đơn hàng ở trạng thái "Chờ thanh toán" thì xoá khỏi database.
     * - Sau đó redirect về trang POS.
     */
    @PostMapping("/cancel-order")
    public String cancelOrder(@RequestParam String orderCode, HttpSession session) {
        Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("Chờ thanh toán".equals(order.getStatus())) {
                orderDAO.delete(order);
            }
        }
        session.removeAttribute("pendingOrder");
        return "redirect:/pos";
    }

    /**
     * Chặn truy cập GET vào /pos/checkout, luôn redirect về trang POS.
     * - Endpoint: GET /pos/checkout
     * - Trả về: redirect với thông báo lỗi.
     */
    @GetMapping("/checkout")
    public String checkoutGet() {
        return "redirect:/pos?error=invalid_request";
    }

    @SuppressWarnings("unchecked")
    /**
     * API xóa sản phẩm khỏi giỏ hàng POS (giảm số lượng hoặc xóa hẳn nếu còn 1).
     * - Endpoint: POST /pos/cart/remove
     * - Tham số: productId (Long)
     * - Trả về: danh sách sản phẩm còn lại trong giỏ (List<CartItemDTO>)
     */
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

    @PostMapping("/manual-confirm-payment")
    /**
     * API xác nhận thanh toán thủ công cho đơn hàng POS.
     * <p>
     * - Endpoint: POST /pos/manual-confirm-payment
     * - Nhận vào JSON body với key "orderCode" (mã đơn hàng cần xác nhận thanh
     * toán)
     * - Trả về JSON thông báo thành công/thất bại và thông điệp tiếng Việt
     * <p>
     * Luồng xử lý:
     * 1. Lấy mã đơn hàng từ request body.
     * 2. Nếu không có mã đơn hàng, trả về lỗi yêu cầu nhập mã.
     * 3. Tìm đơn hàng theo mã. Nếu tìm thấy:
     * - Đổi trạng thái đơn hàng thành "Đã thanh toán" và lưu lại.
     * - Trả về thành công và thông báo xác nhận.
     * Nếu không tìm thấy:
     * - Trả về lỗi không tìm thấy đơn hàng.
     * 4. Nếu có lỗi hệ thống, trả về thông báo lỗi chi tiết.
     *
     * Ví dụ request:
     * {
     * "orderCode": "POS12345"
     * }
     *
     * Ví dụ response thành công:
     * {
     * "success": true,
     * "message": "Đã xác nhận thanh toán cho đơn hàng"
     * }
     *
     * Ví dụ response lỗi:
     * {
     * "success": false,
     * "message": "Không tìm thấy đơn hàng"
     * }
     */
    @ResponseBody
    public Map<String, Object> manualConfirmPayment(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            String orderCode = requestBody.get("orderCode");
            if (orderCode == null || orderCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mã đơn hàng");
                return response;
            }
            Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus("Đã thanh toán");
                orderDAO.save(order);
                response.put("success", true);
                response.put("message", "Đã xác nhận thanh toán cho đơn hàng");
                response.put("redirectUrl", "/pos/bill?orderCode=" + orderCode);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }
}
