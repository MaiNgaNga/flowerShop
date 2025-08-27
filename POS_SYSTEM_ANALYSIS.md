# PHÂN TÍCH TOÀN BỘ LUỒNG XỬ LÝ BÁN HÀNG TẠI QUẦY (POS)

## 1. TỔNG QUAN KIẾN TRÚC

### 1.1 Các Component Chính
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controllers   │    │    Services      │    │      DAOs       │
├─────────────────┤    ├──────────────────┤    ├─────────────────┤
│ PosController   │───▶│ PosService       │───▶│ PosDAO          │
│ PosManager      │    │ OrderService     │    │ OrderDAO        │
│ Controller      │    │ ProductService   │    │ ProductDAO      │
└─────────────────┘    │ QRCodeService    │    └─────────────────┘
                       │ AuthService      │
                       └──────────────────┘
```

### 1.2 Database Tables Liên Quan
- **orders**: Lưu thông tin đơn hàng POS
- **order_details**: Chi tiết sản phẩm trong đơn hàng
- **products**: Sản phẩm được bán
- **categories**: Danh mục sản phẩm
- **colors**: Màu sắc sản phẩm
- **users**: Thông tin nhân viên/khách hàng

## 2. LUỒNG XỬ LÝ CHI TIẾT

### 2.1 Hiển Thị Trang POS
**Controller**: `PosController.showPosPage()`
**Endpoint**: `GET /pos`

```java
@GetMapping
public String showPosPage(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String color,
    @RequestParam(required = false) String type,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Double minPrice,
    @RequestParam(required = false) Double maxPrice,
    Model model)
```

**Luồng xử lý**:
1. Nhận parameters lọc từ frontend
2. Gọi `PosService.filterProducts()` với pagination
3. Load danh sách categories, colors qua các service
4. Trả về view `pos.html` với dữ liệu sản phẩm

**Câu lệnh SQL chính**:
```sql
-- PosDAO.filterProducts()
SELECT p FROM Product p 
WHERE (:color IS NULL OR p.color.name = :color) 
AND (:type IS NULL OR p.productCategory.name = :type) 
AND (:category IS NULL OR p.category.name = :category) 
AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) 
AND (p.price BETWEEN :minPrice AND :maxPrice)
```

### 2.2 Quản Lý Giỏ Hàng POS (Session-based)

#### 2.2.1 Thêm Sản Phẩm Vào Giỏ
**Endpoint**: `POST /pos/cart/add`
**Controller**: `PosController.addToCart()`

```java
@PostMapping("/cart/add")
@ResponseBody
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session)
```

**Luồng xử lý**:
1. Lấy giỏ hàng từ session attribute "cart"
2. Kiểm tra sản phẩm đã có trong giỏ chưa
3. Nếu có: tăng quantity +1
4. Nếu chưa: tạo CartItemDTO mới với quantity = 1
5. Tính giá sau giảm nếu có discount
6. Lưu lại giỏ hàng vào session
7. Trả về danh sách giỏ hàng hiện tại

**Logic tính giá giảm**:
```java
double price = product.getPrice();
LocalDate today = LocalDate.now();
if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0
    && product.getDiscountStart() != null && product.getDiscountEnd() != null
    && !today.isBefore(product.getDiscountStart()) && !today.isAfter(product.getDiscountEnd())) {
    priceAfterDiscount = product.getPriceAfterDiscount();
    price = priceAfterDiscount;
}
```

#### 2.2.2 Xóa Sản Phẩm Khỏi Giỏ
**Endpoint**: `POST /pos/cart/remove`
**Controller**: `PosController.removeFromCart()`

**Luồng xử lý**:
1. Lấy giỏ hàng từ session
2. Tìm sản phẩm theo productId
3. Nếu quantity > 1: giảm quantity -1
4. Nếu quantity = 1: xóa khỏi giỏ hàng
5. Cập nhật session

#### 2.2.3 Lấy Giỏ Hàng Hiện Tại
**Endpoint**: `GET /pos/cart`
**Controller**: `PosController.getCart()`

### 2.3 Xử Lý Thanh Toán

#### 2.3.1 Tạo Đơn Hàng POS
**Endpoint**: `POST /pos/checkout`
**Controller**: `PosController.checkout()`

```java
@PostMapping("/checkout")
public String checkout(
    @RequestParam String paymentMethod,
    HttpSession session,
    Model model)
```

**Luồng xử lý**:
1. **Validate dữ liệu đầu vào**:
   - Kiểm tra giỏ hàng không rỗng
   - Kiểm tra user đã đăng nhập (POS cần nhân viên)

2. **Tạo đơn hàng**:
   ```java
   Order order = new Order();
   order.setUser(user);
   order.setOrderType("Offline");
   order.setCreateDate(new Date());
   order.setStatus("Chờ thanh toán");
   
   // Tùy theo phương thức thanh toán
   if ("cash".equalsIgnoreCase(paymentMethod)) {
       order.setStatus("Hoàn tất");
       order.setPaymentStatus("Đã thanh toán");
   }
   ```

3. **Tạo OrderDetails**:
   ```java
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
   ```

4. **Lưu đơn hàng và sinh mã**:
   ```java
   Order savedOrder = orderDAO.save(order);
   String orderCode = "POS" + savedOrder.getId();
   savedOrder.setOrderCode(orderCode);
   savedOrder = orderDAO.save(savedOrder);
   ```

5. **Xử lý theo phương thức thanh toán**:
   - **Cash**: Redirect đến bill để in hóa đơn
   - **QR Code**: Sinh QR code và redirect đến trang thanh toán
   - **Card**: Xử lý tương tự QR

#### 2.3.2 Sinh QR Code Thanh Toán
**Service**: `QRCodeService`

##### 2.3.2.1 Các Loại QR Code
1. **QR Code Chuyển Khoản Ngân Hàng (VietQR)**
2. **QR Code Thanh Toán Thẻ**

##### 2.3.2.2 Xử Lý QR Chuyển Khoản Ngân Hàng
**Method**: `QRCodeService.generateBankTransferQR()`

**Luồng xử lý**:
1. **Chuẩn bị thông tin ngân hàng**:
   ```java
   String bankAccount = "19039778212018";    // Số tài khoản
   String bankCode = "TCB";                  // Mã ngân hàng Techcombank
   String accountName = "BUI ANH THIEN";     // Tên tài khoản
   String acqId = "970407";                  // Mã AcqId của Techcombank
   ```

2. **Tạo nội dung chuyển khoản**:
   ```java
   String addInfo = "Thanh toan don hang " + orderCode;
   ```

3. **Gọi API VietQR**:
   ```java
   String apiUrl = "https://api.vietqr.io/v2/generate";
   String jsonBody = String.format(
       "{\"accountNo\":\"%s\",\"accountName\":\"%s\",\"acqId\":\"%s\",\"amount\":%.0f,\"addInfo\":\"%s\",\"format\":\"png\"}",
       bankAccount, safeAccountName, acqId, amount, safeAddInfo);
   ```

4. **Xử lý phản hồi API**:
   - Parse JSON response từ VietQR API
   - Extract base64 image từ field "qrDataURL" hoặc "qrData"
   - Decode base64 thành file PNG
   - Lưu file với tên: `transfer_{orderCode}.png`

5. **Trả về đường dẫn**:
   ```java
   return "/images/qr/" + fileName;
   ```

##### 2.3.2.3 Xử Lý QR Thanh Toán Thẻ
**Method**: `QRCodeService.generatePaymentQRCode()`

**Luồng xử lý**:
1. **Tạo nội dung QR theo chuẩn EMV**:
   ```java
   String qrContent = String.format(
       "00020101021138570010A00000072701270006970455011%s0208QRIBFTTA5303704540%.0f5802VN6304",
       bankAccount, amount);
   ```

2. **Sinh QR Code image**:
   ```java
   QRCodeWriter qrCodeWriter = new QRCodeWriter();
   BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);
   Path path = FileSystems.getDefault().getPath(QR_CODE_IMAGE_PATH + fileName);
   MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
   ```

3. **Trả về đường dẫn**: `/images/qr/payment_{orderCode}.png`

##### 2.3.2.4 Xử Lý Trong Controller
**File**: `PosController.checkout()`

```java
if ("qr_code".equalsIgnoreCase(paymentMethod) || "card".equalsIgnoreCase(paymentMethod)) {
    String qrCodePath = null;
    try {
        if ("card".equalsIgnoreCase(paymentMethod)) {
            qrCodePath = qrCodeService.generatePaymentQRCode(orderCode, total, "1234567890");
        } else if ("qr_code".equalsIgnoreCase(paymentMethod)) {
            qrCodePath = qrCodeService.generateBankTransferQR(orderCode, total, 
                bankAccount, bankCode, accountName);
        }
        
        if (qrCodePath != null) {
            session.setAttribute("pendingOrder", orderCode);
            session.removeAttribute("cart");
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
```

#### 2.3.3 Hiển Thị QR Code Thanh Toán
**Endpoint**: `GET /pos/payment-qr?orderCode=...`
**Controller**: `PosController.showPaymentQR()`
**Template**: `pos-payment-qr.html`

##### 2.3.3.1 Luồng xử lý Backend
```java
@GetMapping("/payment-qr")
public String showPaymentQR(@RequestParam String orderCode, Model model, HttpSession session) {
    // 1. Validate đơn hàng tồn tại
    Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
    if (orderOpt.isEmpty()) {
        model.addAttribute("errorMessage", "Đơn hàng đã bị xóa hoặc không tồn tại.");
        return "redirect:/pos";
    }
    
    Order order = orderOpt.get();
    
    // 2. Kiểm tra trạng thái đơn hàng
    if (!"Chờ thanh toán".equals(order.getStatus())) {
        model.addAttribute("errorMessage", "Đơn hàng đã được xử lý hoặc không còn hiệu lực.");
        return "redirect:/pos";
    }
    
    // 3. Lấy đường dẫn QR từ session
    String qrCodePath = (String) session.getAttribute("qrCodePath");
    if (qrCodePath == null) {
        model.addAttribute("errorMessage", "Không tìm thấy mã QR cho đơn hàng này.");
        return "redirect:/pos";
    }
    
    // 4. Truyền dữ liệu cho template
    model.addAttribute("qrCodePath", qrCodePath);
    model.addAttribute("orderCode", orderCode);
    model.addAttribute("totalAmount", order.getTotalAmount());
    model.addAttribute("paymentMethod", order.getOrderType());
    model.addAttribute("orderDetails", order.getOrderDetails());
    model.addAttribute("view", "pos-payment-qr");
    
    return "layouts/pos-layout";
}
```

##### 2.3.3.2 Giao Diện Template
**Layout**: 2 cột responsive
- **Cột trái**: Hiển thị QR code và thông tin đơn hàng
- **Cột phải**: Chi tiết đơn hàng và countdown timer

**Các thành phần chính**:
1. **QR Code Image**: 
   ```html
   <img th:src="${qrCodePath}" alt="QR Code thanh toán" class="qr-img" 
        onerror="console.error('QR Code load failed:', this.src);" />
   ```

2. **Thông tin đơn hàng**:
   ```html
   <div class="qr-order-info">
     <div class="info-block">
       <div class="info-label">Mã đơn</div>
       <div class="info-value" th:text="${orderCode}"></div>
     </div>
     <div class="info-block">
       <div class="info-label">Số tiền</div>
       <div class="info-value text-danger" 
            th:text="${#numbers.formatDecimal(totalAmount, 0, 'COMMA', 0, 'POINT')} + ' VNĐ'"></div>
     </div>
   </div>
   ```

3. **Chi tiết sản phẩm**:
   ```html
   <table class="table qr-summary-table">
     <tbody>
       <tr th:each="detail : ${orderDetails}">
         <td><small th:text="${detail.product.name}"></small></td>
         <td class="text-center" th:text="${detail.quantity}"></td>
         <td class="text-end">
           <small th:text="${#numbers.formatDecimal(detail.price * detail.quantity, 0, 'COMMA', 0, 'POINT')}"></small>
         </td>
       </tr>
     </tbody>
   </table>
   ```

4. **Countdown Timer**:
   ```html
   <div class="qr-countdown">
     <span id="countdown">15:00</span>
     <div class="progress">
       <div id="progressBar" class="progress-bar"></div>
     </div>
   </div>
   ```

##### 2.3.3.3 JavaScript Frontend Logic
**File**: `pos-payment-qr.js`

**Chức năng chính**:
1. **Countdown Timer**:
   ```javascript
   let timeLeft = 15 * 60; // 15 phút
   function updateCountdown() {
     const minutes = Math.floor(timeLeft / 60);
     const seconds = timeLeft % 60;
     document.getElementById("countdown").textContent = 
       `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
     
     // Cập nhật progress bar
     const percentage = (timeLeft / totalTime) * 100;
     progressBar.style.width = percentage + "%";
     
     // Đổi màu theo thời gian còn lại
     if (percentage < 30) {
       progressBar.className = "progress-bar bg-danger";
     } else if (percentage < 60) {
       progressBar.className = "progress-bar bg-warning";
     }
     
     // Hết thời gian -> redirect
     if (timeLeft <= 0) {
       alert("⏰ Hết thời gian thanh toán! Đơn hàng sẽ bị hủy.");
       window.location.href = "/pos?error=timeout";
       return;
     }
     timeLeft--;
   }
   setInterval(updateCountdown, 1000);
   ```

2. **Xác nhận thanh toán thủ công**:
   ```javascript
   document.getElementById("confirmPaymentActionBtn").onclick = function() {
     let orderCode = this.getAttribute("data-ordercode");
     fetch("/pos/manual-confirm-payment", {
       method: "POST",
       headers: { "Content-Type": "application/json" },
       body: JSON.stringify({ orderCode: orderCode })
     })
     .then(res => res.json())
     .then(data => {
       if (data.success) {
         // Đóng modal và chuyển đến bill
         if (data.redirectUrl) {
           window.open(data.redirectUrl, "_blank");
         }
         window.location.href = "/pos?success=payment_completed";
       } else {
         alert("Lỗi: " + data.message);
       }
     });
   };
   ```

3. **In QR Code**:
   ```javascript
   document.getElementById("printQRBtn").addEventListener("click", function() {
     const qrImage = document.querySelector('img[alt="QR Code thanh toán"]');
     
     // Convert image to base64 cho print
     fetch(qrImage.src)
       .then(res => res.blob())
       .then(blob => {
         const reader = new FileReader();
         reader.onloadend = function() {
           printQR(`<img src="${reader.result}" alt="QR Code">`);
         };
         reader.readAsDataURL(blob);
       });
   });
   
   function printQR(qrImageHtml) {
     const printWindow = window.open("", "_blank");
     printWindow.document.write(`
       <html>
       <head>
         <title>In mã QR - ${orderCode}</title>
         <style>
           body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }
           .qr-container { border: 2px solid #333; padding: 20px; margin: 20px auto; max-width: 400px; }
           img { max-width: 300px; margin: 20px 0; }
         </style>
       </head>
       <body>
         <div class="qr-container">
           <h2>🌸 Cửa Hàng Hoa</h2>
           <div class="info">Mã đơn hàng: <strong>${orderCode}</strong></div>
           ${qrImageHtml}
           <div class="total">Tổng tiền: ${totalAmount}</div>
           <div class="info">Quét mã QR để thanh toán</div>
         </div>
       </body>
       </html>
     `);
     printWindow.print();
   }
   ```

4. **Hủy đơn hàng**:
   ```javascript
   document.getElementById("confirmCancelBtn").addEventListener("click", function() {
     document.getElementById("cancelOrderForm").submit();
   });
   ```

##### 2.3.3.4 CSS Styling
**File**: `pos-payment-qr.css`

**Highlights**:
- Responsive 2-column layout
- Gradient background và modern UI
- QR code với shadow effect
- Animated progress bar
- Color-coded status indicators

### 2.4 Xử Lý Thanh Toán Thủ Công & QR Code

#### 2.4.1 Xác Nhận Thanh Toán Thủ Công
**Endpoint**: `POST /pos/manual-confirm-payment`
**Controller**: `PosController.manualConfirmPayment()`

```java
@PostMapping("/manual-confirm-payment")
@ResponseBody
public Map<String, Object> manualConfirmPayment(@RequestBody Map<String, String> requestBody)
```

**Luồng xử lý**:
1. **Validate input**:
   ```java
   String orderCode = requestBody.get("orderCode");
   if (orderCode == null || orderCode.isEmpty()) {
       response.put("success", false);
       response.put("message", "Vui lòng nhập mã đơn hàng");
       return response;
   }
   ```

2. **Tìm và cập nhật đơn hàng**:
   ```java
   Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
   if (orderOpt.isPresent()) {
       Order order = orderOpt.get();
       // Khi xác nhận thanh toán thủ công cho chuyển khoản
       order.setStatus("Hoàn tất");
       order.setPaymentStatus("Đã thanh toán");
       orderDAO.save(order);
       
       response.put("success", true);
       response.put("message", "Đã xác nhận thanh toán cho đơn hàng");
       response.put("redirectUrl", "/pos/bill?orderCode=" + orderCode);
   } else {
       response.put("success", false);
       response.put("message", "Không tìm thấy đơn hàng");
   }
   ```

3. **Trả về JSON response**

**Use Cases**:
- Nhân viên xác nhận khách hàng đã chuyển khoản thành công
- Xử lý các trường hợp thanh toán offline (tiền mặt, chuyển khoản trực tiếp)
- Backup method khi QR code không hoạt động

#### 2.4.2 Quy Trình Thanh Toán QR Code Hoàn Chỉnh

##### 2.4.2.1 Workflow Tổng Thể
```
1. Khách hàng chọn sản phẩm → Giỏ hàng (Session)
2. Nhân viên chọn "Thanh toán QR" → POST /pos/checkout
3. Hệ thống tạo đơn hàng với status "Chờ thanh toán"
4. Gọi VietQR API để sinh QR chuyển khoản
5. Lưu QR path vào session → Redirect /pos/payment-qr
6. Hiển thị QR code với countdown 15 phút
7. Khách hàng scan QR và chuyển khoản
8. Nhân viên xác nhận thanh toán → POST /pos/manual-confirm-payment
9. Cập nhật status = "Hoàn tất" → Redirect /pos/bill
10. In hóa đơn
```

##### 2.4.2.2 Error Handling
1. **QR Generation Failed**:
   ```java
   if (qrCodePath == null) {
       return "redirect:/pos?error=qr_generation_failed";
   }
   ```

2. **Order Not Found**:
   ```java
   if (orderOpt.isEmpty()) {
       model.addAttribute("errorMessage", "Đơn hàng đã bị xóa hoặc không tồn tại.");
       return "redirect:/pos";
   }
   ```

3. **Timeout Handling**:
   ```javascript
   if (timeLeft <= 0) {
       alert("⏰ Hết thời gian thanh toán! Đơn hàng sẽ bị hủy.");
       window.location.href = "/pos?error=timeout";
   }
   ```

4. **QR Image Load Failed**:
   ```html
   <img th:src="${qrCodePath}" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';" />
   <div id="qr-fallback" style="display: none;">
     <p>Không thể tải QR Code</p>
     <a th:href="${qrCodePath}" target="_blank">Xem QR trực tiếp</a>
   </div>
   ```

##### 2.4.2.3 Session Management
**Key session attributes**:
- `cart`: Giỏ hàng hiện tại
- `qrCodePath`: Đường dẫn file QR code
- `pendingOrder`: Mã đơn hàng đang chờ thanh toán

**Cleanup logic**:
```java
// Sau khi tạo đơn hàng thành công
session.setAttribute("pendingOrder", orderCode);
session.removeAttribute("cart");
session.setAttribute("qrCodePath", qrCodePath);

// Khi hủy đơn hàng
session.removeAttribute("pendingOrder");
```

##### 2.4.2.4 Database Schema Impact
**Order table fields**:
- `order_code`: Mã đơn hàng (POS{id})
- `order_type`: "Offline" cho POS
- `status`: "Chờ thanh toán" → "Hoàn tất"
- `payment_status`: "Chưa thanh toán" → "Đã thanh toán"
- `create_date`: Thời gian tạo đơn
- `total_amount`: Tổng tiền

**File storage**:
- QR images lưu tại: `target/classes/static/images/qr/`
- Naming pattern: `transfer_{orderCode}.png`, `payment_{orderCode}.png`

##### 2.4.2.5 Security Considerations
1. **Order validation**: Kiểm tra orderCode hợp lệ
2. **Status validation**: Chỉ cho phép xác nhận đơn "Chờ thanh toán"
3. **Session security**: QR path chỉ lưu trong session của user tạo
4. **Timeout protection**: Auto-expire sau 15 phút
5. **CSRF protection**: Sử dụng form token cho cancel order

#### 2.4.2 Hủy Đơn Hàng
**Endpoint**: `POST /pos/cancel-order`
**Controller**: `PosController.cancelOrder()`

**Luồng xử lý**:
1. Tìm đơn hàng theo orderCode
2. Kiểm tra trạng thái "Chờ thanh toán"
3. Xóa đơn hàng khỏi database
4. Clear session và redirect về POS

### 2.5 In Hóa Đơn
**Endpoint**: `GET /pos/bill?orderCode=...`
**Controller**: `PosController.showBill()`

**Luồng xử lý**:
1. Tìm đơn hàng theo orderCode
2. Load thông tin chi tiết đơn hàng
3. Hiển thị template bill để in

## 3. QUẢN LÝ ĐỚN HÀNG POS (ADMIN)

### 3.1 Danh Sách Đơn Hàng POS
**Controller**: `PosManagerController.listOrders()`
**Endpoint**: `GET /admin/pos/index`

**Luồng xử lý**:
1. **Nhận parameters lọc**:
   - fromDate, toDate: Lọc theo ngày tạo
   - orderCode: Tìm kiếm theo mã đơn hàng
   - page, size: Phân trang

2. **Validate dữ liệu**:
   ```java
   // Parse và validate ngày
   LocalDate from = LocalDate.parse(fromDate);
   LocalDate to = LocalDate.parse(toDate);
   
   // Kiểm tra logic ngày
   if (from != null && to != null && from.isAfter(to)) {
       model.addAttribute("error", "'Từ ngày' không thể sau 'Đến ngày'");
       return "admin/layout";
   }
   ```

3. **Gọi service lấy dữ liệu**:
   ```java
   Page<Order> orders;
   if (orderCode != null && !orderCode.isEmpty()) {
       orders = orderService.searchPosOrdersByOrderCode("Offline", orderCode, from, to, 
           PageRequest.of(page, size, Sort.Direction.DESC, "createDate"));
   } else {
       orders = orderService.getPosOrdersByType("Offline", from, to, 
           PageRequest.of(page, size, Sort.Direction.DESC, "createDate"));
   }
   ```

4. **Trả về view với pagination**

### 3.2 Câu Lệnh SQL Trong OrderDAO

#### 3.2.1 Lấy Đơn Hàng POS Theo Loại
```java
@Query("SELECT o FROM Order o WHERE o.orderType = :orderType "
    + "AND (:fromDate IS NULL OR o.createDate >= :fromDate) "
    + "AND (:toDate IS NULL OR o.createDate <= :toDate) "
    + "ORDER BY o.createDate DESC")
Page<Order> findPosOrders(@Param("orderType") String orderType,
    @Param("fromDate") LocalDate fromDate,
    @Param("toDate") LocalDate toDate,
    Pageable pageable);
```

#### 3.2.2 Tìm Kiếm Đơn Hàng Theo Mã
```java
@Query("SELECT o FROM Order o WHERE o.orderType = :orderType "
    + "AND (:fromDate IS NULL OR o.createDate >= :fromDate) "
    + "AND (:toDate IS NULL OR o.createDate <= :toDate) "
    + "AND o.orderCode LIKE %:orderCode% "
    + "ORDER BY o.createDate DESC")
Page<Order> searchPosOrdersByOrderCode(
    @Param("orderType") String orderType,
    @Param("orderCode") String orderCode,
    @Param("fromDate") LocalDate fromDate,
    @Param("toDate") LocalDate toDate,
    Pageable pageable);
```

#### 3.2.3 Tìm Đơn Hàng Theo Mã Code
```java
@Query("SELECT o FROM Order o WHERE UPPER(o.orderCode) = UPPER(:orderCode)")
Optional<Order> findByOrderCode(@Param("orderCode") String orderCode);
```

## 4. SERVICE LAYER

### 4.1 PosService
**File**: `PosServiceImpl.java`

**Chức năng**:
- `getAllProducts()`: Lấy tất cả sản phẩm
- `getProductsPage()`: Lấy sản phẩm có phân trang
- `filterProducts()`: Lọc sản phẩm theo điều kiện

### 4.2 OrderService
**File**: `OrderServiceImpl.java`

**Các method liên quan POS**:
```java
// Lấy đơn hàng POS theo loại
Page<Order> getPosOrdersByType(String orderType, LocalDate fromDate, LocalDate toDate, Pageable pageable)

// Tìm kiếm đơn hàng POS theo mã
Page<Order> searchPosOrdersByOrderCode(String orderType, String orderCode, LocalDate fromDate, LocalDate toDate, Pageable pageable)

// Lấy tất cả đơn hàng offline
List<Order> getAllOfflineOrders()

// Lưu đơn hàng với chi tiết
Order saveOrder(Order order, List<OrderDetail> orderDetails)
```

## 5. TEMPLATE VÀ FRONTEND

### 5.1 Template Chính
- **pos.html**: Giao diện bán hàng tại quầy
- **pos-payment-qr.html**: Hiển thị QR code thanh toán
- **bill.html**: Template in hóa đơn
- **admin/posCRUD.html**: Quản lý đơn hàng POS

### 5.2 JavaScript Frontend
**Các chức năng chính**:
- AJAX call để thêm/xóa sản phẩm khỏi giỏ hàng
- Cập nhật realtime giỏ hàng và tổng tiền
- Xử lý thanh toán và hiển thị QR
- Auto-refresh trạng thái thanh toán

## 6. WORKFLOW TỔNG THỂ

```
1. Nhân viên truy cập /pos
2. Chọn sản phẩm → AJAX call /pos/cart/add
3. Kiểm tra giỏ hàng → GET /pos/cart
4. Thanh toán → POST /pos/checkout
5. Tùy phương thức:
   - Cash: Redirect /pos/bill
   - QR: Redirect /pos/payment-qr
   - Manual confirm: POST /pos/manual-confirm-payment
6. Admin quản lý: GET /admin/pos/index
```

## 7. CÁC ĐIỂM QUAN TRỌNG

### 7.1 Session Management
- Giỏ hàng lưu trong session với key "cart"
- QR code path lưu trong session
- User authentication check cho POS

### 7.2 Database Transactions
- Đơn hàng và chi tiết được lưu trong transaction
- OrderCode được generate sau khi save: "POS" + orderId

### 7.3 Error Handling
- Validate input parameters
- Handle empty cart
- Check user authentication
- Database exception handling

### 7.4 Security
- Check user role cho POS access
- Validate orderCode trước khi xử lý thanh toán
- Session-based cart để tránh conflict

### 7.5 Performance
- Pagination cho danh sách sản phẩm và đơn hàng
- AJAX calls để không reload trang
- Efficient SQL queries với indexing

## 8. EXTENSION POINTS

### 8.1 Có Thể Mở Rộng
- Thêm payment gateway khác
- In hóa đơn tự động qua printer
- Báo cáo thống kê POS chi tiết
- Quản lý kho realtime
- Tích hợp với máy quét mã vạch

### 8.2 Các Cải Tiến Tiềm Năng
- Redis cache cho session management
- WebSocket cho real-time updates
- Mobile-responsive POS interface
- Advanced inventory management
- Customer loyalty program integration
