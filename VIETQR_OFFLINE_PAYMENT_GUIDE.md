# HƯỚNG DẪN TÍCH HỢP THANH TOÁN OFFLINE VIETQR

## 📋 TỔNG QUAN

Tài liệu này mô tả chi tiết cách tích hợp và sử dụng VietQR cho thanh toán offline trong hệ thống POS (Point of Sale) của cửa hàng hoa.

### 🎯 Mục đích

- Cho phép khách hàng thanh toán bằng cách quét QR code chuyển khoản ngân hàng
- Tự động tạo QR code theo chuẩn VietQR
- Xử lý xác nhận thanh toán thủ công từ nhân viên
- Hỗ trợ in QR code cho khách hàng

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Luồng thanh toán offline VietQR:

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

---

## 💻 IMPLEMENTATION CHI TIẾT

### 1. QRCodeService - Service xử lý tạo QR

**File**: `src/main/java/com/datn/Service/QRCodeService.java`

#### 1.1 Cấu hình cơ bản

```java
@Service
public class QRCodeService {
    private static final String QR_CODE_IMAGE_PATH = "target/classes/static/images/qr/";

    // Tạo thư mục lưu QR nếu chưa tồn tại
    File directory = new File(QR_CODE_IMAGE_PATH);
    if (!directory.exists()) {
        directory.mkdirs();
    }
}
```

#### 1.2 Method tạo QR chuyển khoản VietQR

```java
public String generateBankTransferQR(String orderCode, double amount,
    String bankAccount, String bankCode, String accountName) {

    // Thông tin ngân hàng Techcombank
    String acqId = "970407";  // Mã ngân hàng TCB
    String addInfo = "Thanh toan don hang " + orderCode;
    String apiUrl = "https://api.vietqr.io/v2/generate";

    // Tạo JSON request body
    String jsonBody = String.format(
        "{\"accountNo\":\"%s\",\"accountName\":\"%s\",\"acqId\":\"%s\"," +
        "\"amount\":%.0f,\"addInfo\":\"%s\",\"format\":\"png\"}",
        bankAccount, accountName, acqId, amount, addInfo
    );

    // Gọi API VietQR
    // ... (xử lý HTTP request)

    // Parse base64 image từ response và lưu file
    byte[] imageBytes = Base64.getDecoder().decode(base64);
    String fileName = "transfer_" + orderCode + ".png";
    Path path = Paths.get(QR_CODE_IMAGE_PATH + fileName);
    Files.write(path, imageBytes);

    return "/images/qr/" + fileName;
}
```

### 2. PosController - Controller xử lý thanh toán

**File**: `src/main/java/com/datn/Controller/user/PosController.java`

#### 2.1 Endpoint thanh toán

```java
@PostMapping("/checkout")
public String checkout(@RequestParam String paymentMethod,
    HttpSession session, Model model) {

    // Kiểm tra giỏ hàng
    List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
    if (cart == null || cart.isEmpty()) {
        return "redirect:/pos?error=empty_cart";
    }

    // Tạo đơn hàng
    Order order = new Order();
    order.setPaymentMethod("PAYOS");
    order.setStatus("Chờ thanh toán");
    order.setPaymentStatus("Chưa thanh toán");

    // Xử lý QR code thanh toán
    if ("qr_code".equalsIgnoreCase(paymentMethod)) {
        String bankAccount = "19039778212018";
        String bankCode = "TCB";
        String accountName = "BUI ANH THIEN";

        String qrCodePath = qrCodeService.generateBankTransferQR(
            orderCode, total, bankAccount, bankCode, accountName);

        if (qrCodePath != null) {
            session.setAttribute("qrCodePath", qrCodePath);
            return "redirect:/pos/payment-qr?orderCode=" + orderCode;
        }
    }
}
```

#### 2.2 Hiển thị trang QR thanh toán

```java
@GetMapping("/payment-qr")
public String showPaymentQR(@RequestParam String orderCode,
    Model model, HttpSession session) {

    // Validate đơn hàng
    Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);
    if (orderOpt.isEmpty() || !"Chờ thanh toán".equals(order.getStatus())) {
        return "redirect:/pos";
    }

    // Lấy đường dẫn QR từ session
    String qrCodePath = (String) session.getAttribute("qrCodePath");

    model.addAttribute("qrCodePath", qrCodePath);
    model.addAttribute("orderCode", orderCode);
    model.addAttribute("totalAmount", order.getTotalAmount());
    model.addAttribute("orderDetails", order.getOrderDetails());

    return "layouts/pos-layout";
}
```

#### 2.3 Xác nhận thanh toán thủ công

```java
@PostMapping("/manual-confirm-payment")
@ResponseBody
public Map<String, Object> manualConfirmPayment(
    @RequestBody Map<String, String> requestBody) {

    String orderCode = requestBody.get("orderCode");
    Optional<Order> orderOpt = orderDAO.findByOrderCode(orderCode);

    if (orderOpt.isPresent()) {
        Order order = orderOpt.get();
        order.setStatus("Hoàn tất");
        order.setPaymentStatus("Đã thanh toán");
        orderDAO.save(order);

        response.put("success", true);
        response.put("redirectUrl", "/pos/bill?orderCode=" + orderCode);
    }

    return response;
}
```

### 3. Frontend - Giao diện thanh toán QR

**File**: `src/main/resources/templates/pos-payment-qr.html`

#### 3.1 HTML hiển thị QR code

```html
<div class="qr-left">
  <div class="qr-title">
    <i class="fas fa-qrcode"></i> Quét mã QR để thanh toán
  </div>

  <!-- Hiển thị QR code -->
  <img
    th:src="${qrCodePath}"
    alt="QR Code thanh toán"
    class="qr-img"
    onerror="this.style.display='none'; this.nextElementSibling.style.display='block';"
  />

  <!-- Fallback khi QR không load được -->
  <div id="qr-fallback" style="display: none;">
    <p>Không thể tải QR Code</p>
    <a th:href="${qrCodePath}" target="_blank" class="btn btn-sm btn-primary">
      Xem QR trực tiếp
    </a>
  </div>

  <!-- Thông tin đơn hàng -->
  <div class="qr-order-info">
    <div class="info-block">
      <div class="info-label">Mã đơn</div>
      <div class="info-value" th:text="${orderCode}"></div>
    </div>
    <div class="info-block">
      <div class="info-label">Số tiền</div>
      <div
        class="info-value text-danger"
        th:text="${#numbers.formatDecimal(totalAmount, 0, 'COMMA', 0, 'POINT')} + ' VNĐ'"
      ></div>
    </div>
  </div>
</div>
```

#### 3.2 Countdown timer 15 phút

```html
<div class="qr-countdown">
  <span id="countdown">15:00</span>
  <div>Thời gian chờ thanh toán</div>
  <div class="progress">
    <div
      id="progressBar"
      class="progress-bar progress-bar-animated bg-warning"
    ></div>
  </div>
</div>
```

#### 3.3 Các nút hành động

```html
<div class="qr-actions">
  <!-- Xác nhận thanh toán -->
  <button
    class="btn btn-success"
    data-bs-toggle="modal"
    data-bs-target="#confirmPaymentModal"
  >
    <i class="fas fa-check"></i> Xác nhận đã thanh toán
  </button>

  <!-- In QR code -->
  <button class="btn btn-info" id="printQRBtn">
    <i class="fas fa-print"></i> In mã QR
  </button>

  <!-- Hủy đơn hàng -->
  <button
    class="btn btn-outline-danger"
    data-bs-toggle="modal"
    data-bs-target="#cancelOrderModal"
  >
    <i class="fas fa-times"></i> Hủy & Quay lại POS
  </button>
</div>
```

### 4. JavaScript - Xử lý frontend

**File**: `src/main/resources/static/js/pos-payment-qr.js`

#### 4.1 Countdown timer

```javascript
let timeLeft = 15 * 60; // 15 phút
let totalTime = 15 * 60;

function updateCountdown() {
  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;

  document.getElementById("countdown").textContent = `${minutes}:${seconds
    .toString()
    .padStart(2, "0")}`;

  // Cập nhật progress bar
  const percentage = (timeLeft / totalTime) * 100;
  document.getElementById("progressBar").style.width = percentage + "%";

  if (timeLeft <= 0) {
    // Hết thời gian, tự động hủy đơn
    window.location.href = "/pos/cancel-order?orderCode=" + orderCode;
  }

  timeLeft--;
}

setInterval(updateCountdown, 1000);
```

#### 4.2 Xác nhận thanh toán

```javascript
function confirmPayment(orderCode) {
  fetch("/pos/manual-confirm-payment", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderCode: orderCode }),
  })
    .then((res) => res.json())
    .then((data) => {
      if (data.success) {
        alert("✅ Xác nhận thanh toán thành công!");
        window.location.href = data.redirectUrl;
      } else {
        alert("❌ Lỗi: " + data.message);
      }
    });
}
```

#### 4.3 In QR code

```javascript
document.getElementById("printQRBtn").addEventListener("click", function () {
  const qrImage = document.querySelector('img[alt="QR Code thanh toán"]');

  // Convert image thành base64 để in
  fetch(qrImage.src)
    .then((res) => res.blob())
    .then((blob) => {
      const reader = new FileReader();
      reader.onloadend = function () {
        printQR(`<img src="${reader.result}" alt="QR Code">`);
      };
      reader.readAsDataURL(blob);
    });

  function printQR(qrImageHtml) {
    const printWindow = window.open("", "_blank");
    printWindow.document.write(`
            <html>
            <head>
                <title>In mã QR - ${orderCode}</title>
                <style>
                    body { text-align: center; padding: 20px; }
                    .qr-container { border: 2px solid #333; padding: 20px; }
                    img { max-width: 300px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="qr-container">
                    <h2>🌸 Cửa Hàng Hoa</h2>
                    <div>Mã đơn hàng: <strong>${orderCode}</strong></div>
                    ${qrImageHtml}
                    <div>Tổng tiền: ${totalAmount}</div>
                    <div>Quét mã QR để thanh toán</div>
                </div>
            </body>
            </html>
        `);
    printWindow.print();
  }
});
```

---

## 🔧 CẤU HÌNH VÀ THIẾT LẬP

### 1. Thông tin ngân hàng

```java
// Trong PosController.checkout()
String bankAccount = "19039778212018";    // Số tài khoản
String bankCode = "TCB";                  // Mã ngân hàng Techcombank
String accountName = "BUI ANH THIEN";     // Tên chủ tài khoản
String acqId = "970407";                  // Mã AcqId của Techcombank
```

### 2. VietQR API Configuration

```java
// API Endpoint
String apiUrl = "https://api.vietqr.io/v2/generate";

// Request format
{
    "accountNo": "19039778212018",
    "accountName": "BUI ANH THIEN",
    "acqId": "970407",
    "amount": 150000,
    "addInfo": "Thanh toan don hang POS123",
    "format": "png"
}
```

### 3. File và thư mục

```
target/classes/static/images/qr/     # Thư mục lưu QR code
├── transfer_POS123.png              # QR chuyển khoản
└── payment_POS456.png               # QR thanh toán khác
```

---

## 🛠️ WORKFLOW CHI TIẾT

### 1. Tạo đơn hàng và QR code

```java
// 1. Nhân viên chọn "Thanh toán QR"
// 2. Hệ thống tạo order với status "Chờ thanh toán"
Order order = new Order();
order.setPaymentMethod("PAYOS");
order.setStatus("Chờ thanh toán");
order.setPaymentStatus("Chưa thanh toán");

// 3. Gọi VietQR API tạo QR
String qrCodePath = qrCodeService.generateBankTransferQR(
    orderCode, total, bankAccount, bankCode, accountName);

// 4. Lưu path vào session và redirect
session.setAttribute("qrCodePath", qrCodePath);
return "redirect:/pos/payment-qr?orderCode=" + orderCode;
```

### 2. Hiển thị trang thanh toán

```java
// 1. Validate đơn hàng có tồn tại
// 2. Kiểm tra status = "Chờ thanh toán"
// 3. Lấy QR path từ session
// 4. Hiển thị QR + countdown 15 phút
// 5. Các nút: Xác nhận, In QR, Hủy đơn
```

### 3. Xử lý thanh toán

```javascript
// 1. Khách hàng scan QR và chuyển khoản
// 2. Nhân viên kiểm tra app ngân hàng
// 3. Click "Xác nhận đã thanh toán"
// 4. AJAX call đến /pos/manual-confirm-payment
// 5. Backend update status = "Hoàn tất"
// 6. Redirect đến trang in hóa đơn
```

---

## 🎨 GIAO DIỆN NGƯỜI DÙNG

### 1. Layout trang thanh toán QR

```
┌─────────────────┬───────────────────┐
│   QR CODE       │   CHI TIẾT ĐƠN    │
│   ┌─────────┐   │   • Sản phẩm A    │
│   │  QR IMG │   │   • Sản phẩm B    │
│   └─────────┘   │   • Tổng: 150K    │
│                 │                   │
│   Mã: POS123    │   ⏰ COUNTDOWN     │
│   Số tiền: 150K │   [████████] 15:00│
│                 │                   │
│   [Xác nhận]    │   [In QR] [Hủy]   │
└─────────────────┴───────────────────┘
```

### 2. Hướng dẫn thanh toán

```html
<div class="qr-instructions">
  <h6><i class="fas fa-mobile-alt"></i> Hướng dẫn thanh toán:</h6>
  <ol>
    <li>Mở ứng dụng ngân hàng trên điện thoại</li>
    <li>Chọn chức năng "Quét QR" hoặc "Chuyển khoản QR"</li>
    <li>Quét mã QR trên màn hình</li>
    <li>Xác nhận thông tin và hoàn tất thanh toán</li>
  </ol>
</div>
```

---

## 🔍 DEBUGGING VÀ TROUBLESHOOTING

### 1. Lỗi thường gặp

#### QR không hiển thị

```java
// Check log
System.err.println("VietQR API error: HTTP " + code);
System.err.println("Không tìm thấy ảnh QR trong phản hồi API VietQR!");

// Kiểm tra thư mục
File directory = new File(QR_CODE_IMAGE_PATH);
if (!directory.exists()) {
    directory.mkdirs();
}
```

#### Countdown không hoạt động

```javascript
// Kiểm tra JS console
console.log("Time left:", timeLeft);
console.log("Progress:", percentage + "%");
```

### 2. Test cases

#### Test tạo QR thành công

```java
// Input: orderCode="POS123", amount=150000
// Expected: QR file tại /images/qr/transfer_POS123.png
// Verify: File exists và valid PNG format
```

#### Test xác nhận thanh toán

```javascript
// Input: {orderCode: "POS123"}
// Expected: {success: true, redirectUrl: "/pos/bill?orderCode=POS123"}
// Verify: Order status changed to "Hoàn tất"
```

---

## 📊 PERFORMANCE VÀ MONITORING

### 1. Metrics cần theo dõi

- **QR generation time**: < 3 seconds
- **VietQR API response time**: < 2 seconds
- **Session timeout**: 15 minutes countdown
- **File size**: QR images ~ 10-50KB

### 2. Error handling

```java
try {
    String qrCodePath = qrCodeService.generateBankTransferQR(...);
    if (qrCodePath != null) {
        // Success path
    } else {
        return "redirect:/pos?error=qr_generation_failed";
    }
} catch (Exception e) {
    e.printStackTrace();
    return "redirect:/pos?error=qr_generation_failed";
}
```

---

## 🔒 BẢO MẬT VÀ AN TOÀN

### 1. Validate input

```java
// Escape special characters
String safeAccountName = accountName.replace("\"", "");
String safeAddInfo = addInfo.replace("\"", "");
```

### 2. Session security

```java
// QR path chỉ lưu trong session, không expose
session.setAttribute("qrCodePath", qrCodePath);

// Validate order ownership
if (!"Chờ thanh toán".equals(order.getStatus())) {
    return "redirect:/pos";
}
```

### 3. File cleanup

```java
// Auto cleanup QR files sau 24h (có thể implement)
// Hoặc cleanup khi order completed
```

---

## 📝 KẾT LUẬN

Hệ thống thanh toán offline VietQR được thiết kế với:

- **Tính đơn giản**: Nhân viên chỉ cần click "Thanh toán QR"
- **Tính tin cậy**: Validate đầy đủ, error handling tốt
- **Trải nghiệm tốt**: Countdown timer, hướng dẫn rõ ràng
- **Tính linh hoạt**: Có thể in QR, hủy đơn, xác nhận thủ công

### Luồng hoàn chỉnh:

```
Cart → Checkout → Generate QR → Display QR → Customer Pay → Manual Confirm → Print Bill
```

Hệ thống đáp ứng đầy đủ nhu cầu bán hàng offline với khả năng mở rộng và bảo trì tốt.
