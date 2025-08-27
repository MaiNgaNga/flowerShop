# CƠ CHẾ THÊM VÀ LƯU SẢN PHẨM VÀO GIỎ HÀNG TRONG BÁN HÀNG OFFLINE (POS)

## 1. TỔNG QUAN VỀ HỆ THỐNG POS

### 1.1 Khái niệm và đặc điểm

- **POS (Point of Sale)**: Hệ thống bán hàng tại quầy
- **Offline Cart**: Giỏ hàng lưu trên **Session** thay vì Database
- **Real-time Updates**: Cập nhật tức thì qua AJAX
- **Session-based**: Không cần đăng nhập để sử dụng cart

### 1.2 Kiến trúc hệ thống

```
Frontend (HTML/JS) → AJAX Calls → Backend Controller → Session Storage
       ↓                ↓              ↓                    ↓
   pos.html         pos.js      PosController           HttpSession
```

### 1.3 Sơ đồ luồng nghiệp vụ - Thêm sản phẩm vào giỏ hàng

```
┌─────────────────────────────────────────────────────────────────────┐
│                    LUỒNG THÊM SẢN PHẨM VÀO GIỎ HÀNG                 │
└─────────────────────────────────────────────────────────────────────┘

1. NGƯỜI DÙNG TƯƠNG TÁC
   ┌─────────────────┐
   │ Nhân viên click │
   │ nút "Chọn" trên │ ───┐
   │ product card    │    │
   └─────────────────┘    │
                          │
2. FRONTEND XỬ LÝ          │
   ┌─────────────────┐    │
   │ JavaScript lấy  │◄───┘
   │ productId từ    │
   │ data-id attr    │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ Gửi AJAX POST   │
   │ request đến     │
   │ /pos/cart/add   │
   └─────────────────┘
           │
           ▼
3. BACKEND XỬ LÝ
   ┌─────────────────┐
   │ PosController   │
   │ nhận request    │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ Lấy session     │     │ Session rỗng?   │
   │ cart hiện tại   │────▶│ Tạo ArrayList   │
   └─────────────────┘     │ mới             │
           │               └─────────────────┘
           ▼
   ┌─────────────────┐
   │ Kiểm tra sản    │     ┌─────────────────┐
   │ phẩm đã có      │────▶│ SẢN PHẨM CÓ SẴN │
   │ trong cart?     │     │ quantity += 1   │
   └─────────────────┘     └─────────────────┘
           │                        │
           ▼ (Sản phẩm mới)         │
   ┌─────────────────┐              │
   │ Tìm Product     │              │
   │ trong DB theo   │              │
   │ productId       │              │
   └─────────────────┘              │
           │                        │
           ▼                        │
   ┌─────────────────┐              │
   │ Kiểm tra giảm   │              │
   │ giá có hiệu lực │              │
   │ không?          │              │
   └─────────────────┘              │
           │                        │
           ▼                        │
   ┌─────────────────┐     ┌─────────────────┐
   │ CÓ GIẢM GIÁ:    │     │ KHÔNG GIẢM GIÁ: │
   │ price =         │     │ price =         │
   │ priceAfterDisc  │     │ originalPrice   │
   └─────────────────┘     └─────────────────┘
           │                        │
           ▼                        │
   ┌─────────────────┐              │
   │ Tạo CartItemDTO │              │
   │ - productId     │              │
   │ - name          │              │
   │ - price         │              │
   │ - quantity = 1  │              │
   └─────────────────┘              │
           │                        │
           ▼                        │
   ┌─────────────────┐              │
   │ Thêm item vào   │              │
   │ cart List       │              │
   └─────────────────┘              │
           │                        │
           ▼◄───────────────────────┘
   ┌─────────────────┐
   │ Lưu cart vào    │
   │ session với     │
   │ key = "cart"    │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ Trả về JSON     │
   │ cart đầy đủ     │
   │ cho frontend    │
   └─────────────────┘
           │
           ▼
4. FRONTEND CẬP NHẬT
   ┌─────────────────┐
   │ Nhận JSON       │
   │ response từ     │
   │ backend         │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ renderCart()    │
   │ - Xóa nội dung  │
   │   bảng cũ       │
   │ - Tạo rows mới  │
   │ - Cập nhật tổng │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ Hiển thị thông  │
   │ báo thành công  │
   │ cho người dùng  │
   └─────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                            LƯU Ý QUAN TRỌNG                        │
├─────────────────────────────────────────────────────────────────────┤
│ • Session timeout: 30 phút mặc định                                 │
│ • Giỏ hàng bị mất khi session hết hạn                              │
│ • Mỗi terminal có session riêng biệt                               │
│ • Kiểm tra giảm giá theo thời gian thực                            │
│ • Xử lý lỗi với thông báo thân thiện                               │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.4 Sơ đồ xử lý lỗi và ngoại lệ

```
┌─────────────────────────────────────────────────────────────────────┐
│                      XỬ LÝ LỖI VÀ NGOẠI LỆ                         │
└─────────────────────────────────────────────────────────────────────┘

ĐIỂM KIỂM TRA LỖI                    HÀNH ĐỘNG XỬ LÝ
┌─────────────────┐                  ┌─────────────────┐
│ 1. ProductId    │                  │ • Trả về HTTP   │
│    null/empty?  │─────YES─────────▶│   400 Bad Req   │
└─────────────────┘                  │ • Thông báo lỗi │
         │                           └─────────────────┘
         NO
         ▼
┌─────────────────┐                  ┌─────────────────┐
│ 2. Product tồn  │                  │ • Log lỗi       │
│    tại trong DB?│─────NO──────────▶│ • Thông báo     │
└─────────────────┘                  │   "Sản phẩm     │
         │                           │   không tồn tại"│
         YES                         └─────────────────┘
         ▼
┌─────────────────┐                  ┌─────────────────┐
│ 3. Product có   │                  │ • Thông báo     │
│    available =  │─────NO──────────▶│   "Sản phẩm     │
│    true?        │                  │   ngừng bán"    │
└─────────────────┘                  └─────────────────┘
         │
         YES
         ▼
┌─────────────────┐                  ┌─────────────────┐
│ 4. Session có   │                  │ • Redirect      │
│    user login?  │─────NO──────────▶│   trang login   │
└─────────────────┘                  │ • Clear session │
         │                           └─────────────────┘
         YES
         ▼
┌─────────────────┐                  ┌─────────────────┐
│ 5. Quantity >   │                  │ • Thông báo     │
│    MAX_LIMIT?   │─────YES─────────▶│   "Vượt quá    │
└─────────────────┘                  │   giới hạn"     │
         │                           └─────────────────┘
         NO
         ▼
┌─────────────────┐                  ┌─────────────────┐
│ 6. Network/     │                  │ • Retry logic   │
│    Server Error?│─────YES─────────▶│ • Fallback UI   │
└─────────────────┘                  │ • Error toast   │
         │                           └─────────────────┘
         NO
         ▼
    ✅ SUCCESS

┌─────────────────────────────────────────────────────────────────────┐
│                    CHIẾN LƯỢC XỬ LÝ LỖI FRONTEND                    │
├─────────────────────────────────────────────────────────────────────┤
│ try {                                                               │
│   // AJAX call                                                     │
│ } catch (networkError) {                                           │
│   showAlert("Lỗi kết nối. Vui lòng thử lại!", "error");          │
│ } catch (serverError) {                                            │
│   showAlert("Hệ thống bận. Vui lòng đợi!", "warning");           │
│ } catch (validationError) {                                        │
│   showAlert("Dữ liệu không hợp lệ!", "error");                   │
│ }                                                                   │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.5 So sánh luồng: Sản phẩm mới vs Tăng số lượng

```
                    CLICK BUTTON "CHỌN"
                            │
                            ▼
                   ┌─────────────────┐
                   │ Kiểm tra sản    │
                   │ phẩm trong cart │
                   └─────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
               ▼                        ▼
    ┌─────────────────┐         ┌─────────────────┐
    │ SẢN PHẨM MỚI    │         │ SẢN PHẨM CÓ SẴN │
    │ (Chưa có)       │         │ (Đã có)         │
    └─────────────────┘         └─────────────────┘
              │                           │
              ▼                           ▼
    ┌─────────────────┐         ┌─────────────────┐
    │ 1. Tìm Product  │         │ 1. Lấy item     │
    │    trong DB     │         │    hiện tại     │
    │ 2. Kiểm tra     │         │ 2. Kiểm tra     │
    │    giảm giá     │         │    quantity     │
    │ 3. Tính giá     │         │    < MAX_LIMIT  │
    │    cuối cùng    │         │ 3. quantity++   │
    │ 4. Tạo DTO mới  │         │                 │
    │ 5. Add vào List │         │                 │
    └─────────────────┘         └─────────────────┘
              │                           │
              └───────────┬───────────────┘
                          │
                          ▼
                ┌─────────────────┐
                │ Lưu cart vào    │
                │ session         │
                └─────────────────┘
                          │
                          ▼
                ┌─────────────────┐
                │ Trả về JSON     │
                │ cart complete   │
                └─────────────────┘
                          │
                          ▼
                ┌─────────────────┐
                │ Update UI       │
                │ renderCart()    │
                └─────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         HIỆU SUẤT SO SÁNH                          │
├─────────────────────────────────────────────────────────────────────┤
│                    │ SẢN PHẨM MỚI  │ TĂNG SỐ LƯỢNG │              │
│ Database Query     │     1 call    │    0 call      │              │
│ Memory Allocation  │     Cao       │    Thấp        │              │
│ Processing Time    │     ~50ms     │    ~5ms        │              │
│ Network Payload    │     Đầy đủ    │    Nhỏ gọn     │              │
│ UI Re-render       │     Full      │    Partial     │              │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.6 Tại sao dùng HttpSession thay vì SessionStorage/LocalStorage?

```
┌─────────────────────────────────────────────────────────────────────┐
│              HTTP SESSION vs BROWSER STORAGE SO SÁNH                │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────┐                    ┌─────────────────┐
│  HTTP SESSION   │                    │ SESSION STORAGE │
│  (Server-side)  │                    │ (Browser-side)  │
├─────────────────┤                    ├─────────────────┤
│ 🖥️  SERVER       │                    │ 🌐 BROWSER       │
│                 │                    │                 │
│ ┌─────────────┐ │                    │ ┌─────────────┐ │
│ │   Memory    │ │                    │ │ Browser Tab │ │
│ │             │ │                    │ │             │ │
│ │ Session:    │ │                    │ │ sessionS-   │ │
│ │ {           │ │                    │ │ torage:     │ │
│ │   cart: [   │ │                    │ │ {           │ │
│ │     {id:1}  │ │                    │ │   cart: [   │ │
│ │   ]         │ │                    │ │     {id:1}  │ │
│ │ }           │ │                    │ │   ]         │ │
│ └─────────────┘ │                    │ │ }           │ │
└─────────────────┘                    │ └─────────────┘ │
                                       └─────────────────┘

TẠI SAO KHÔNG DÙNG SESSION STORAGE CHO POS?

❌ VẤN ĐỀ BẢO MẬT:
┌─────────────────────────────────────────────────────────────────────┐
│ Với SessionStorage (Browser):                                       │
│ • Khách hàng có thể mở F12 Developer Tools                         │
│ • Xem được toàn bộ dữ liệu giỏ hàng                                │
│ • Sửa đổi giá, quantity ngay trên browser                          │
│ • Bypass validation của server                                      │
│                                                                     │
│ VÍ DỤ: sessionStorage.setItem('cart',                              │
│        '[{"id":1,"price":1000000,"quantity":999}]')               │
│ → Khách có thể tự set giá 1 triệu cho 999 sản phẩm!              │
└─────────────────────────────────────────────────────────────────────┘

✅ ƯU ĐIỂM HTTP SESSION:
┌─────────────────────────────────────────────────────────────────────┐
│ 1. BẢO MẬT TUYỆT ĐỐI:                                              │
│    • Dữ liệu lưu trên server, không thể truy cập từ browser        │
│    • Không thể inspect, modify bằng DevTools                       │
│    • Mọi thao tác phải qua server validation                       │
│                                                                     │
│ 2. TÍNH TOÀN VẸN DỮ LIỆU:                                          │
│    • Giá được tính toán realtime từ database                       │
│    • Discount validation theo thời gian server                     │
│    • Không thể fake data từ client                                 │
│                                                                     │
│ 3. KIỂM SOÁT TRUY CẬP:                                              │
│    • Chỉ nhân viên đăng nhập mới dùng được                         │
│    • Session timeout tự động (30 phút)                             │
│    • Mỗi terminal có session riêng biệt                            │
└─────────────────────────────────────────────────────────────────────┘

LUỒNG DỮ LIỆU AN TOÀN:

Browser                    Server
┌─────────┐    AJAX       ┌─────────┐
│ UI Only │──────────────▶│ Session │
│         │               │ Storage │
│ JSON    │◄──────────────│         │
│ Display │   Response    │ Secured │
└─────────┘               └─────────┘

• Browser chỉ nhận JSON để hiển thị
• Không bao giờ lưu trữ sensitive data
• Mọi logic business ở server

TẠI SAO KHÔNG THỂ XEM TRONG BROWSER DEVTOOLS?

F12 → Application Tab → Session Storage:
┌─────────────────────────────────────────────────────────────────────┐
│ 🔍 KẾT QUẢ: EMPTY hoặc chỉ có UI state                             │
│                                                                     │
│ sessionStorage: {                                                   │
│   // Không có dữ liệu cart                                         │
│   // Chỉ có theme, language settings                               │
│ }                                                                   │
│                                                                     │
│ localStorage: {                                                     │
│   // Cũng không có cart data                                       │
│ }                                                                   │
│                                                                     │
│ Cookies: {                                                          │
│   JSESSIONID: "A1B2C3D4E5F6..."  // Chỉ có session ID             │
│   // Không có cart content                                         │
│ }                                                                   │
└─────────────────────────────────────────────────────────────────────┘

CÁCH XEM DỮ LIỆU CART TRONG DEVELOPMENT:

1. BACKEND LOGGING:
   @PostMapping("/cart/add")
   public List<CartItemDTO> addToCart(..., HttpSession session) {
       List<CartItemDTO> cart = getCart(session);
       System.out.println("DEBUG Cart: " + cart);  // Console log
       return cart;
   }

2. BROWSER NETWORK TAB:
   F12 → Network → XHR → Click /pos/cart/add → Response Preview

3. DATABASE SESSION TABLE (nếu dùng spring-session-jdbc):
   SELECT * FROM SPRING_SESSION_ATTRIBUTES;

4. DEBUG ENDPOINT (chỉ development):
   @GetMapping("/debug/cart")
   public List<CartItemDTO> debugCart(HttpSession session) {
       return (List<CartItemDTO>) session.getAttribute("cart");
   }
```

## 2. PHÂN TÍCH FRONTEND - GIAO DIỆN NGƯỜI DÙNG

### 2.1 Cấu trúc pos.html

#### A. Layout chính

```html
<div class="pos-center">
  <div class="pos-flex">
    <!-- LEFT SIDE: Product Display & Filters -->
    <div class="pos-left">
      <div class="pos-category"><!-- Bộ lọc sản phẩm --></div>
      <div class="pos-product-list"><!-- Danh sách sản phẩm --></div>
    </div>

    <!-- RIGHT SIDE: Shopping Cart & Checkout -->
    <div class="pos-right">
      <h2>Đơn hàng</h2>
      <form id="posForm"><!-- Form thanh toán --></form>
    </div>
  </div>
</div>
```

#### B. Product Card Template

```html
<div th:each="product : ${products}" class="pos-card">
  <div style="position: relative">
    <!-- Product Image with Discount Badge -->
    <img th:src="@{'/images/' + ${product.image_url}}" class="pos-card-img" />
    <span th:if="${product.isDiscountActive()}" class="promo-tag">
      -<span th:text="${product.discountPercent}"></span>%
    </span>
  </div>

  <!-- Product Info -->
  <div class="pos-card-title" th:text="${product.name}"></div>

  <!-- Dynamic Pricing Logic -->
  <div
    class="pos-card-price"
    th:if="${product.discountPercent != null && product.discountPercent > 0 && 
               product.discountStart != null && product.discountEnd != null && 
               !T(java.time.LocalDate).now().isBefore(product.discountStart) && 
               !T(java.time.LocalDate).now().isAfter(product.discountEnd)}"
  >
    <span
      style="color: #e53935; font-weight: bold"
      th:text="${#numbers.formatDecimal(product.priceAfterDiscount, 0, 'COMMA', 0, 'POINT')} + ' VND'"
    ></span>
    <span
      style="text-decoration: line-through; color: #888;"
      th:text="${#numbers.formatDecimal(product.price, 0, 'COMMA', 0, 'POINT')} + ' VND'"
    ></span>
  </div>

  <!-- Add to Cart Button -->
  <button
    class="pos-card-btn"
    type="button"
    th:attr="data-id=${product.id}"
    onclick="addToCart(this)"
  >
    Chọn
  </button>
</div>
```

**Tính năng chính:**

- **Liên kết dữ liệu Thymeleaf**: `th:attr="data-id=${product.id}"` - Gắn ID sản phẩm vào button
- **Logic giảm giá phức tạp**: Kiểm tra nhiều điều kiện để áp dụng giảm giá
- **Thiết kế responsive**: Sử dụng các lớp Bootstrap để tương thích mọi thiết bị
- **Xử lý sự kiện**: `onclick="addToCart(this)"` - Gọi hàm thêm vào giỏ khi click

#### C. Shopping Cart Table

```html
<table class="table table-bordered pos-cart-table" id="cartTable">
  <thead>
    <tr>
      <th>Sản phẩm</th>
      <th>Số lượng</th>
      <th>Thành tiền</th>
      <th></th>
    </tr>
  </thead>
  <tbody id="cartRows">
    <!-- Dynamic content generated by JavaScript -->
  </tbody>
</table>

<div class="mb-3">
  <label class="form-label">Tổng tiền:</label>
  <span id="totalAmount">0</span> VND
</div>
```

**Tạo nội dung động:**

- `id="cartRows"`: Điểm chèn nội dung bằng JavaScript
- `id="totalAmount"`: Tính toán tổng tiền theo thời gian thực
- **Trạng thái rỗng**: Hiển thị "0 VND" khi giỏ hàng trống

## 3. PHÂN TÍCH JAVASCRIPT - FRONTEND LOGIC

### 3.1 Function addToCart() - Core Addition Logic

```javascript
function addToCart(btn) {
  const productId = btn.getAttribute("data-id");

  fetch("/pos/cart/add", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: "productId=" + productId,
  })
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        showCustomAlert("Có lỗi khi thêm vào giỏ hàng!", "error");
        console.error(cart);
      }
    })
    .catch((err) => {
      showCustomAlert("Có lỗi khi thêm vào giỏ hàng: " + err.message, "error");
      console.error(err);
    });
}
```

**Giải thích luồng xử lý:**

1. **Lấy ID sản phẩm**: `btn.getAttribute("data-id")` - Lấy ID từ nút được nhấn
2. **Gửi yêu cầu AJAX POST**: Gửi đến endpoint `/pos/cart/add`
3. **Xử lý phản hồi**: Phân tích dữ liệu JSON trả về
4. **Cập nhật giao diện**: Gọi `renderCart()` để làm mới hiển thị
5. **Xử lý lỗi**: Hiển thị thông báo lỗi thân thiện với người dùng

### 3.2 Function renderCart() - UI Rendering

```javascript
function renderCart(cart) {
  const cartRows = document.getElementById("cartRows");
  cartRows.innerHTML = "";
  let total = 0;

  if (Array.isArray(cart)) {
    cart.forEach((item) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${item.name}</td>
        <td>${item.quantity}</td>
        <td>${(item.price * item.quantity).toLocaleString()} VND</td>
        <td>
          <button type="button" onclick="removeFromCart(${item.productId})" 
                  class="btn btn-link p-0" title="Xóa">
            <span style="color: red; font-size: 18px;">&#10006;</span>
          </button>
        </td>
      `;
      cartRows.appendChild(row);
      total += item.price * item.quantity;
    });
  } else {
    console.error("cart không phải là mảng:", cart);
  }

  document.getElementById("totalAmount").innerText = total.toLocaleString();
}
```

**Các thao tác chính:**

- **Xóa nội dung hiện tại**: `cartRows.innerHTML = ""` - Làm sạch bảng
- **Tạo dòng động**: `createElement("tr")` - Tạo mới các dòng trong bảng
- **Định dạng giá**: `toLocaleString()` - Hiển thị số theo định dạng Việt Nam
- **Nút xóa**: Inline onclick với tham số ID sản phẩm
- **Tính tổng tiền**: Tính tổng theo thời gian thực cho tất cả sản phẩm

### 3.3 Function removeFromCart() - Item Removal

```javascript
function removeFromCart(productId) {
  fetch("/pos/cart/remove", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: "productId=" + productId,
  })
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        showCustomAlert("Có lỗi khi xóa sản phẩm!", "error");
        console.error(cart);
      }
    })
    .catch((err) => {
      showCustomAlert("Có lỗi khi xóa sản phẩm: " + err.message, "error");
      console.error(err);
    });
}
```

**Logic xử lý xóa:**

- **Giảm số lượng**: Nếu số lượng > 1, giảm đi 1
- **Xóa hoàn toàn**: Nếu số lượng = 1, xóa sản phẩm khỏi giỏ hàng
- **Tính nhất quán AJAX**: Cùng mẫu xử lý với addToCart()

### 3.4 Cart Initialization on Page Load

```javascript
window.onload = function () {
  // Load existing cart from session
  fetch("/pos/cart")
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        console.error("cart không phải là mảng:", cart);
      }
    })
    .catch((err) => {
      console.error("Lỗi khi lấy giỏ hàng:", err);
    });
};
```

**Mục đích khởi tạo:**

- **Lưu trữ bền vững session**: Khôi phục giỏ hàng nếu trang được tải lại
- **Trạng thái nhất quán**: Đảm bảo giao diện khớp với dữ liệu backend
- **Khả năng chống lỗi**: Xử lý lỗi kết nối mạng một cách uyển chuyển

## 4. PHÂN TÍCH BACKEND - POSCONTROLLER

### 4.1 Endpoint: POST /pos/cart/add

```java
@PostMapping("/cart/add")
@ResponseBody
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
    // 1. GET EXISTING CART FROM SESSION
    List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
    if (cart == null) cart = new ArrayList<>();

    // 2. CHECK IF PRODUCT ALREADY EXISTS
    Optional<CartItemDTO> existing = cart.stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst();

    if (existing.isPresent()) {
        // 3A. INCREASE QUANTITY IF EXISTS
        existing.get().setQuantity(existing.get().getQuantity() + 1);
    } else {
        // 3B. CREATE NEW CART ITEM
        Product product = productService.findByID(productId);
        CartItemDTO item = new CartItemDTO();
        item.setProductId(productId);
        item.setName(product.getName());

        // 4. HANDLE DISCOUNT PRICING
        double price = product.getPrice();
        Double priceAfterDiscount = null;
        java.time.LocalDate today = java.time.LocalDate.now();

        if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0
                && product.getDiscountStart() != null && product.getDiscountEnd() != null
                && !today.isBefore(product.getDiscountStart())
                && !today.isAfter(product.getDiscountEnd())) {
            priceAfterDiscount = product.getPriceAfterDiscount();
            price = priceAfterDiscount;
        }

        item.setPrice(price);
        item.setPriceAfterDiscount(priceAfterDiscount);
        item.setQuantity(1);
        cart.add(item);
    }

    // 5. SAVE BACK TO SESSION
    session.setAttribute("cart", cart);
    return cart;
}
```

**Phân tích chi tiết logic:**

#### A. Quản lý Session

```java
@SuppressWarnings("unchecked")
List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
if (cart == null) cart = new ArrayList<>();
```

- **Ép kiểu**: Cần thiết cho các collection generic từ session
- **An toàn null**: Khởi tạo giỏ hàng rỗng nếu chưa tồn tại
- **Thuộc tính session**: Key = "cart", Value = List<CartItemDTO>

#### B. Xử lý trùng lặp

```java
Optional<CartItemDTO> existing = cart.stream()
        .filter(i -> i.getProductId().equals(productId))
        .findFirst();
```

- **Stream API**: Cách tiếp cận lập trình hàm
- **Lọc theo ID**: Tìm sản phẩm đã tồn tại trong giỏ hàng
- **Mẫu Optional**: Xử lý null an toàn

#### C. Quản lý số lượng

```java
if (existing.isPresent()) {
    existing.get().setQuantity(existing.get().getQuantity() + 1);
} else {
    // Tạo sản phẩm mới...
}
```

- **Logic tăng**: +1 cho sản phẩm đã có
- **Logic sản phẩm mới**: Lấy toàn bộ dữ liệu sản phẩm

#### D. Logic tính giá động

```java
double price = product.getPrice();
Double priceAfterDiscount = null;
java.time.LocalDate today = java.time.LocalDate.now();

if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0
        && product.getDiscountStart() != null && product.getDiscountEnd() != null
        && !today.isBefore(product.getDiscountStart())
        && !today.isAfter(product.getDiscountEnd())) {
    priceAfterDiscount = product.getPriceAfterDiscount();
    price = priceAfterDiscount;
}
```

**Quy tắc kiểm tra giảm giá:**

1. `discountPercent != null && discountPercent > 0`: Có % giảm giá hợp lệ
2. `discountStart != null && discountEnd != null`: Có khoảng thời gian hợp lệ
3. `!today.isBefore(discountStart)`: Chương trình giảm giá đã bắt đầu
4. `!today.isAfter(discountEnd)`: Chương trình giảm giá chưa hết hạn
5. **Kết quả**: Sử dụng giá giảm nếu thỏa mãn tất cả điều kiện

### 4.2 Endpoint: GET /pos/cart

```java
@GetMapping("/cart")
@ResponseBody
public List<CartItemDTO> getCart(HttpSession session) {
    List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
    if (cart == null) cart = new ArrayList<>();
    return cart;
}
```

**Truy xuất đơn giản:**

- **Truy cập session trực tiếp**: Không cần logic phức tạp
- **An toàn null**: Trả về danh sách rỗng nếu không có giỏ hàng
- **Tuần tự hóa JSON**: Spring tự động chuyển đổi sang JSON

### 4.3 Endpoint: POST /pos/cart/remove

```java
@PostMapping("/cart/remove")
@ResponseBody
public List<CartItemDTO> removeFromCart(@RequestParam Long productId, HttpSession session) {
    List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
    if (cart == null) cart = new ArrayList<>();

    for (int i = 0; i < cart.size(); i++) {
        CartItemDTO item = cart.get(i);
        if (item.getProductId().equals(productId)) {
            if (item.getQuantity() > 1) {
                // DECREASE QUANTITY
                item.setQuantity(item.getQuantity() - 1);
            } else {
                // REMOVE COMPLETELY
                cart.remove(i);
            }
            break; // Exit loop after first match
        }
    }

    session.setAttribute("cart", cart);
    return cart;
}
```

**Chiến lược xóa:**

- **Vòng lặp dựa trên chỉ số**: An toàn khi xóa trong quá trình lặp
- **Kiểm tra số lượng**: Giảm so với xóa hoàn toàn
- **Thoát sớm**: `break` sau khi tìm thấy để tối ưu hiệu suất

## 5. DATA TRANSFER OBJECT - CARTITEMDTO

### 5.1 Class Structure

```java
public class CartItemDTO {
    private Long productId;           // Unique product identifier
    private String name;              // Product display name
    private double price;             // Final price (after discount if applicable)
    private int quantity;             // Number of items
    private Double priceAfterDiscount; // Original discounted price (nullable)

    // Getters and Setters...
}
```

### 5.2 Giải thích các trường

#### A. productId (Long)

- **Mục đích**: Định danh duy nhất để tìm kiếm trong database
- **Sử dụng**: Thao tác giỏ hàng, tạo chi tiết đơn hàng
- **Kiểu dữ liệu**: Long để khớp với ID của Product entity

#### B. name (String)

- **Mục đích**: Tên hiển thị trong giao diện giỏ hàng
- **Nguồn**: Sao chép từ Product.name
- **Lợi ích**: Tránh việc truy vấn database lặp lại

#### C. price (double)

- **Mục đích**: Giá cuối cùng đã tính toán cho mỗi đơn vị
- **Logic**: Giá gốc HOẶC giá sau giảm
- **Đơn vị tiền tệ**: Đồng Việt Nam (VND)

#### D. quantity (int)

- **Mục đích**: Số lượng sản phẩm trong giỏ hàng
- **Phạm vi**: Từ 1 đến Integer.MAX_VALUE
- **Thao tác**: Tăng, giảm, đặt giá trị

#### E. priceAfterDiscount (Double)

- **Mục đích**: Lưu trữ thông tin giảm giá gốc
- **Có thể null**: null nếu không áp dụng giảm giá
- **Sử dụng**: So sánh giá, phân tích kinh doanh

### 5.3 Lợi ích của DTO

#### A. Tối ưu hóa hiệu suất

- **Giảm DB calls**: Lưu trữ dữ liệu được truy cập thường xuyên
- **Hiệu quả bộ nhớ**: Chỉ các trường cần thiết
- **Tuần tự hóa session**: Các đối tượng nhẹ

#### B. Tách biệt

- **Độc lập entity**: Thay đổi Product không ảnh hưởng giỏ hàng
- **Kiểm soát phiên bản**: Giỏ hàng vẫn hợp lệ qua các lần triển khai
- **Bảo mật**: Không có dữ liệu nhạy cảm của entity trong session

## 6. SESSION MANAGEMENT STRATEGY

### 6.1 HttpSession trong Spring

```java
@PostMapping("/cart/add")
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
    // Session automatically injected by Spring
    List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");

    // Modify cart...

    session.setAttribute("cart", cart);
    return cart;
}
```

**Tính năng Spring Session:**

- **Tự động inject**: Tham số HttpSession
- **Thread-safe**: Spring xử lý đồng thời
- **Timeout có thể cấu hình**: Mặc định 30 phút
- **Dựa trên cookie**: JSESSIONID để tracking

### 6.2 Session Storage Model

```
User Browser ←→ Server Session Store
     |                    |
  JSESSIONID         HttpSession
                          |
                   ┌─────────────┐
                   │ Attributes  │
                   │ - cart      │
                   │ - user      │
                   │ - ...       │
                   └─────────────┘
```

### 6.3 Chu kỳ sống của Session

#### A. Tạo Session

```java
// Tự động tạo khi có yêu cầu đầu tiên sử dụng session
HttpSession session = request.getSession(true);
```

#### B. Khởi tạo giỏ hàng

```java
List<CartItemDTO> cart = new ArrayList<>();
session.setAttribute("cart", cart);
```

#### C. Lưu trữ Session

- **Lưu trữ bộ nhớ**: Mặc định lưu trong bộ nhớ
- **Có thể cấu hình**: Có thể sử dụng Redis, Database, v.v.
- **Dọn dẹp tự động**: Các session hết hạn được xóa

#### D. Vô hiệu hóa Session

```java
// Khi hoàn tất thanh toán
session.removeAttribute("cart");

// Khi đăng xuất
session.invalidate();
```

## 7. ERROR HANDLING VÀ VALIDATION

### 7.1 Frontend Error Handling

```javascript
function addToCart(btn) {
  fetch("/pos/cart/add", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: "productId=" + productId,
  })
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        showCustomAlert("Có lỗi khi thêm vào giỏ hàng!", "error");
        console.error(cart);
      }
    })
    .catch((err) => {
      showCustomAlert("Có lỗi khi thêm vào giỏ hàng: " + err.message, "error");
      console.error(err);
    });
}
```

**Các lớp xử lý lỗi:**

1. **Trạng thái HTTP**: Kiểm tra `res.ok`
2. **Định dạng phản hồi**: Kiểm tra cấu trúc JSON
3. **Kiểu dữ liệu**: Đảm bảo cart là mảng
4. **Phản hồi người dùng**: Thông báo `showCustomAlert()`
5. **Console Logging**: Thông tin debug

### 7.2 Custom Alert System

```javascript
function showCustomAlert(message, type = "success") {
  // Remove existing alerts
  document.querySelectorAll(".custom-alert").forEach((alert) => alert.remove());

  const alertDiv = document.createElement("div");
  alertDiv.className = `custom-alert alert-${type}`;

  const icon =
    type === "success"
      ? "bi-check-circle-fill"
      : type === "error"
      ? "bi-x-circle-fill"
      : type === "warning"
      ? "bi-exclamation-triangle-fill"
      : "bi-info-circle-fill";

  alertDiv.innerHTML = `
    <i class="alert-icon bi ${icon}"></i>
    <span class="alert-message">${message}</span>
    <button class="alert-close" onclick="this.parentElement.remove()">×</button>
  `;

  document.body.appendChild(alertDiv);

  // Auto-hide after 6 seconds
  setTimeout(() => {
    if (alertDiv.parentElement) {
      alertDiv.style.animation = "slideOutRight 0.3s ease-in";
      setTimeout(() => alertDiv.remove(), 300);
    }
  }, 6000);
}
```

**Tính năng Alert:**

- **Nhiều loại**: success, error, warning, info
- **Hệ thống icon**: Tích hợp Bootstrap Icons
- **Tự động đóng**: Timeout 6 giây
- **Đóng thủ công**: Nút X
- **Hiệu ứng**: Hiệu ứng trượt vào/ra
- **Singleton**: Xóa các alert hiện tại

### 7.3 Backend Validation

```java
@PostMapping("/cart/add")
@ResponseBody
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
    try {
        // Validate product exists
        Product product = productService.findByID(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        // Validate product is available
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product not available");
        }

        // Process cart addition...

    } catch (Exception e) {
        // Log error for debugging
        logger.error("Error adding to cart: " + e.getMessage(), e);

        // Return error response
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
```

**Các lớp kiểm tra:**

1. **Kiểm tra tham số**: productId không null
2. **Tồn tại entity**: Sản phẩm có trong database
3. **Quy tắc kinh doanh**: Sản phẩm có sẵn không
4. **Xử lý ngoại lệ**: Phản hồi lỗi thích hợp

## 8. PERFORMANCE CONSIDERATIONS

### 8.1 Tối ưu Session

#### A. Sử dụng bộ nhớ

```java
// DTO nhẹ thay vì Product entity đầy đủ
CartItemDTO item = new CartItemDTO();
item.setProductId(productId);
item.setName(product.getName());
item.setPrice(price);
// Không lưu trữ đối tượng Product đầy đủ
```

#### B. Giới hạn kích thước Session

- **Giới hạn mặc định**: 4KB dữ liệu session
- **Dung lượng giỏ hàng**: ~50-100 sản phẩm thông thường
- **Giám sát**: Theo dõi tăng trưởng kích thước session

### 8.2 Tối ưu Database

#### A. Giảm thiểu Database Calls

```java
// Một DB call cho mỗi thao tác thêm
Product product = productService.findByID(productId);

// Cache trong DTO, tránh lookup lặp lại
item.setName(product.getName());
item.setPrice(product.getPrice());
```

#### B. Chiến lược Lazy Loading

- **Loading theo yêu cầu**: Chỉ fetch dữ liệu cần thiết
- **Caching**: Cân nhắc Redis cho sản phẩm được truy cập thường xuyên
- **Thao tác batch**: Nhóm nhiều thao tác giỏ hàng

### 8.3 Tối ưu mạng

#### A. JSON Payloads tối thiểu

```json
// Phản hồi giỏ hàng hiệu quả
[
  {
    "productId": 123,
    "name": "Hoa hồng đỏ",
    "price": 150000,
    "quantity": 2,
    "priceAfterDiscount": null
  }
]
```

#### B. Hiệu quả AJAX

- **Endpoint đơn**: `/pos/cart/add` xử lý tất cả thêm vào
- **Cập nhật batch**: Cân nhắc thao tác giỏ hàng hàng loạt
- **Response caching**: Browser cache cho dữ liệu tĩnh

## 9. SECURITY CONSIDERATIONS

### 9.1 Session Security

#### A. Session Hijacking Prevention

```java
// Spring Security configuration
@Configuration
public class SecurityConfig {
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
```

#### B. CSRF Protection

```html
<!-- Automatic CSRF token inclusion -->
<form th:action="@{/pos/checkout}" method="post">
  <!-- Spring automatically adds CSRF token -->
  <input type="hidden" name="paymentMethod" value="cash" />
</form>
```

### 9.2 Input Validation

#### A. Product ID Validation

```java
@PostMapping("/cart/add")
public List<CartItemDTO> addToCart(@RequestParam @Min(1) Long productId, HttpSession session) {
    // Spring validation annotations
    // @Min(1) ensures positive product ID
}
```

#### B. Quantity Limits

```java
if (existing.isPresent()) {
    int currentQuantity = existing.get().getQuantity();
    if (currentQuantity >= MAX_QUANTITY_PER_ITEM) {
        throw new IllegalArgumentException("Exceeded maximum quantity per item");
    }
    existing.get().setQuantity(currentQuantity + 1);
}
```

### 9.3 Authorization

#### A. Role-Based Access

```java
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
@GetMapping("")
public String showPosPage(...) {
    // Only admin and staff can access POS
}
```

#### B. Session Validation

```java
@PostMapping("/cart/add")
public List<CartItemDTO> addToCart(..., HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null || !user.hasRole("STAFF")) {
        throw new UnauthorizedException("Access denied");
    }
}
```

## 10. INTEGRATION VỚI HỆ THỐNG THANH TOÁN

### 10.1 Checkout Process

```java
@PostMapping("/checkout")
public String checkout(@RequestParam String paymentMethod, HttpSession session, Model model) {
    try {
        // 1. GET CART FROM SESSION
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "redirect:/pos?error=empty_cart";
        }

        // 2. CREATE ORDER ENTITY
        Order order = new Order();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        order.setUser(user);
        order.setCreateDate(new Date());
        order.setOrderType("offline");

        // 3. SET PAYMENT METHOD AND STATUS
        if ("cash".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentMethod("CASH");
            order.setStatus("Hoàn tất");
            order.setPaymentStatus("Đã thanh toán");
        } else if ("qr_code".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentMethod("PAYOS");
            order.setStatus("Chờ thanh toán");
            order.setPaymentStatus("Chưa thanh toán");
        }

        // 4. CREATE ORDER DETAILS FROM CART
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

        // 5. SAVE ORDER TO DATABASE
        Order savedOrder = orderDAO.save(order);
        String orderCode = "POS" + savedOrder.getId();
        savedOrder.setOrderCode(orderCode);
        savedOrder = orderDAO.save(savedOrder);

        // 6. HANDLE PAYMENT METHOD
        if ("qr_code".equalsIgnoreCase(paymentMethod)) {
            // Generate QR code for bank transfer
            String qrCodePath = qrCodeService.generateBankTransferQR(
                orderCode, total, "19039778212018", "TCB", "BUI ANH THIEN");

            if (qrCodePath != null) {
                session.setAttribute("pendingOrder", orderCode);
                session.removeAttribute("cart");
                session.setAttribute("qrCodePath", qrCodePath);
                return "redirect:/pos/payment-qr?orderCode=" + orderCode;
            } else {
                return "redirect:/pos?error=qr_generation_failed";
            }
        }

        // 7. CASH PAYMENT - COMPLETE IMMEDIATELY
        if ("cash".equalsIgnoreCase(paymentMethod)) {
            session.removeAttribute("cart");
            return "redirect:/pos/bill?orderCode=" + orderCode;
        }

        session.removeAttribute("cart");
        return "redirect:/pos?success=payment_completed";

    } catch (Exception e) {
        e.printStackTrace();
        return "redirect:/pos?error=system_error";
    }
}
```

**Luồng thanh toán:**

1. **Kiểm tra giỏ hàng**: Đảm bảo giỏ hàng không rỗng
2. **Xác thực người dùng**: Kiểm tra nhân viên đã đăng nhập
3. **Tạo đơn hàng**: Chuyển đổi giỏ hàng thành order entity
4. **Xử lý phương thức thanh toán**: Luồng khác nhau cho tiền mặt/QR
5. **Lưu trữ database**: Lưu order và details
6. **Dọn dẹp session**: Xóa giỏ hàng sau khi thành công
7. **Xử lý redirect**: Điểm đến khác nhau theo loại thanh toán

### 10.2 QR Code Payment Integration

```java
// QR Code generation for bank transfer
String qrCodePath = qrCodeService.generateBankTransferQR(
    orderCode,           // Order identifier
    total,              // Amount to pay
    "19039778212018",   // Bank account number
    "TCB",              // Bank code
    "BUI ANH THIEN"     // Account holder name
);
```

**Tính năng thanh toán QR:**

- **QR chuyển khoản ngân hàng**: Trực tiếp vào tài khoản ngân hàng
- **Theo dõi đơn hàng**: Liên kết QR với đơn hàng cụ thể
- **Xác nhận thủ công**: Nhân viên có thể xác nhận thanh toán
- **Xử lý timeout**: Tự động hủy sau một khoảng thời gian

## 11. COMPARISON: OFFLINE vs ONLINE CART

### 11.1 Storage Mechanism

| Aspect                | Offline (POS)                 | Online (E-commerce)          |
| --------------------- | ----------------------------- | ---------------------------- |
| **Storage**           | HttpSession                   | Database                     |
| **Persistence**       | Session lifetime (30 min)     | Permanent until user removes |
| **Scope**             | Single browser session        | Cross-device, cross-session  |
| **User Login**        | Staff authentication          | Customer authentication      |
| **Concurrent Access** | Single terminal               | Multiple devices             |
| **Data Loss Risk**    | Session timeout/browser close | Very low (DB backup)         |

### 11.2 Performance Characteristics

| Aspect            | Offline (POS)            | Online (E-commerce)       |
| ----------------- | ------------------------ | ------------------------- |
| **Read Speed**    | Very fast (memory)       | Fast (indexed DB queries) |
| **Write Speed**   | Very fast (memory)       | Medium (DB transactions)  |
| **Scalability**   | Limited (session memory) | High (DB scaling)         |
| **Memory Usage**  | Per-session overhead     | Shared DB storage         |
| **Network Calls** | Minimal                  | More frequent DB calls    |

### 11.3 Use Case Optimization

#### A. Hệ thống POS (Offline)

- **Giao dịch nhanh**: Phản hồi ngay lập tức cho nhân viên
- **Lưu trữ tạm thời**: Không cần lưu trữ lâu dài
- **Người dùng đơn**: Một nhân viên cho mỗi terminal
- **Quy trình đơn giản**: Thêm/xóa/thanh toán nhanh chóng

#### B. Thương mại điện tử (Online)

- **Tính liên tục mua sắm**: Tiếp tục giỏ hàng qua các phiên
- **Đa thiết bị**: Truy cập từ điện thoại, máy tính bảng, desktop
- **Danh sách yêu thích**: Chức năng lưu để sau
- **So sánh**: Nhiều giỏ hàng, sản phẩm đã lưu

## 12. MONITORING VÀ DEBUGGING

### 12.1 Session Monitoring

```java
@Component
public class SessionMonitor {

    @EventListener
    public void handleSessionCreated(HttpSessionCreatedEvent event) {
        logger.info("Session created: " + event.getSession().getId());
    }

    @EventListener
    public void handleSessionDestroyed(HttpSessionDestroyedEvent event) {
        HttpSession session = event.getSession();
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart != null && !cart.isEmpty()) {
            logger.warn("Session destroyed with non-empty cart: " + cart.size() + " items");
        }
    }
}
```

### 12.2 Cart Analytics

```java
@PostMapping("/cart/add")
@ResponseBody
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
    // Track cart operations for analytics
    analyticsService.trackCartAddition(productId, session.getId());

    // Existing cart logic...

    // Track cart size changes
    analyticsService.trackCartSize(cart.size(), session.getId());

    return cart;
}
```

### 12.3 Error Logging

```java
@PostMapping("/cart/add")
@ResponseBody
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
    try {
        // Cart operations...
    } catch (Exception e) {
        // Detailed error logging
        logger.error("Cart addition failed for productId: {} in session: {}. Error: {}",
                    productId, session.getId(), e.getMessage(), e);

        // Track error metrics
        metricsService.incrementCartError("add_to_cart", e.getClass().getSimpleName());

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Unable to add product to cart");
    }
}
```

## 13. BEST PRACTICES VÀ RECOMMENDATIONS

### 13.1 Session Management Best Practices

#### A. Session Timeout Configuration

```properties
# application.properties
server.servlet.session.timeout=30m
server.servlet.session.cookie.max-age=1800
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
```

#### B. Session Size Monitoring

```java
public void validateSessionSize(HttpSession session) {
    // Monitor session attribute sizes
    int cartSize = getSessionObjectSize(session.getAttribute("cart"));
    if (cartSize > MAX_CART_SIZE_BYTES) {
        logger.warn("Large cart session detected: {} bytes", cartSize);
    }
}
```

### 13.2 Frontend Best Practices

#### A. Progressive Enhancement

```javascript
// Ensure functionality works without JavaScript
document.addEventListener("DOMContentLoaded", function () {
  if (!window.fetch) {
    // Fallback for older browsers
    showAlert(
      "Your browser doesn't support modern features. Please upgrade.",
      "warning"
    );
    return;
  }

  // Enhanced functionality
  initializeCartFeatures();
});
```

#### B. Accessibility

```html
<!-- Proper ARIA labels -->
<button
  class="pos-card-btn"
  type="button"
  th:attr="data-id=${product.id}"
  onclick="addToCart(this)"
  aria-label="Add product to cart"
>
  Chọn
</button>

<!-- Screen reader friendly cart table -->
<table
  class="table table-bordered pos-cart-table"
  role="grid"
  aria-label="Shopping cart items"
>
  <thead>
    <tr role="row">
      <th scope="col">Sản phẩm</th>
      <th scope="col">Số lượng</th>
      <th scope="col">Thành tiền</th>
      <th scope="col">Thao tác</th>
    </tr>
  </thead>
</table>
```

### 13.3 Security Best Practices

#### A. Input Sanitization

```java
@PostMapping("/cart/add")
public List<CartItemDTO> addToCart(@RequestParam @Valid @Positive Long productId,
                                  HttpSession session) {
    // Spring validation ensures positive product ID
    // Additional business validation
    if (productId > MAX_PRODUCT_ID) {
        throw new IllegalArgumentException("Invalid product ID");
    }
}
```

#### B. Rate Limiting

```java
@Component
public class CartRateLimiter {
    private final Map<String, Integer> sessionCartOperations = new ConcurrentHashMap<>();

    public boolean isAllowed(String sessionId) {
        int operations = sessionCartOperations.getOrDefault(sessionId, 0);
        if (operations > MAX_OPERATIONS_PER_MINUTE) {
            return false;
        }
        sessionCartOperations.put(sessionId, operations + 1);
        return true;
    }
}
```

## 14. TROUBLESHOOTING COMMON ISSUES

### 14.1 Session Lost Issues

#### Problem: Cart disappears unexpectedly

```java
// Debug session status
@GetMapping("/debug/session")
public ResponseEntity<Map<String, Object>> debugSession(HttpSession session) {
    Map<String, Object> info = new HashMap<>();
    info.put("sessionId", session.getId());
    info.put("creationTime", new Date(session.getCreationTime()));
    info.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
    info.put("maxInactiveInterval", session.getMaxInactiveInterval());
    info.put("cartSize", getCartSize(session));
    return ResponseEntity.ok(info);
}
```

#### Solution: Session configuration

```properties
# Increase session timeout
server.servlet.session.timeout=60m

# Enable session persistence
spring.session.store-type=redis
spring.redis.host=localhost
spring.redis.port=6379
```

### 14.2 Memory Issues

#### Problem: High memory usage

```java
// Monitor session memory usage
@Scheduled(fixedRate = 60000) // Every minute
public void monitorSessionMemory() {
    long totalSessions = sessionRegistry.getAllPrincipals().size();
    long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    if (memoryUsed > MAX_MEMORY_THRESHOLD) {
        logger.warn("High memory usage: {} MB with {} active sessions",
                   memoryUsed / 1024 / 1024, totalSessions);
    }
}
```

#### Solution: Session cleanup

```java
@Scheduled(fixedRate = 300000) // Every 5 minutes
public void cleanupInactiveSessions() {
    sessionRegistry.getAllPrincipals().forEach(principal -> {
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
        sessions.stream()
                .filter(SessionInformation::isExpired)
                .forEach(session -> {
                    logger.info("Cleaning up expired session: " + session.getSessionId());
                    sessionRegistry.removeSessionInformation(session.getSessionId());
                });
    });
}
```

### 14.3 Concurrency Issues

#### Problem: Race conditions in cart updates

```java
// Synchronized cart operations
private final Object cartLock = new Object();

@PostMapping("/cart/add")
public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
    synchronized (cartLock) {
        // Atomic cart operations
        List<CartItemDTO> cart = getCart(session);
        // ... modify cart
        session.setAttribute("cart", cart);
        return cart;
    }
}
```

## 15. KẾT LUẬN

### 15.1 Tóm tắt kiến trúc

Hệ thống POS cart sử dụng **lưu trữ dựa trên session** với các đặc điểm:

1. **Frontend (pos.html + pos.js)**:

   - Thao tác giỏ hàng điều khiển bằng AJAX
   - Cập nhật giao diện theo thời gian thực
   - Hệ thống thông báo tùy chỉnh
   - Thiết kế responsive

2. **Backend (PosController)**:

   - Lưu trữ dựa trên session
   - Các endpoint RESTful API
   - Tính toán giá giảm
   - Xử lý lỗi

3. **Lớp dữ liệu (CartItemDTO)**:
   - Mẫu DTO nhẹ
   - Có thể tuần tự hóa session
   - Tách biệt logic kinh doanh

### 15.2 Ưu điểm chính

#### A. Hiệu suất

- **Thời gian phản hồi nhanh**: Lưu trữ dựa trên bộ nhớ
- **Tải database tối thiểu**: Không có overhead lưu trữ giỏ hàng
- **Cập nhật thời gian thực**: AJAX không cần refresh trang

#### B. Đơn giản

- **Không có quản lý state phức tạp**: Session xử lý persistence
- **Dễ debug**: Tất cả dữ liệu trong HTTP session
- **Triển khai đơn giản**: Không cần yêu cầu lưu trữ bổ sung

#### C. Bảo mật

- **Cô lập session**: Mỗi terminal có giỏ hàng riêng biệt
- **Dọn dẹp tự động**: Sessions tự nhiên hết hạn
- **Truy cập dựa trên role**: Yêu cầu xác thực nhân viên

### 15.3 Hạn chế và cân nhắc

#### A. Khả năng mở rộng

- **Sử dụng bộ nhớ**: Tỷ lệ thuận với sessions đồng thời
- **Máy chủ đơn**: Session không sống sót qua việc restart server
- **Load balancing**: Yêu cầu sticky sessions

#### B. Lưu trữ dữ liệu

- **Lưu trữ tạm thời**: Giỏ hàng mất khi session hết hạn
- **Không cross-device**: Không thể tiếp tục trên terminal khác
- **Khôi phục**: Không có cách nào khôi phục giỏ hàng đã mất

### 15.4 Cải tiến tương lai

#### A. Tính năng nâng cao

- **Templates giỏ hàng**: Lưu các tổ hợp sản phẩm thông dụng
- **Thao tác bulk**: Thêm nhiều sản phẩm cùng lúc
- **Ghi đè giá**: Điều chỉnh giá thủ công cho nhân viên
- **Màn hình khách hàng**: Màn hình ngoài cho khách hàng xem

#### B. Cải tiến kỹ thuật

- **Lưu trữ session Redis**: Lưu trữ qua các lần restart server
- **Cập nhật Websocket**: Cộng tác thời gian thực
- **Audit trail**: Theo dõi tất cả thao tác giỏ hàng
- **A/B testing**: Các biến thể giao diện giỏ hàng khác nhau

### 15.5 Khuyến nghị cuối cùng

Hệ thống cart offline hiện tại **phù hợp với nhu cầu POS** với:

- Thao tác giỏ hàng nhanh, đáng tin cậy
- Bảo trì và debug đơn giản
- Bảo mật phù hợp cho môi trường bán lẻ
- Trải nghiệm người dùng tốt cho nhân viên

Đây là một **triển khai vững chắc** cho hệ thống bán hàng tại quầy với sự cân bằng tốt giữa hiệu suất, đơn giản và chức năng.
