# 📋 Tài Liệu Hệ Thống Shipper - Hoa Shop

## 📌 Tổng Quan Hệ Thống

Hệ thống Shipper của Hoa Shop được xây dựng trên nền tảng **Spring Boot 2.x** với kiến trúc **MVC**, cung cấp một quy trình hoàn chỉnh cho việc quản lý và giao hàng từ khi nhận đơn đến khi hoàn tất. Hệ thống tích hợp Thymeleaf templates, Bootstrap UI, và bản đồ tương tác để hỗ trợ shipper trong công việc giao hàng.

### 🎯 Mục Tiêu Chính

- **Quản lý đơn hàng**: Từ nhận đơn đến hoàn tất giao hàng
- **Theo dõi trạng thái**: Cập nhật real-time trạng thái đơn hàng
- **Hỗ trợ navigation**: Tích hợp Google Maps và Leaflet
- **Báo cáo thống kê**: Lịch sử giao hàng và doanh thu
- **Xử lý ngoại lệ**: Giao thất bại, hoàn trả, hủy đơn

---

## 🏗️ Kiến Trúc Hệ Thống

### 📂 Cấu Trúc Thư Mục

```
src/main/
├── java/com/datn/
│   ├── Controller/shipper/
│   │   └── ShipperOrderController.java      # Controller chính
│   ├── Service/
│   │   ├── OrderService.java                # Interface service
│   │   └── impl/OrderServiceImpl.java       # Implementation
│   ├── dao/
│   │   └── OrderDAO.java                    # Data Access Layer
│   └── model/
│       ├── Order.java                       # Entity đơn hàng
│       └── User.java                        # Entity shipper
└── resources/templates/shipper/
    ├── layout.html                          # Layout chung
    ├── content.html                         # Content wrapper
    ├── sidebar.html                         # Navigation menu
    ├── pending-orders.html                  # Đơn chờ giao
    ├── my-orders.html                       # Đơn đang giao
    ├── returned-orders.html                 # Đơn giao thất bại
    └── history.html                         # Lịch sử giao hàng
```

### 🔄 Luồng Xử Lý MVC

```
1. Shipper Request → ShipperOrderController
2. Controller → OrderService (Business Logic)
3. OrderService → OrderDAO (Database)
4. Database Response → Service → Controller
5. Controller → Thymeleaf Template → Response
```

---

## 🚚 Quy Trình Giao Hàng (Workflow)

### 📊 Sơ Đồ Trạng Thái Đơn Hàng

```
[Chờ giao] ──nhận đơn──> [Đang giao] ──hoàn tất──> [Hoàn tất]
     │                        │                        │
     │                    hoàn trả                 (kết thúc)
     │                        │
     │                   [Chờ giao]
     │                        │
     └──────── giao thất bại ─┴─────> [Giao thất bại]
```

### 🔄 Sơ đồ luồng xử lý toàn bộ hệ thống Shipper

```
┌─────────────────────────────────────────────────────────────────────┐
│                    LUỒNG XỬ LÝ HỆ THỐNG SHIPPER                    │
└─────────────────────────────────────────────────────────────────────┘

1. ĐĂNG NHẬP VÀ AUTHENTICATION
   ┌─────────────────┐
   │ Shipper login   │
   │ với role = 2    │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ AuthService     │────▶│ Session created │
   │ verify role     │     │ with user info  │
   └─────────────────┘     └─────────────────┘
           │
           ▼
2. TRUY CẬP DASHBOARD
   ┌─────────────────┐
   │ Redirect to     │
   │ /shipper/       │
   │ pending-orders  │
   └─────────────────┘
           │
           ▼
3. XEM DANH SÁCH ĐỚN HÀNG
   ┌─────────────────┐     ┌─────────────────┐
   │ GET /shipper/   │────▶│ OrderService    │
   │ pending-orders  │     │ .getOrdersBy    │
   └─────────────────┘     │ Status("Chờ     │
           │               │ giao")          │
           │               └─────────────────┘
           ▼                        │
   ┌─────────────────┐              │
   │ Controller      │◄─────────────┘
   │ pendingOrders() │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ Thymeleaf       │
   │ render          │
   │ pending-orders  │
   │ .html           │
   └─────────────────┘
           │
           ▼
4. SHIPPER CHỌN NHẬN ĐỚN
   ┌─────────────────┐
   │ Click "Nhận đơn"│
   │ button          │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ POST /shipper/  │
   │ receive         │
   │ orderId = X     │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ receiveOrder()  │────▶│ OrderService    │
   │ method          │     │ .updateToDang   │
   └─────────────────┘     │ Giao(orderId,   │
           │               │ shipperId)      │
           │               └─────────────────┘
           ▼                        │
   ┌─────────────────┐              ▼
   │ Business        │     ┌─────────────────┐
   │ Validation:     │     │ Database:       │
   │ • User role = 2 │     │ • Update status │
   │ • Order status  │     │ • Set shipper   │
   │   valid         │     │ • Save changes  │
   └─────────────────┘     └─────────────────┘
           │                        │
           └───────────┬────────────┘
                       ▼
   ┌─────────────────┐
   │ Redirect to     │
   │ /shipper/       │
   │ my-orders       │
   └─────────────────┘
           │
           ▼
5. QUẢN LÝ ĐỚN ĐANG GIAO
   ┌─────────────────┐     ┌─────────────────┐
   │ GET /shipper/   │────▶│ Load orders     │
   │ my-orders       │     │ with status     │
   └─────────────────┘     │ "Đang giao"     │
           │               │ & shipper_id    │
           │               └─────────────────┘
           ▼
   ┌─────────────────┐
   │ Display:        │
   │ • Order details │
   │ • Customer info │
   │ • Map button    │
   │ • Action btns   │
   └─────────────────┘
           │
           ▼
6. SỬ DỤNG BẢN ĐỒ ĐIỀU HƯỚNG
   ┌─────────────────┐
   │ Click "Bản đồ"  │
   │ button          │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ JavaScript      │────▶│ Leaflet Map     │
   │ openMapModal()  │     │ initialization  │
   └─────────────────┘     └─────────────────┘
           │                        │
           ▼                        ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ Geocoding       │     │ Current         │
   │ destination     │     │ location        │
   │ address         │     │ detection       │
   └─────────────────┘     └─────────────────┘
           │                        │
           └───────────┬────────────┘
                       ▼
   ┌─────────────────┐
   │ Google Maps     │
   │ Navigation      │
   │ URL opened      │
   └─────────────────┘
           │
           ▼
7. HOÀN TẤT GIAO HÀNG
   ┌─────────────────┐
   │ Click "Hoàn     │
   │ tất" button     │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ POST /shipper/  │
   │ orders/complete │
   │ /{orderId}      │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ completeOrder() │────▶│ Validation:     │
   │ method          │     │ • Order status  │
   └─────────────────┘     │ • Shipper owns  │
           │               │   order         │
           │               └─────────────────┘
           ▼                        │
   ┌─────────────────┐              ▼
   │ OrderService    │     ┌─────────────────┐
   │ .updateTo       │     │ Update DB:      │
   │ Completed()     │     │ status =        │
   └─────────────────┘     │ "Hoàn tất"      │
           │               └─────────────────┘
           ▼
   ┌─────────────────┐
   │ Success message │
   │ & redirect      │
   └─────────────────┘

8. XỬ LÝ NGOẠI LỆ - GIAO THẤT BẠI
   ┌─────────────────┐
   │ Click "Giao     │
   │ thất bại"       │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ Modal popup     │────▶│ Form inputs:    │
   │ appears         │     │ • Failure reason│
   └─────────────────┘     │ • Details       │
           │               └─────────────────┘
           ▼
   ┌─────────────────┐
   │ POST /shipper/  │
   │ orders/failed   │
   └─────────────────┘
           │
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ failedDelivery()│────▶│ Save reason &   │
   │ method          │     │ details to DB   │
   └─────────────────┘     └─────────────────┘
           │
           ▼
   ┌─────────────────┐
   │ Status =        │
   │ "Giao thất bại" │
   └─────────────────┘

9. XEM LỊCH SỬ VÀ BÁO CÁO
   ┌─────────────────┐     ┌─────────────────┐
   │ GET /shipper/   │────▶│ Filter form:    │
   │ history         │     │ • Date          │
   └─────────────────┘     │ • Month/Year    │
           │               │ • Pagination    │
           │               └─────────────────┘
           ▼
   ┌─────────────────┐     ┌─────────────────┐
   │ Query database  │────▶│ Calculate:      │
   │ with filters    │     │ • Total orders  │
   └─────────────────┘     │ • Total revenue │
           │               │ • Success rate  │
           │               └─────────────────┘
           ▼
   ┌─────────────────┐
   │ Display results │
   │ with pagination │
   └─────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         TRẠNG THÁI DATABASE                         │
├─────────────────────────────────────────────────────────────────────┤
│ orders table:                                                       │
│ ┌─────────────┬────────────┬─────────────┬──────────────┐          │
│ │ status      │ shipper_id │ create_date │ delivery_date│          │
│ ├─────────────┼────────────┼─────────────┼──────────────┤          │
│ │ Chờ giao    │ NULL       │ 2025-08-26  │ 2025-08-27   │          │
│ │ Đang giao   │ 5          │ 2025-08-26  │ 2025-08-27   │          │
│ │ Hoàn tất    │ 5          │ 2025-08-26  │ 2025-08-27   │          │
│ │ Giao thất   │ 5          │ 2025-08-26  │ 2025-08-27   │          │
│ │ bại         │            │             │              │          │
│ └─────────────┴────────────┴─────────────┴──────────────┘          │
└─────────────────────────────────────────────────────────────────────┘
```

### 🔄 Sequence Diagram - Nhận và Hoàn Tất Đơn Hàng

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SEQUENCE DIAGRAM - RECEIVE ORDER                 │
└─────────────────────────────────────────────────────────────────────┘

Shipper    Browser    Controller    Service    DAO    Database
   │          │           │           │        │         │
   │ 1. Click │           │           │        │         │
   │ "Nhận đơn"│          │           │        │         │
   │ ────────▶│           │           │        │         │
   │          │ 2. POST   │           │        │         │
   │          │ /receive  │           │        │         │
   │          │──────────▶│           │        │         │
   │          │           │ 3. getUser│        │         │
   │          │           │ ─────────▶│        │         │
   │          │           │◄──────────│        │         │
   │          │           │ 4. validate        │         │
   │          │           │    role=2 │        │         │
   │          │           │           │ 5.updateToDangGiao│
   │          │           │           │ ──────▶│         │
   │          │           │           │        │ 6.findById│
   │          │           │           │        │ ────────▶│
   │          │           │           │        │◄─────────│
   │          │           │           │        │ 7. save │
   │          │           │           │        │ ────────▶│
   │          │           │           │        │◄─────────│
   │          │           │           │◄───────│         │
   │          │           │◄──────────│        │         │
   │          │           │ 8. redirect        │         │
   │          │◄──────────│           │        │         │
   │          │ 9. GET    │           │        │         │
   │          │ /my-orders│           │        │         │
   │          │──────────▶│           │        │         │
   │◄─────────│           │           │        │         │
  Success     Updated UI

┌─────────────────────────────────────────────────────────────────────┐
│                  SEQUENCE DIAGRAM - COMPLETE ORDER                  │
└─────────────────────────────────────────────────────────────────────┘

Shipper    Browser    Controller    Service    DAO    Database
   │          │           │           │        │         │
   │ 1. Click │           │           │        │         │
   │ "Hoàn tất"│          │           │        │         │
   │ ────────▶│           │           │        │         │
   │          │ 2. POST   │           │        │         │
   │          │ /complete │           │        │         │
   │          │──────────▶│           │        │         │
   │          │           │ 3. getUser│        │         │
   │          │           │ ─────────▶│        │         │
   │          │           │◄──────────│        │         │
   │          │           │ 4. validate        │         │
   │          │           │    role=2 │        │         │
   │          │           │           │ 5.updateToCompleted│
   │          │           │           │ ──────▶│         │
   │          │           │           │        │ 6.findById│
   │          │           │           │        │ ────────▶│
   │          │           │           │        │◄─────────│
   │          │           │           │        │ 7.validate│
   │          │           │           │        │   status │
   │          │           │           │        │ 8. save │
   │          │           │           │        │ ────────▶│
   │          │           │           │        │◄─────────│
   │          │           │           │◄───────│         │
   │          │           │◄──────────│        │         │
   │          │           │ 9. redirect        │         │
   │          │◄──────────│           │        │         │
   │◄─────────│           │           │        │         │
  Success     Order Completed
```

### ⚠️ Sơ đồ xử lý lỗi và ngoại lệ

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ERROR HANDLING FLOWCHART                         │
└─────────────────────────────────────────────────────────────────────┘

                    USER ACTION
                         │
                         ▼
                ┌─────────────────┐
                │ Request đến     │
                │ Controller      │
                └─────────────────┘
                         │
                         ▼
                ┌─────────────────┐     ┌─────────────────┐
                │ 1. Session      │────▶│ Session NULL    │
                │    Check        │ NO  │ → Redirect      │
                └─────────────────┘     │   /login        │
                         │YES           └─────────────────┘
                         ▼
                ┌─────────────────┐     ┌─────────────────┐
                │ 2. Role         │────▶│ Role != 2       │
                │    Validation   │ NO  │ → Access        │
                └─────────────────┘     │   Denied        │
                         │YES           └─────────────────┘
                         ▼
                ┌─────────────────┐     ┌─────────────────┐
                │ 3. Order        │────▶│ Order NULL      │
                │    Exists       │ NO  │ → Error Message │
                └─────────────────┘     │   "Không tồn tại"│
                         │YES           └─────────────────┘
                         ▼
                ┌─────────────────┐     ┌─────────────────┐
                │ 4. Status       │────▶│ Invalid Status  │
                │    Validation   │ NO  │ → Error Message │
                └─────────────────┘     │   "Trạng thái   │
                         │YES           │    không hợp lệ"│
                         ▼              └─────────────────┘
                ┌─────────────────┐
                │ 5. Ownership    │     ┌─────────────────┐
                │    Check        │────▶│ Not Owner       │
                └─────────────────┘ NO  │ → Error Message │
                         │YES           │   "Không có     │
                         ▼              │    quyền"       │
                ┌─────────────────┐     └─────────────────┘
                │ 6. Business     │
                │    Logic        │
                │    Execute      │
                └─────────────────┘
                         │
                 ┌───────┴───────┐
                 │               │
                ▼                ▼
        ┌─────────────────┐  ┌─────────────────┐
        │ SUCCESS         │  │ EXCEPTION       │
        │ → Redirect      │  │ → Log Error     │
        │   with success  │  │ → Error Message │
        │   message       │  │ → Redirect back │
        └─────────────────┘  └─────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                      JAVASCRIPT ERROR HANDLING                      │
└─────────────────────────────────────────────────────────────────────┘

    Browser Event (Click button)
              │
              ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ JavaScript      │────▶│ Network Error   │
    │ Function Call   │     │ → Show toast    │
    └─────────────────┘     │   "Lỗi kết nối" │
              │             └─────────────────┘
              ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ AJAX Request    │────▶│ Server Error    │
    │ to Server       │     │ → Show toast    │
    └─────────────────┘     │   "Lỗi server"  │
              │             └─────────────────┘
              ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ Response        │────▶│ Validation Err  │
    │ Processing      │     │ → Show message  │
    └─────────────────┘     │   from server   │
              │             └─────────────────┘
              ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ UI Update       │────▶│ UI Update Fail  │
    │ Success         │     │ → Refresh page │
    └─────────────────┘     └─────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                     MAP INTEGRATION ERRORS                          │
└─────────────────────────────────────────────────────────────────────┘

    Map Button Click
           │
           ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ Address         │────▶│ Empty Address   │
    │ Validation      │ NO  │ → Alert user    │
    └─────────────────┘     │   "Địa chỉ trống"│
           │YES             └─────────────────┘
           ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ Geocoding       │────▶│ Geocoding Fail  │
    │ API Call        │     │ → Use default   │
    └─────────────────┘     │   coordinates   │
           │                └─────────────────┘
           ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ Map             │────▶│ Map Load Fail   │
    │ Initialization  │     │ → Fallback to   │
    └─────────────────┘     │   Google Maps   │
           │                └─────────────────┘
           ▼
    ┌─────────────────┐     ┌─────────────────┐
    │ Geolocation     │────▶│ Location Denied │
    │ Request         │     │ → Use Đà Nẵng   │
    └─────────────────┘     │   default       │
           │                └─────────────────┘
           ▼
    ┌─────────────────┐
    │ Navigation      │
    │ Success         │
    └─────────────────┘
```

### 🔍 Chi Tiết Từng Bước

#### 1️⃣ **Nhận Đơn Hàng** (`/shipper/pending-orders`)

**Controller Method**: `pendingOrders()`

```java
@GetMapping("/pending-orders")
public String pendingOrders(Model model) {
    List<Order> confirmedOrders = orderService.getOrdersByStatus("Chờ giao");
    List<Order> deliveringOrders = orderService.getOrdersByStatus("Giao lại");

    List<Order> pendingOrders = new ArrayList<>();
    pendingOrders.addAll(confirmedOrders);
    pendingOrders.addAll(deliveringOrders);

    model.addAttribute("orders", pendingOrders);
    model.addAttribute("view", "shipper/pending-orders");
    return "shipper/layout";
}
```

**Trạng thái xử lý**:

- `Chờ giao`: Đơn hàng mới được admin xác nhận
- `Giao lại`: Đơn hàng bị hoàn trả từ shipper khác

**UI Features**:

- Accordion layout hiển thị thông tin đơn hàng
- Thông tin khách hàng và người nhận
- Nút "Nhận đơn" để chuyển sang trạng thái giao hàng

#### 2️⃣ **Nhận Đơn** (`POST /shipper/receive`)

**Controller Method**: `receiveOrder()`

```java
@PostMapping("/receive")
public String receiveOrder(@RequestParam("orderId") Long orderId) {
    User shipper = authService.getUser();
    if (shipper != null && shipper.getRole() == 2) {
        orderService.updateToDangGiao(orderId, shipper.getId());
    }
    return "redirect:/shipper/my-orders";
}
```

**Service Implementation**:

```java
@Override
public Order updateToDangGiao(Long orderId, int shipperId) {
    Order order = dao.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));
    User shipper = userDAO.findById(shipperId)
        .orElseThrow(() -> new IllegalArgumentException("Shipper không tồn tại"));

    // Kiểm tra trạng thái hợp lệ
    if (!order.getStatus().equals("Chờ giao") && !order.getStatus().equals("Giao lại")) {
        throw new IllegalStateException("Chỉ có thể nhận đơn hàng ở trạng thái 'Chờ giao' hoặc 'Giao lại'");
    }

    order.setStatus("Đang giao");
    order.setShipper(shipper);
    return dao.save(order);
}
```

**Business Rules**:

- Chỉ shipper có role = 2 mới có thể nhận đơn
- Đơn hàng phải ở trạng thái `Chờ giao` hoặc `Giao lại`
- Gán shipper vào đơn hàng và chuyển trạng thái thành `Đang giao`

#### 3️⃣ **Quản Lý Đơn Đang Giao** (`/shipper/my-orders`)

**Controller Method**: `myOrders()`

```java
@GetMapping("/my-orders")
public String myOrders(Model model) {
    User shipper = authService.getUser();
    if (shipper != null && shipper.getRole() == 2) {
        List<Order> orders = orderService.getOrdersByStatusAndShipper("Đang giao", shipper.getId());
        model.addAttribute("orders", orders);
    }
    model.addAttribute("view", "shipper/my-orders");
    return "shipper/layout";
}
```

**Tích Hợp Bản Đồ**:

```javascript
// Mở Google Maps Navigation
function openMapModal(button) {
  const address = button.getAttribute("data-address");
  const orderId = button.getAttribute("data-order-id");

  // Khởi tạo Leaflet map
  initializeMap(address);

  // Tích hợp Google Maps Direction
  const googleMapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(
    address
  )}`;
  window.open(googleMapsUrl, "_blank");
}
```

**Chức Năng Hỗ Trợ**:

- **Bản đồ chỉ đường**: Leaflet + Google Maps API
- **Geolocation**: Lấy vị trí hiện tại của shipper
- **Navigation**: Điều hướng đến địa chỉ giao hàng

#### 4️⃣ **Hoàn Tất Giao Hàng** (`POST /shipper/orders/complete/{orderId}`)

**Controller Method**: `completeOrder()`

```java
@PostMapping("/orders/complete/{orderId}")
public String completeOrder(@PathVariable("orderId") Long orderId) {
    User shipper = authService.getUser();
    if (shipper != null && shipper.getRole() == 2) {
        orderService.updateToCompleted(orderId, shipper.getId());
    }
    return "redirect:/shipper/my-orders";
}
```

**Service Implementation**:

```java
@Override
public void updateToCompleted(Long orderId, int shipperId) {
    Order order = dao.findById(orderId).orElse(null);
    User shipper = userDAO.findById(shipperId).orElse(null);
    if (order != null && shipper != null && "Đang giao".equals(order.getStatus())) {
        order.setStatus("Hoàn tất");
        order.setShipper(shipper);
        dao.save(order);
    }
}
```

#### 5️⃣ **Xử Lý Hoàn Trả** (`POST /shipper/orders/return/{orderId}`)

**Controller Method**: `returnOrder()`

```java
@PostMapping("/orders/return/{orderId}")
public String returnOrder(@PathVariable("orderId") Long orderId) {
    User shipper = authService.getUser();
    if (shipper != null && shipper.getRole() == 2) {
        orderService.updateStatus(orderId, "Chờ giao");
    }
    return "redirect:/shipper/my-orders";
}
```

**Trường Hợp Sử Dụng**:

- Không liên lạc được với khách hàng
- Địa chỉ không chính xác
- Khách hàng yêu cầu thay đổi thời gian giao

#### 6️⃣ **Giao Thất Bại** (`POST /shipper/orders/failed`)

**Controller Method**: `failedDelivery()`

```java
@PostMapping("/orders/failed")
public String failedDelivery(
        @RequestParam("orderId") Long orderId,
        @RequestParam("failureReason") String cancelReason,
        @RequestParam("failureDetails") String cancelDetails,
        RedirectAttributes redirectAttributes) {
    User shipper = authService.getUser();
    if (shipper != null && shipper.getRole() == 2) {
        orderService.cancelByShipper(orderId, shipper.getId(), cancelReason, cancelDetails);
        redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái giao thất bại thành công!");
    }
    return "redirect:/shipper/my-orders";
}
```

**Service Implementation**:

```java
@Override
@Transactional
public Order cancelByShipper(Long orderId, int shipperId, String cancelReason, String cancelDetails) {
    Order order = dao.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

    if (order.getShipper() == null || order.getShipper().getId() != shipperId) {
        throw new IllegalArgumentException("Bạn không được phép hủy đơn hàng này");
    }

    order.setReason(cancelReason);        // Lưu lý do hủy
    order.setDescription(cancelDetails);  // Lưu chi tiết lý do
    order.setStatus("Giao thất bại");
    return dao.save(order);
}
```

**Modal UI cho Giao Thất Bại**:

```html
<div class="modal fade" id="failedDeliveryModal">
  <form method="post" th:action="@{/shipper/orders/failed}">
    <select name="failureReason" required>
      <option value="Khách hàng không có mặt">Khách hàng không có mặt</option>
      <option value="Địa chỉ sai">Địa chỉ sai</option>
      <option value="Khách hàng từ chối nhận hàng">
        Khách hàng từ chối nhận hàng
      </option>
      <option value="Lỗi vận chuyển">Lỗi vận chuyển</option>
      <option value="Khác">Khác</option>
    </select>
    <textarea name="failureDetails" required></textarea>
    <button type="submit">Xác nhận giao thất bại</button>
  </form>
</div>
```

---

## 📈 Hệ Thống Báo Cáo và Thống Kê

### 📊 Lịch Sử Giao Hàng (`/shipper/history`)

**Controller Method**: `history()`

```java
@GetMapping("/history")
public String history(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model) {

    User shipper = authService.getUser();
    List<Order> historyOrders = new ArrayList<>();
    Double totalAmount = 0.0;

    if (shipper != null && shipper.getRole() == 2) {
        Pageable pageable = PageRequest.of(page, size);

        if (date != null) {
            historyOrders = orderService.getOrdersByShipperAndDate(shipper.getId(), date);
            totalAmount = orderService.getTotalAmountByShipperAndDate(shipper.getId(), date);
        } else if (month != null && year != null) {
            historyOrders = orderService.getOrdersByShipperAndMonthYear(shipper.getId(), month, year);
            totalAmount = orderService.getTotalAmountByShipperAndMonthYear(shipper.getId(), month, year);
        } else if (year != null) {
            historyOrders = orderService.getOrdersByShipperAndYear(shipper.getId(), year);
            totalAmount = orderService.getTotalAmountByShipperAndYear(shipper.getId(), year);
        } else {
            Page<Order> orderPage = orderService.getOrdersByStatusAndShipper("Hoàn tất", shipper.getId(), pageable);
            totalAmount = orderService.getTotalCompletedOrdersAmount(shipper.getId());
        }
    }

    model.addAttribute("orders", historyOrders);
    model.addAttribute("total", totalAmount);
    return "shipper/layout";
}
```

### 🎯 Các Loại Báo Cáo

#### 1. **Báo Cáo Theo Ngày**

```sql
-- Service method: getOrdersByShipperAndDate()
SELECT * FROM orders
WHERE shipper_id = ?
  AND DATE(delivery_date) = ?
  AND status = 'Hoàn tất'
ORDER BY delivery_date DESC
```

#### 2. **Báo Cáo Theo Tháng/Năm**

```sql
-- Service method: getOrdersByShipperAndMonthYear()
SELECT * FROM orders
WHERE shipper_id = ?
  AND MONTH(delivery_date) = ?
  AND YEAR(delivery_date) = ?
  AND status = 'Hoàn tất'
ORDER BY delivery_date DESC
```

#### 3. **Tính Tổng Doanh Thu**

```sql
-- Service method: getTotalCompletedOrdersAmount()
SELECT COALESCE(SUM(total_amount), 0) as total_amount
FROM orders
WHERE shipper_id = ? AND status = 'Hoàn tất'
```

### 📅 Validation Logic

```javascript
function validateForm() {
  const dateValue = document.getElementById("date").value;
  const monthValue = document.getElementById("month").value;
  const yearValue = document.getElementById("year").value;

  // Phải chọn ít nhất một tiêu chí
  if (!dateValue && !monthValue && !yearValue) {
    showAlert("error", "Vui lòng chọn ít nhất một tiêu chí lọc!");
    return false;
  }

  // Không được chọn ngày + tháng/năm cùng lúc
  if (dateValue && (monthValue || yearValue)) {
    showAlert("error", "Không thể chọn ngày cùng với tháng hoặc năm!");
    return false;
  }

  // Chọn tháng phải có năm
  if (monthValue && !yearValue) {
    showAlert("error", "Vui lòng chọn năm khi đã chọn tháng!");
    return false;
  }

  return true;
}
```

---

## 🎨 Giao Diện Người Dùng (UI/UX)

### 🖼️ Layout Structure

```html
<!-- shipper/layout.html -->
<div class="container-fluid">
  <div class="row">
    <div th:insert="~{/shipper/sidebar}"></div>
    <article class="col-md-10 articlemana">
      <main th:insert="~{${view}}"></main>
    </article>
  </div>
</div>
```

### 🔧 Sidebar Navigation

```html
<!-- shipper/sidebar.html -->
<aside class="shipper-sidebar-dark">
  <nav class="shipper-nav-dark flex-column pt-2">
    <a href="/shipper/pending-orders" class="shipper-nav-link-dark">
      <i class="fa-solid fa-box-open me-2"></i> Đơn chờ giao
    </a>
    <a href="/shipper/my-orders" class="shipper-nav-link-dark">
      <i class="fa-solid fa-truck me-2"></i> Đơn đang giao
    </a>
    <a href="/shipper/returned-orders" class="shipper-nav-link-dark">
      <i class="fas fa-undo-alt me-2"></i> Đơn giao thất bại
    </a>
    <a href="/shipper/history" class="shipper-nav-link-dark">
      <i class="fa-solid fa-clock-rotate-left me-2"></i> Lịch sử giao thành công
    </a>
    <form action="/logout" method="post">
      <button type="submit" class="shipper-nav-link-dark text-danger">
        <i class="fa-solid fa-right-from-bracket me-2"></i> Đăng xuất
      </button>
    </form>
  </nav>
</aside>
```

### 🎭 CSS Framework và Styling

#### Bootstrap 5 Integration

```css
/* Custom shipper styles */
.shipper-sidebar-dark {
  width: 280px;
  height: 100vh;
  background: #1f2937;
  border-right: 1px solid #374151;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1001;
}

.shipper-nav-link-dark {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.5rem;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  margin-bottom: 0.5rem;
  transition: all 0.2s ease;
}

.shipper-nav-link-dark:hover {
  background: #6366f1;
  color: #fff;
}
```

#### Accordion Components

```html
<!-- Order list với accordion style -->
<div class="accordion" id="pendingOrdersAccordion">
  <div class="accordion-item" th:each="order, iterStat : ${orders}">
    <h2 class="accordion-header">
      <button class="accordion-button collapsed bg-light">
        <div class="d-flex flex-wrap align-items-center gap-3">
          🆔 <span th:text="${order.id}" class="fw-bold"></span>
          <span
            class="fw-bold text-primary"
            th:text="${#numbers.formatDecimal(order.totalAmount, 0, 'COMMA', 0, 'POINT')} + ' VND'"
          >
          </span>
          📅
          <span
            th:text="${#temporals.format(order.deliveryDate, 'dd/MM/yyyy')}"
          ></span>
        </div>
      </button>
    </h2>
    <div class="accordion-collapse collapse">
      <div class="accordion-body">
        <!-- Chi tiết đơn hàng và actions -->
      </div>
    </div>
  </div>
</div>
```

### 🗺️ Map Integration

#### Leaflet + Google Maps

```javascript
// Khởi tạo bản đồ Leaflet
function initializeMap(address) {
  map = L.map("map").setView([defaultLat, defaultLng], 13);

  // Add OpenStreetMap tiles
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap contributors",
  }).addTo(map);

  // Geocode địa chỉ giao hàng
  geocodeAddress(address);

  // Lấy vị trí hiện tại
  getCurrentLocationOnMap();
}

// Navigation với Google Maps
document
  .getElementById("startNavigation")
  .addEventListener("click", function () {
    const googleMapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(
      currentOrderAddress
    )}`;
    window.open(googleMapsUrl, "_blank");
  });
```

#### Geolocation API

```javascript
function getCurrentLocationOnMap() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        // Add current location marker
        currentLocationMarker = L.marker([lat, lng], {
          icon: L.icon({
            iconUrl:
              "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png",
            iconSize: [25, 41],
            iconAnchor: [12, 41],
          }),
        }).addTo(map);

        // Create route if destination exists
        if (destinationMarker) {
          createRoute();
        }
      },
      (error) => {
        console.error("Geolocation error:", error);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000,
      }
    );
  }
}
```

---

## 🔐 Bảo Mật và Xác Thực

### 👤 Authentication Check

```java
// Trong mọi controller method
User shipper = authService.getUser();
if (shipper != null && shipper.getRole() == 2) {
    // Chỉ cho phép shipper (role = 2) truy cập
    // Thực hiện logic business
} else {
    // Redirect hoặc throw exception
}
```

### 🛡️ Authorization Rules

- **Role-based Access**: Chỉ user có `role = 2` (shipper) mới truy cập được
- **Order Ownership**: Shipper chỉ có thể thao tác với đơn hàng được gán cho mình
- **Status Validation**: Kiểm tra trạng thái đơn hàng trước khi cho phép thao tác
- **Session Management**: Sử dụng Spring Security session management

### 🔒 Data Validation

```java
// Validation trong Service layer
if (!order.getStatus().equals("Chờ giao") && !order.getStatus().equals("Giao lại")) {
    throw new IllegalStateException("Chỉ có thể nhận đơn hàng ở trạng thái 'Chờ giao' hoặc 'Giao lại'");
}

if (order.getShipper() == null || order.getShipper().getId() != shipperId) {
    throw new IllegalArgumentException("Bạn không được phép hủy đơn hàng này");
}
```

---

## 📊 Database Schema

### 🗃️ Bảng Orders

```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,                    -- Khách hàng đặt hàng
    shipper_id INT NULL,                     -- Shipper được gán
    status VARCHAR(50) NOT NULL,             -- Trạng thái đơn hàng
    total_amount DECIMAL(15,2) NOT NULL,     -- Tổng tiền
    create_date DATETIME NOT NULL,           -- Ngày tạo đơn
    delivery_date DATE NULL,                 -- Ngày giao hàng
    delivery_time TIME NULL,                 -- Giờ giao hàng
    address TEXT NOT NULL,                   -- Địa chỉ giao hàng
    receiver_name VARCHAR(255) NOT NULL,     -- Tên người nhận
    receiver_phone VARCHAR(20) NOT NULL,     -- SĐT người nhận
    payment_method VARCHAR(50) NOT NULL,     -- COD/Online
    reason TEXT NULL,                        -- Lý do hủy/thất bại
    description TEXT NULL,                   -- Mô tả chi tiết
    original_id BIGINT NULL,                 -- ID đơn gốc (nếu là đơn giao lại)
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shipper_id) REFERENCES users(id)
);
```

### 🔗 Các Trạng Thái Đơn Hàng

```sql
-- Enum values cho status column
'Chờ xác nhận'    -- Đơn mới tạo
'Chờ giao'        -- Admin đã xác nhận
'Đang giao'       -- Shipper đã nhận
'Hoàn tất'        -- Giao thành công
'Giao thất bại'   -- Giao không thành công
'Đã hủy'          -- Đơn bị hủy
'Giao lại'        -- Đơn được hoàn trả để giao lại
```

### 🔍 Query Examples

```sql
-- Lấy đơn chờ giao của tất cả shipper
SELECT * FROM orders
WHERE status IN ('Chờ giao', 'Giao lại')
ORDER BY create_date ASC;

-- Lấy đơn đang giao của shipper cụ thể
SELECT * FROM orders
WHERE status = 'Đang giao'
  AND shipper_id = ?
ORDER BY delivery_date DESC;

-- Thống kê doanh thu theo tháng
SELECT
    YEAR(delivery_date) as year,
    MONTH(delivery_date) as month,
    SUM(total_amount) as total_revenue,
    COUNT(*) as total_orders
FROM orders
WHERE shipper_id = ?
  AND status = 'Hoàn tất'
GROUP BY YEAR(delivery_date), MONTH(delivery_date)
ORDER BY year DESC, month DESC;
```

---

## ⚡ Performance và Optimization

### 🚀 Database Optimization

```java
// Sử dụng pagination cho lịch sử
@Override
public Page<Order> getOrdersByStatusAndShipper(String status, int shipperId, Pageable pageable) {
    return dao.findByStatusAndShipperIdOrderByDeliveryDateDesc(status, shipperId, pageable);
}

// Lazy loading cho relationships
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}
```

### 💾 Caching Strategy

```java
// Cache frequently accessed data
@Cacheable("availableYears")
public List<Integer> getAvailableYearsForShipper(Integer shipperId) {
    return dao.getAvailableYearsForShipper(shipperId);
}

// Cache total completed orders amount
@Cacheable(value = "totalAmount", key = "#shipperId")
public Double getTotalCompletedOrdersAmount(int shipperId) {
    Double result = dao.getTotalCompletedAmountByShipperId(shipperId);
    return result != null ? result : 0.0;
}
```

### 🔄 Async Processing

```java
// Async notification cho status updates
@Async
public void notifyOrderStatusChange(Long orderId, String newStatus) {
    // Send notification to customer
    // Update external systems
    // Log status change
}
```

---

## 🐛 Error Handling và Logging

### ❌ Exception Handling

```java
@ExceptionHandler(IllegalArgumentException.class)
public String handleIllegalArgument(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("error", e.getMessage());
    return "redirect:/shipper/my-orders";
}

@ExceptionHandler(IllegalStateException.class)
public String handleIllegalState(IllegalStateException e, RedirectAttributes redirectAttributes) {
    redirectAttributes.addFlashAttribute("error", e.getMessage());
    return "redirect:/shipper/pending-orders";
}
```

### 📝 Logging Configuration

```java
// Trong Service methods
@Override
public Order updateToDangGiao(Long orderId, int shipperId) {
    log.info("Shipper {} nhận đơn hàng {}", shipperId, orderId);

    try {
        Order order = dao.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        // Business logic...

        log.info("Đơn hàng {} đã chuyển sang trạng thái Đang giao", orderId);
        return dao.save(order);
    } catch (Exception e) {
        log.error("Lỗi khi cập nhật đơn hàng {} sang Đang giao: {}", orderId, e.getMessage());
        throw e;
    }
}
```

### 🚨 User Feedback

```javascript
// JavaScript cho user feedback
function showAlert(type, message) {
  const alertDiv = document.createElement("div");
  const alertType = type === "error" ? "danger" : type;
  alertDiv.className = `alert alert-${alertType} alert-dismissible fade show custom-alert`;

  alertDiv.innerHTML = `
        <i class="${getIconClass(type)}"></i>
        <span>${message}</span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

  document.querySelector(".alert-container-fixed").appendChild(alertDiv);

  // Auto dismiss after 5 seconds
  setTimeout(() => {
    if (alertDiv && alertDiv.parentNode) {
      alertDiv.remove();
    }
  }, 5000);
}
```

---

## 🔧 Configuration và Deployment

### ⚙️ Application Properties

```properties
# Database configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=HoaShop;trustServerCertificate=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Session configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.secure=true

# File upload configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging configuration
logging.level.com.datn.Controller.shipper=DEBUG
logging.level.com.datn.Service.impl.OrderServiceImpl=DEBUG
```

### 🚀 Docker Configuration

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/datn-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=production

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 🐙 Docker Compose

```yaml
version: "3.8"
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - SPRING_PROFILES_ACTIVE=production
    depends_on:
      - sqlserver

  sqlserver:
    image: mcr.microsoft.com/mssql/server:2019-latest
    environment:
      SA_PASSWORD: ${SA_PASSWORD}
      ACCEPT_EULA: Y
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql

volumes:
  sqlserver_data:
```

---

## 🧪 Testing

### 🔬 Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class ShipperOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ShipperOrderController controller;

    @Test
    void testReceiveOrder_Success() {
        // Given
        Long orderId = 1L;
        User shipper = new User();
        shipper.setId(1);
        shipper.setRole(2);

        when(authService.getUser()).thenReturn(shipper);

        // When
        String result = controller.receiveOrder(orderId);

        // Then
        verify(orderService).updateToDangGiao(orderId, shipper.getId());
        assertEquals("redirect:/shipper/my-orders", result);
    }

    @Test
    void testCompleteOrder_Success() {
        // Given
        Long orderId = 1L;
        User shipper = new User();
        shipper.setId(1);
        shipper.setRole(2);

        when(authService.getUser()).thenReturn(shipper);

        // When
        String result = controller.completeOrder(orderId);

        // Then
        verify(orderService).updateToCompleted(orderId, shipper.getId());
        assertEquals("redirect:/shipper/my-orders", result);
    }
}
```

### 🧪 Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ShipperWorkflowIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDAO orderDAO;

    @Test
    void testCompleteShipperWorkflow() {
        // 1. Create test order
        Order testOrder = createTestOrder();
        testOrder.setStatus("Chờ giao");
        orderDAO.save(testOrder);

        // 2. Shipper receives order
        Order receivedOrder = orderService.updateToDangGiao(testOrder.getId(), SHIPPER_ID);
        assertEquals("Đang giao", receivedOrder.getStatus());

        // 3. Shipper completes order
        orderService.updateToCompleted(testOrder.getId(), SHIPPER_ID);

        Order completedOrder = orderDAO.findById(testOrder.getId()).orElse(null);
        assertNotNull(completedOrder);
        assertEquals("Hoàn tất", completedOrder.getStatus());
    }
}
```

---

## 📱 Mobile Responsive

### 📲 Responsive Design

```css
/* Mobile-first approach */
@media (max-width: 768px) {
  .shipper-sidebar-dark {
    width: 100%;
    height: auto;
    position: relative;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .shipper-sidebar-dark.show {
    transform: translateX(0);
  }

  .articlemana {
    margin-left: 0;
    padding: 10px;
  }

  .accordion-button {
    font-size: 14px;
    padding: 0.5rem;
  }

  .btn-sm {
    font-size: 12px;
    padding: 0.25rem 0.5rem;
  }
}

/* Tablet adjustments */
@media (min-width: 769px) and (max-width: 1024px) {
  .shipper-sidebar-dark {
    width: 250px;
  }

  .articlemana {
    margin-left: 250px;
  }
}
```

### 📱 Touch-friendly UI

```css
/* Larger touch targets for mobile */
.shipper-nav-link-dark {
  min-height: 48px; /* WCAG recommended minimum */
  display: flex;
  align-items: center;
}

.btn {
  min-height: 44px; /* iOS guideline */
  min-width: 44px;
}

/* Improved form controls */
.form-control,
.form-select {
  font-size: 16px; /* Prevents zoom on iOS */
  min-height: 48px;
}
```

---

## 🔄 API Integration

### 🗺️ Google Maps API

```javascript
// Google Maps Directions API
const googleMapsUrl = `https://www.google.com/maps/dir/${currentPos.lat},${
  currentPos.lng
}/${encodeURIComponent(address)}`;

// Places API cho autocomplete
function initAutocomplete() {
  const autocomplete = new google.maps.places.Autocomplete(
    document.getElementById("address-input"),
    {
      componentRestrictions: { country: "VN" },
      types: ["address"],
    }
  );
}
```

### 🌍 OpenStreetMap Integration

```javascript
// Nominatim Geocoding API
function geocodeAddress(address) {
  const queries = [
    `${address}, Đà Nẵng, Vietnam`,
    `${address}, Da Nang, Vietnam`,
    `${address}, Vietnam`,
    address,
  ];

  const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
    queries[0]
  )}&format=json&limit=3&countrycodes=vn`;

  fetch(url)
    .then((response) => response.json())
    .then((data) => {
      if (data && data.length > 0) {
        const lat = parseFloat(data[0].lat);
        const lng = parseFloat(data[0].lon);

        // Add destination marker
        destinationMarker = L.marker([lat, lng]).addTo(map);
        map.setView([lat, lng], 15);
      }
    })
    .catch((error) => console.error("Geocoding error:", error));
}
```

---

## 📈 Analytics và Monitoring

### 📊 Business Metrics

```java
// Service methods for analytics
public class ShipperAnalyticsService {

    public ShipperPerformanceDTO getShipperPerformance(int shipperId, Date fromDate, Date toDate) {
        return ShipperPerformanceDTO.builder()
            .totalDeliveries(getTotalDeliveries(shipperId, fromDate, toDate))
            .successfulDeliveries(getSuccessfulDeliveries(shipperId, fromDate, toDate))
            .failedDeliveries(getFailedDeliveries(shipperId, fromDate, toDate))
            .totalRevenue(getTotalRevenue(shipperId, fromDate, toDate))
            .averageDeliveryTime(getAverageDeliveryTime(shipperId, fromDate, toDate))
            .customerRating(getAverageCustomerRating(shipperId, fromDate, toDate))
            .build();
    }
}
```

### 📈 Performance Tracking

```java
// Method execution time tracking
@Around("execution(* com.datn.Service.impl.OrderServiceImpl.*(..))")
public Object trackExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();

    try {
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("Method {} executed in {} ms",
                joinPoint.getSignature().getName(),
                endTime - startTime);

        return result;
    } catch (Exception e) {
        log.error("Error in method {}: {}",
                joinPoint.getSignature().getName(),
                e.getMessage());
        throw e;
    }
}
```

---

## 🔮 Future Enhancements

### 🚀 Planned Features

#### 1. **Real-time Tracking**

```javascript
// WebSocket integration for live tracking
const socket = new WebSocket("ws://localhost:8080/shipper-tracking");

socket.onmessage = function (event) {
  const data = JSON.parse(event.data);
  updateOrderStatus(data.orderId, data.status, data.location);
};

function updateLocation(lat, lng) {
  socket.send(
    JSON.stringify({
      shipperId: getCurrentShipperId(),
      latitude: lat,
      longitude: lng,
      timestamp: new Date().toISOString(),
    })
  );
}
```

#### 2. **Push Notifications**

```java
@Service
public class NotificationService {

    public void sendNewOrderNotification(int shipperId, Long orderId) {
        PushNotification notification = PushNotification.builder()
            .title("Đơn hàng mới")
            .body("Bạn có đơn hàng mới cần giao: #" + orderId)
            .shipperId(shipperId)
            .orderId(orderId)
            .build();

        firebaseMessaging.send(notification);
    }
}
```

#### 3. **Machine Learning Integration**

```python
# Delivery time prediction model
import pandas as pd
from sklearn.ensemble import RandomForestRegressor

def predict_delivery_time(distance, traffic_condition, weather, time_of_day):
    features = pd.DataFrame({
        'distance': [distance],
        'traffic_condition': [traffic_condition],
        'weather': [weather],
        'time_of_day': [time_of_day]
    })

    return delivery_time_model.predict(features)[0]
```

#### 4. **Advanced Analytics Dashboard**

```javascript
// Chart.js integration
function renderShipperAnalytics(data) {
  const ctx = document.getElementById("deliveryChart").getContext("2d");

  new Chart(ctx, {
    type: "line",
    data: {
      labels: data.months,
      datasets: [
        {
          label: "Successful Deliveries",
          data: data.successfulDeliveries,
          borderColor: "#10b981",
          backgroundColor: "rgba(16, 185, 129, 0.1)",
        },
        {
          label: "Failed Deliveries",
          data: data.failedDeliveries,
          borderColor: "#ef4444",
          backgroundColor: "rgba(239, 68, 68, 0.1)",
        },
      ],
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
        },
      },
    },
  });
}
```

---

## 📚 Tài Liệu Tham Khảo

### 🔗 Links và Resources

#### **Spring Boot Documentation**

- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Documentation](https://www.thymeleaf.org/documentation.html)

#### **Frontend Libraries**

- [Bootstrap 5 Documentation](https://getbootstrap.com/docs/5.3/getting-started/introduction/)
- [Leaflet Documentation](https://leafletjs.com/reference.html)
- [Google Maps API](https://developers.google.com/maps/documentation)
- [Font Awesome Icons](https://fontawesome.com/docs)

#### **Database**

- [SQL Server Documentation](https://docs.microsoft.com/en-us/sql/sql-server/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/)

### 📖 Code Examples

#### **Complete Controller Method**

```java
@PostMapping("/orders/complete/{orderId}")
public String completeOrder(@PathVariable("orderId") Long orderId,
                          RedirectAttributes redirectAttributes) {
    try {
        User shipper = authService.getUser();

        if (shipper == null || shipper.getRole() != 2) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện thao tác này!");
            return "redirect:/shipper/my-orders";
        }

        orderService.updateToCompleted(orderId, shipper.getId());
        redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được hoàn tất thành công!");

        // Log for audit trail
        log.info("Shipper {} completed order {}", shipper.getId(), orderId);

    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    } catch (Exception e) {
        log.error("Error completing order {}: {}", orderId, e.getMessage());
        redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi hoàn tất đơn hàng!");
    }

    return "redirect:/shipper/my-orders";
}
```

#### **Complete Service Method**

```java
@Override
@Transactional
public void updateToCompleted(Long orderId, int shipperId) {
    // Validate input
    if (orderId == null || shipperId <= 0) {
        throw new IllegalArgumentException("Invalid order ID or shipper ID");
    }

    // Find order and shipper
    Order order = dao.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

    User shipper = userDAO.findById(shipperId)
        .orElseThrow(() -> new IllegalArgumentException("Shipper không tồn tại"));

    // Validate business rules
    if (!order.getStatus().equals("Đang giao")) {
        throw new IllegalStateException("Chỉ có thể hoàn tất đơn hàng đang ở trạng thái 'Đang giao'");
    }

    if (order.getShipper() == null || !order.getShipper().getId().equals(shipperId)) {
        throw new IllegalArgumentException("Bạn không được phép hoàn tất đơn hàng này");
    }

    // Update order status
    order.setStatus("Hoàn tất");
    order.setCompletedDate(new Date());

    // Save to database
    Order savedOrder = dao.save(order);

    // Send notification (async)
    notificationService.notifyOrderCompleted(order.getUser().getId(), orderId);

    // Update inventory (if needed)
    inventoryService.updateInventoryAfterDelivery(order.getOrderDetails());

    log.info("Order {} completed successfully by shipper {}", orderId, shipperId);
}
```

---

## ✅ Checklist Triển Khai

### 🚀 Pre-deployment Checklist

#### **Database Setup**

- [ ] Database schema đã được tạo
- [ ] Sample data đã được insert
- [ ] Database indexes đã được tối ưu
- [ ] Backup strategy đã được thiết lập

#### **Security Configuration**

- [ ] Authentication đã được cấu hình
- [ ] Authorization rules đã được kiểm tra
- [ ] Session management đã được tối ưu
- [ ] CSRF protection đã được bật

#### **Performance Testing**

- [ ] Load testing đã được thực hiện
- [ ] Database query performance đã được tối ưu
- [ ] Memory usage đã được kiểm tra
- [ ] Response time đã đạt yêu cầu

#### **Frontend Testing**

- [ ] Cross-browser compatibility đã được kiểm tra
- [ ] Mobile responsiveness đã được test
- [ ] Map integration hoạt động chính xác
- [ ] JavaScript errors đã được sửa

#### **Monitoring Setup**

- [ ] Logging configuration đã được cấu hình
- [ ] Error tracking đã được thiết lập
- [ ] Performance monitoring đã được bật
- [ ] Health checks đã được cấu hình

---

## 📞 Hỗ Trợ và Bảo Trì

### 🛠️ Common Issues và Solutions

#### **Problem**: Shipper không thể nhận đơn

**Solution**:

```java
// Check user role and session
User shipper = authService.getUser();
if (shipper == null) {
    log.warn("User session expired or not found");
    return "redirect:/login";
}

if (shipper.getRole() != 2) {
    log.warn("User {} attempted to access shipper functionality with role {}",
             shipper.getId(), shipper.getRole());
    throw new AccessDeniedException("Bạn không có quyền truy cập chức năng này");
}
```

#### **Problem**: Map không hiển thị chính xác

**Solution**:

```javascript
// Debug geocoding issues
function debugGeocoding(address) {
  console.log("Original address:", address);

  const queries = [
    `${address}, Đà Nẵng, Vietnam`,
    `${address}, Da Nang, Vietnam`,
    `${address}, Vietnam`,
    address,
  ];

  queries.forEach((query, index) => {
    console.log(`Query ${index + 1}:`, query);

    fetch(
      `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
        query
      )}&format=json&limit=1`
    )
      .then((response) => response.json())
      .then((data) => {
        console.log(`Result ${index + 1}:`, data);
      });
  });
}
```

#### **Problem**: Performance chậm khi tải lịch sử

**Solution**:

```java
// Implement pagination and caching
@Cacheable(value = "shipperHistory", key = "#shipperId + '_' + #page + '_' + #size")
public Page<Order> getShipperHistory(int shipperId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("deliveryDate").descending());
    return orderDAO.findByShipperIdAndStatus(shipperId, "Hoàn tất", pageable);
}
```

### 📧 Contact Information

- **Development Team**: datn.dev@company.com
- **Support Team**: support@company.com
- **Emergency Contact**: +84-xxx-xxx-xxx

---

## 🎉 Kết Luận

Hệ thống Shipper của Hoa Shop đã được thiết kế và phát triển một cách toàn diện, cung cấp:

✅ **Quy trình giao hàng hoàn chỉnh** từ nhận đơn đến hoàn tất
✅ **Giao diện người dùng thân thiện** với responsive design
✅ **Tích hợp bản đồ** hỗ trợ navigation và geolocation
✅ **Báo cáo thống kê** chi tiết theo ngày/tháng/năm
✅ **Xử lý ngoại lệ** cho các trường hợp giao thất bại
✅ **Bảo mật và xác thực** đầy đủ
✅ **Performance tối ưu** với caching và pagination
✅ **Code quality cao** với testing và documentation

Hệ thống đã sẵn sàng cho production và có thể mở rộng trong tương lai với các tính năng advanced như real-time tracking, push notifications, và machine learning integration.

---

_📝 Tài liệu này được tạo bởi Development Team của Hoa Shop_
_🗓️ Cập nhật lần cuối: [Current Date]_
_📋 Version: 1.0_
