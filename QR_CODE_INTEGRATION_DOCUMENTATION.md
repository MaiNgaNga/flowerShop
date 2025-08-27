# HỆ THỐNG TÍCH HỢP VÀ TẠO QR CODE CHO BÁN HÀNG OFFLINE

## TỔNG QUAN

Hệ thống POS (Point of Sale) tích hợp nhiều phương thức thanh toán, đặc biệt là QR Code payment để hỗ trợ bán hàng offline. Tài liệu này mô tả chi tiết cách tích hợp và cài đặt QR code payment trong hệ thống.

## KIẾN TRÚC HỆ THỐNG

### 1. Cấu trúc thành phần chính

```
📁 QR Code Payment System
├── 🏗️ Backend Components
│   ├── QRCodeService.java              # Service tạo QR code
│   ├── PosController.java              # Controller xử lý POS
│   └── PayOSServiceImpl.java           # Service PayOS (tùy chọn)
├── 🎨 Frontend Components
│   ├── pos.html                        # Giao diện POS chính
│   ├── pos-payment-qr.html             # Trang hiển thị QR
│   ├── pos.js                          # Logic POS
│   └── pos-payment-qr.js               # Logic xử lý QR
└── 📚 Resources
    ├── static/images/qr/               # Thư mục lưu QR codes
    └── application.properties          # Cấu hình PayOS
```

## CÁC PHƯƠNG THỨC THANH TOÁN

### 1. Thanh toán tiền mặt (CASH)

- **Luồng**: Chọn sản phẩm → Checkout → Xác nhận tiền mặt → Hoàn tất
- **Trạng thái**: Trực tiếp chuyển sang "Hoàn tất"
- **In hóa đơn**: Tự động redirect đến trang in bill

### 2. Thanh toán QR chuyển khoản ngân hàng

- **API**: VietQR API
- **Luồng**: Chọn sản phẩm → Checkout QR → Tạo QR → Hiển thị → Xác nhận thủ công
- **Đặc điểm**: Sử dụng VietQR API để tạo QR chuyển khoản trực tiếp

### 3. Thanh toán QR thẻ (EMV QR)

- **Chuẩn**: EMV QR Code Standard
- **Luồng**: Tương tự QR ngân hàng nhưng sử dụng chuẩn EMV
- **Đặc điểm**: Tạo QR code theo chuẩn quốc tế EMV

## CHI TIẾT TRIỂN KHAI

### 1. QRCodeService Implementation

#### 1.1. Cấu hình cơ bản

```java
@Service
public class QRCodeService {
    private static final String QR_CODE_IMAGE_PATH = "target/classes/static/images/qr/";

    // Phương thức tạo QR code cơ bản
    public String generateQRCodeImage(String text, int width, int height, String fileName)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = FileSystems.getDefault().getPath(QR_CODE_IMAGE_PATH + fileName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        return "/images/qr/" + fileName;
    }
}
```

#### 1.2. QR Chuyển khoản ngân hàng (VietQR)

```java
public String generateBankTransferQR(String orderCode, double amount,
                                   String bankAccount, String bankCode, String accountName) {
    try {
        // Cấu hình thông tin ngân hàng
        String acqId = "970407";  // Techcombank AcqId
        String addInfo = "Thanh toan don hang " + orderCode;
        String apiUrl = "https://api.vietqr.io/v2/generate";

        // Tạo JSON request body
        String jsonBody = String.format(
            "{\"accountNo\":\"%s\",\"accountName\":\"%s\",\"acqId\":\"%s\"," +
            "\"amount\":%.0f,\"addInfo\":\"%s\",\"format\":\"png\"}",
            bankAccount, accountName, acqId, amount, addInfo
        );

        // Gọi API VietQR
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Gửi request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Đọc response và extract base64 image
        if (conn.getResponseCode() == 200) {
            // Parse JSON response để lấy base64 image
            String json = readResponse(conn);
            String base64 = extractBase64FromJson(json);

            if (base64 != null) {
                // Decode và lưu file
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                String fileName = "transfer_" + orderCode + ".png";
                Path path = Paths.get(QR_CODE_IMAGE_PATH + fileName);
                Files.write(path, imageBytes);
                return "/images/qr/" + fileName;
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
```

#### 1.3. QR Thanh toán thẻ (EMV Standard)

```java
public String generatePaymentQRCode(String orderCode, double amount, String bankAccount) {
    try {
        // Tạo nội dung QR theo chuẩn EMV
        String qrContent = String.format(
            "00020101021138570010A00000072701270006970455011%s0208QRIBFTTA5303704540%.0f5802VN6304",
            bankAccount, amount
        );

        String fileName = "payment_" + orderCode + ".png";
        return generateQRCodeImage(qrContent, 300, 300, fileName);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

### 2. PosController Implementation

#### 2.1. Checkout Process

```java
@PostMapping("/checkout")
public String checkout(@RequestParam String paymentMethod,
                      HttpSession session, Model model) {
    try {
        // Lấy giỏ hàng từ session
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/pos?error=empty_cart";
        }

        // Tạo đơn hàng
        Order order = createOrderFromCart(cart, session);

        // Xử lý theo phương thức thanh toán
        if ("qr_code".equalsIgnoreCase(paymentMethod)) {
            return handleQRCodePayment(order, session);
        } else if ("card".equalsIgnoreCase(paymentMethod)) {
            return handleCardPayment(order, session);
        } else {
            return handleCashPayment(order, session);
        }
    } catch (Exception e) {
        return "redirect:/pos?error=system_error";
    }
}
```

#### 2.2. QR Payment Handler

```java
private String handleQRCodePayment(Order order, HttpSession session) {
    try {
        // Cấu hình thông tin ngân hàng
        String bankAccount = "19039778212018";
        String bankCode = "TCB";
        String accountName = "BUI ANH THIEN";

        // Tạo thư mục QR nếu chưa có
        File qrDir = new File("target/classes/static/images/qr/");
        if (!qrDir.exists()) qrDir.mkdirs();

        // Tạo QR code
        String qrCodePath = qrCodeService.generateBankTransferQR(
            order.getOrderCode(),
            order.getTotalAmount(),
            bankAccount,
            bankCode,
            accountName
        );

        if (qrCodePath != null) {
            // Lưu thông tin vào session
            session.setAttribute("pendingOrder", order.getOrderCode());
            session.setAttribute("qrCodePath", qrCodePath);
            session.removeAttribute("cart");

            return "redirect:/pos/payment-qr?orderCode=" + order.getOrderCode();
        } else {
            return "redirect:/pos?error=qr_generation_failed";
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/pos?error=qr_generation_failed";
    }
}
```

#### 2.3. Manual Payment Confirmation

```java
@PostMapping("/manual-confirm-payment")
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
            // Cập nhật trạng thái thanh toán
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
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Lỗi: " + e.getMessage());
    }
    return response;
}
```

### 3. Frontend Implementation

#### 3.1. QR Payment Display (pos-payment-qr.html)

```html
<div class="qr-main-container">
  <div class="qr-left">
    <div class="qr-title">
      <i class="fas fa-qrcode"></i> Quét mã QR để thanh toán
    </div>

    <!-- QR Code Image -->
    <img
      th:src="${qrCodePath}"
      alt="QR Code thanh toán"
      class="qr-img"
      onerror="handleQRLoadError(this)"
    />

    <!-- Order Information -->
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

    <!-- Payment Instructions -->
    <div class="qr-instructions">
      <h6><i class="fas fa-mobile-alt"></i> Hướng dẫn thanh toán:</h6>
      <ol>
        <li>Mở ứng dụng ngân hàng trên điện thoại</li>
        <li>Chọn chức năng "Quét QR" hoặc "Chuyển khoản QR"</li>
        <li>Quét mã QR trên màn hình</li>
        <li>Xác nhận thông tin và hoàn tất thanh toán</li>
      </ol>
    </div>
  </div>

  <div class="qr-right">
    <!-- Order Details -->
    <div class="qr-summary-title">
      <i class="fas fa-cash-register"></i> Chi tiết đơn hàng
    </div>

    <!-- Order Items Table -->
    <table class="table qr-summary-table">
      <thead>
        <tr>
          <th>Sản phẩm</th>
          <th>SL</th>
          <th>Giá</th>
        </tr>
      </thead>
      <tbody>
        <tr th:each="detail : ${orderDetails}">
          <td><small th:text="${detail.product.name}"></small></td>
          <td class="text-center" th:text="${detail.quantity}"></td>
          <td class="text-end">
            <small
              th:text="${#numbers.formatDecimal(detail.price * detail.quantity, 0, 'COMMA', 0, 'POINT')}"
            ></small>
          </td>
        </tr>
      </tbody>
      <tfoot>
        <tr>
          <th colspan="2">💰 Tổng:</th>
          <th
            class="text-end"
            th:text="${#numbers.formatDecimal(totalAmount, 0, 'COMMA', 0, 'POINT')} + ' VNĐ'"
          ></th>
        </tr>
      </tfoot>
    </table>

    <!-- Countdown Timer -->
    <div class="qr-countdown">
      <span id="countdown">15:00</span>
      <div style="font-size: 0.95em; color: #888">Thời gian chờ thanh toán</div>
      <div class="progress" style="height: 8px; margin-top: 6px">
        <div
          id="progressBar"
          class="progress-bar progress-bar-striped progress-bar-animated bg-warning"
          role="progressbar"
          style="width: 100%"
        ></div>
      </div>
    </div>

    <!-- Payment Status -->
    <div id="statusAlert" class="qr-status alert alert-warning">
      <i class="fas fa-hourglass-half"></i> Đang chờ khách hàng thanh toán...
    </div>

    <!-- Action Buttons -->
    <div class="qr-actions">
      <button
        class="btn btn-success"
        id="manualConfirmBtn"
        data-bs-toggle="modal"
        data-bs-target="#confirmPaymentModal"
      >
        <i class="fas fa-check"></i> Xác nhận đã thanh toán
      </button>
      <button class="btn btn-info" id="printQRBtn">
        <i class="fas fa-print"></i> In mã QR
      </button>
      <button
        type="button"
        class="btn btn-outline-danger"
        data-bs-toggle="modal"
        data-bs-target="#cancelOrderModal"
      >
        <i class="fas fa-times"></i> Hủy & Quay lại POS
      </button>
    </div>
  </div>
</div>
```

#### 3.2. JavaScript Functionality (pos-payment-qr.js)

```javascript
// Countdown Timer
let timeLeft = 15 * 60; // 15 phút
let totalTime = 15 * 60;

function updateCountdown() {
  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  document.getElementById("countdown").textContent = `${minutes
    .toString()
    .padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;

  // Cập nhật progress bar
  const progress = (timeLeft / totalTime) * 100;
  document.getElementById("progressBar").style.width = progress + "%";

  if (timeLeft <= 0) {
    // Hết thời gian
    document.getElementById("statusAlert").innerHTML =
      '<i class="fas fa-clock"></i> Hết thời gian thanh toán!';
    document.getElementById("statusAlert").className =
      "qr-status alert alert-danger";
  }

  timeLeft--;
}

// Chạy countdown mỗi giây
setInterval(updateCountdown, 1000);

// Print QR Code
const printQRBtn = document.getElementById("printQRBtn");
if (printQRBtn) {
  printQRBtn.addEventListener("click", function () {
    const qrImage = document.querySelector('img[alt="QR Code thanh toán"]');
    const orderCode =
      document.querySelector(".info-value")?.textContent.trim() || "";
    const totalAmount =
      document.querySelector(".info-value.text-danger")?.textContent.trim() ||
      "";

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
                        .info { margin: 10px 0; font-size: 14px; }
                        .total { font-size: 18px; font-weight: bold; color: #d9534f; }
                    </style>
                </head>
                <body>
                    <div class="qr-container">
                        <h2>🌸 Cửa Hàng Hoa</h2>
                        <div class="info">Mã đơn hàng: <strong>${orderCode}</strong></div>
                        ${qrImageHtml}
                        <div class="total">Tổng tiền: ${totalAmount}</div>
                        <div class="info">Quét mã QR để thanh toán</div>
                        <div class="info"><small>Cảm ơn quý khách!</small></div>
                    </div>
                </body>
                </html>
            `);
      printWindow.document.close();
      printWindow.print();
    }

    // Convert image to base64 cho print
    if (qrImage && qrImage.src && !qrImage.src.startsWith("data:")) {
      fetch(qrImage.src)
        .then((res) => res.blob())
        .then((blob) => {
          const reader = new FileReader();
          reader.onloadend = function () {
            printQR(`<img src="${reader.result}" alt="QR Code">`);
          };
          reader.readAsDataURL(blob);
        });
    } else {
      let qrImageHtml =
        qrImage && qrImage.src
          ? `<img src="${qrImage.src}" alt="QR Code">`
          : "<div style='color:red;'>Không có mã QR</div>";
      printQR(qrImageHtml);
    }
  });
}

// Manual Payment Confirmation
const confirmPaymentModal = document.getElementById("confirmPaymentModal");
if (confirmPaymentModal) {
  confirmPaymentModal.addEventListener("show.bs.modal", function () {
    let confirmBtn = document.getElementById("confirmPaymentActionBtn");
    if (confirmBtn) {
      confirmBtn.onclick = function () {
        let orderCode = this.getAttribute("data-ordercode");
        if (!orderCode) {
          alert("Không tìm thấy mã đơn hàng!");
          return;
        }

        fetch("/pos/manual-confirm-payment", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ orderCode: orderCode }),
        })
          .then((res) => res.json())
          .then((data) => {
            if (data.success) {
              // Thành công
              document.getElementById("statusAlert").innerHTML =
                '<i class="fas fa-check-circle"></i> Đã xác nhận thanh toán thành công!';
              document.getElementById("statusAlert").className =
                "qr-status alert alert-success";

              // Ẩn modal và chuyển trang
              const modal = bootstrap.Modal.getInstance(confirmPaymentModal);
              modal.hide();

              setTimeout(() => {
                if (data.redirectUrl) {
                  window.location.href = data.redirectUrl;
                } else {
                  window.location.href = "/pos?success=payment_completed";
                }
              }, 1500);
            } else {
              alert("Lỗi: " + data.message);
            }
          })
          .catch((error) => {
            console.error("Error:", error);
            alert("Có lỗi xảy ra khi xác nhận thanh toán!");
          });
      };
    }
  });
}
```

## CẤU HÌNH VÀ THIẾT LẬP

### 1. Dependencies (pom.xml)

```xml
<!-- QR Code Generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.1</version>
</dependency>

<!-- PayOS SDK (Optional) -->
<dependency>
    <groupId>vn.payos</groupId>
    <artifactId>payos-java</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 2. Application Properties

```properties
# PayOS Configuration (nếu sử dụng)
payos.client-id=your-client-id
payos.api-key=your-api-key
payos.checksum-key=your-checksum-key

# QR Code Settings
qr.code.directory=target/classes/static/images/qr/
qr.code.size.width=300
qr.code.size.height=300

# Bank Transfer Settings
bank.account.number=19039778212018
bank.code=TCB
bank.account.name=BUI ANH THIEN
bank.acq.id=970407
```

### 3. Cấu trúc thư mục

```
src/main/resources/static/
├── images/
│   └── qr/                    # Thư mục lưu QR codes được tạo
│       ├── transfer_POS1.png  # QR chuyển khoản
│       ├── payment_POS2.png   # QR thanh toán thẻ
│       └── ...
├── css/
│   └── pos-payment-qr.css     # Styling cho QR payment
└── js/
    └── pos-payment-qr.js      # Logic xử lý QR payment
```

## WORKFLOW HOÀN CHỈNH

### 1. Luồng Thanh Toán QR Code

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Chọn sản phẩm  │ -> │   Thêm vào giỏ   │ -> │ Chọn thanh toán │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                                          │
                                                          v
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Tạo đơn hàng    │ <- │   POST /checkout │ <- │   QR Payment    │
│ Status: Chờ TT  │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
          │
          v
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Gọi VietQR API  │ -> │  Tạo QR Image    │ -> │   Lưu vào file  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
          │
          v
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Hiển thị QR    │ -> │ Khách quét & TT  │ -> │ Xác nhận thủ công│
│  + Countdown    │    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
          │
          v
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│ Cập nhật Order  │ -> │  In hóa đơn      │ -> │   Hoàn tất      │
│ Status: Hoàn tất│    │                  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### 2. Trạng thái đơn hàng

```
Trạng thái đơn hàng POS:
├── "Chờ thanh toán"     # Vừa tạo, đang hiển thị QR
├── "Hoàn tất"           # Đã thanh toán (tiền mặt hoặc xác nhận QR)
└── "Đã hủy"             # Hủy trong quá trình thanh toán QR

Trạng thái thanh toán:
├── "Chưa thanh toán"    # QR code payments
├── "Đã thanh toán"      # Sau khi xác nhận
└── "Đã hủy"             # Hủy thanh toán
```

## TÍNH NĂNG NÂNG CAO

### 1. Auto-refresh Payment Status

```javascript
// Polling để kiểm tra trạng thái thanh toán tự động
function checkPaymentStatus() {
  const orderCode = getCurrentOrderCode();

  fetch(`/pos/payment-status?orderCode=${orderCode}`)
    .then((res) => res.json())
    .then((data) => {
      if (data.status === "PAID") {
        // Tự động chuyển trang khi thanh toán thành công
        window.location.href = `/pos/bill?orderCode=${orderCode}`;
      }
    })
    .catch(console.error);
}

// Kiểm tra mỗi 10 giây
setInterval(checkPaymentStatus, 10000);
```

### 2. QR Code Error Handling

```javascript
function handleQRLoadError(img) {
  // Hiển thị fallback khi QR load lỗi
  img.style.display = "none";
  const fallback = document.getElementById("qr-fallback");
  if (fallback) {
    fallback.style.display = "block";
  }

  // Log lỗi để debug
  console.error("QR Code load failed:", img.src);
}
```

### 3. Print Functionality Enhancement

```javascript
function enhancedPrintQR() {
  // Tạo template in đẹp hơn với logo, thông tin shop
  const printContent = `
        <div class="receipt">
            <div class="header">
                <img src="/images/logo.png" alt="Logo">
                <h1>FLOWER SHOP</h1>
                <p>Địa chỉ: 123 Đường ABC, Quận XYZ</p>
                <p>Hotline: 0123.456.789</p>
            </div>
            <div class="qr-section">
                <h2>THANH TOÁN QR CODE</h2>
                <img src="${qrCodeSrc}" alt="QR Payment">
                <p>Mã đơn: ${orderCode}</p>
                <p>Tổng tiền: ${totalAmount}</p>
            </div>
            <div class="footer">
                <p>Cảm ơn quý khách!</p>
                <p>Thời gian: ${new Date().toLocaleString()}</p>
            </div>
        </div>
    `;

  // Mở cửa sổ in với CSS được tối ưu
  const printWindow = window.open("", "_blank");
  printWindow.document.write(getPrintTemplate(printContent));
  printWindow.print();
}
```

## BẢO MẬT VÀ TỐI ƯU

### 1. Bảo mật thông tin

- **Mã hóa thông tin nhạy cảm**: Sử dụng environment variables cho API keys
- **Validate input**: Kiểm tra tất cả input từ frontend
- **Rate limiting**: Giới hạn số lần tạo QR code per session
- **HTTPS**: Đảm bảo all communications qua HTTPS

### 2. Tối ưu performance

- **QR Code caching**: Cache QR codes đã tạo
- **Image optimization**: Tối ưu kích thước QR images
- **Session management**: Clean up expired sessions
- **Database indexing**: Index trên orderCode, status

### 3. Error Handling Best Practices

```java
@ExceptionHandler(Exception.class)
public String handleQRGenerationError(Exception e, Model model) {
    log.error("QR Generation Error: ", e);
    model.addAttribute("errorMessage", "Không thể tạo mã QR. Vui lòng thử lại.");
    return "redirect:/pos?error=qr_generation_failed";
}
```

## TESTING VÀ DEBUGGING

### 1. Test Cases chính

- ✅ Tạo QR code thành công
- ✅ Xử lý lỗi API VietQR
- ✅ Timeout countdown
- ✅ Manual confirmation
- ✅ Print functionality
- ✅ Order cancellation
- ✅ Session management

### 2. Debug Tools

```javascript
// Debug mode cho development
const DEBUG_MODE = true;

if (DEBUG_MODE) {
  console.log("QR Code Path:", qrCodePath);
  console.log("Order Code:", orderCode);
  console.log("Total Amount:", totalAmount);

  // Thêm debug buttons
  addDebugButtons();
}

function addDebugButtons() {
  const debugPanel = document.createElement("div");
  debugPanel.innerHTML = `
        <div style="position: fixed; top: 10px; right: 10px; background: #fff; padding: 10px; border: 1px solid #ccc;">
            <h5>Debug Panel</h5>
            <button onclick="simulatePaymentSuccess()">Simulate Payment Success</button>
            <button onclick="simulateTimeout()">Simulate Timeout</button>
            <button onclick="showQRInfo()">Show QR Info</button>
        </div>
    `;
  document.body.appendChild(debugPanel);
}
```

## KẾT LUẬN

Hệ thống QR Code Payment đã được tích hợp hoàn chỉnh với các tính năng:

### ✅ Đã hoàn thành

- Tạo QR chuyển khoản ngân hàng (VietQR API)
- Tạo QR thanh toán thẻ (EMV standard)
- Giao diện hiển thị QR đẹp và responsive
- Countdown timer và progress bar
- Xác nhận thanh toán thủ công
- Print QR code functionality
- Error handling và fallback
- Session management

### 🔄 Có thể mở rộng

- Webhook payment verification
- Real-time payment status updates
- Multiple payment gateways integration
- Advanced analytics và reporting
- Mobile app integration
- Inventory real-time updates

### 📋 Maintenance Notes

- Định kỳ kiểm tra VietQR API status
- Monitor QR code file storage
- Clean up expired order sessions
- Update bank information nếu cần
- Performance monitoring và optimization

---

**Tài liệu này cung cấp đầy đủ thông tin để hiểu, triển khai và maintenance hệ thống QR Code Payment trong ứng dụng POS. Để biết thêm chi tiết về từng component, vui lòng tham khảo source code tương ứng.**
