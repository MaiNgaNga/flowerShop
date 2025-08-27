# 🗺️ Tài Liệu Hệ Thống Chỉ Đường Bản Đồ - Shipper Module

## 📌 Tổng Quan

Hệ thống chỉ đường bằng bản đồ trong module Shipper được thiết kế để hỗ trợ shipper trong việc điều hướng đến địa chỉ giao hàng một cách chính xác và hiệu quả. Hệ thống tích hợp nhiều công nghệ bản đồ tiên tiến bao gồm **Leaflet**, **OpenStreetMap**, **Google Maps API**, và **HTML5 Geolocation**.

### 🎯 Mục Tiêu Chính

- **Hiển thị bản đồ tương tác**: Leaflet map với OpenStreetMap tiles
- **Định vị chính xác**: Geocoding địa chỉ giao hàng
- **Vị trí thời gian thực**: HTML5 Geolocation cho vị trí shipper
- **Điều hướng thông minh**: Tích hợp Google Maps Navigation
- **Trải nghiệm người dùng**: Responsive design và error handling

---

## 🏗️ Kiến Trúc Hệ Thống

### 📂 Cấu Trúc Components

```
Hệ Thống Điều Hướng Bản Đồ
├── Thành Phần Giao Diện
│   ├── Giao Diện Modal (Bootstrap 5)
│   ├── Container Bản Đồ Leaflet
│   ├── Các Nút Điều Khiển
│   └── Chỉ Báo Trạng Thái
├── Các Module JavaScript
│   ├── Khởi Tạo Bản Đồ
│   ├── Dịch Vụ Geocoding
│   ├── Dịch Vụ Định Vị
│   ├── Tính Toán Tuyến Đường
│   └── Tích Hợp Điều Hướng
└── API Bên Ngoài
    ├── OpenStreetMap (Tiles)
    ├── Nominatim (Geocoding)
    ├── Google Maps (Điều Hướng)
    └── HTML5 Geolocation
```

### 🔧 Ngăn Xếp Công Nghệ

#### **Thư Viện Giao Diện**

```html
<!-- Leaflet CSS -->
<link
  rel="stylesheet"
  href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
  integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
  crossorigin=""
/>

<!-- Leaflet JavaScript -->
<script
  src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
  integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
  crossorigin=""
></script>

<!-- Leaflet Routing Machine -->
<script src="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.js"></script>
<link
  rel="stylesheet"
  href="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.css"
/>
```

#### **Các Phụ Thuộc Cốt Lõi**

- **Leaflet v1.9.4**: Bản đồ tương tác
- **Bootstrap 5**: Modal và giao diện đáp ứng
- **OpenStreetMap**: Tiles bản đồ và dịch vụ geocoding
- **Google Maps API**: Dịch vụ điều hướng
- **HTML5 Geolocation API**: Định vị thời gian thực

---

## 🚀 Quy Trình Hoạt Động (Workflow)

### 📊 Sơ Đồ Luồng Xử Lý

```
[Shipper nhấn "Mở bản đồ"]
    ↓
[Trích xuất địa chỉ từ data-attribute]
    ↓
[Hiển thị Bootstrap Modal]
    ↓
[Khởi tạo Leaflet Map]
    ↓
[Geocode địa chỉ giao hàng] → [Thêm marker đích đến]
    ↓
[Lấy vị trí hiện tại] → [Thêm marker vị trí hiện tại]
    ↓
[Tính toán tuyến đường] → [Hiển thị thông tin tuyến đường]
    ↓
[Tùy chọn điều hướng khả dụng]
```

### 🔍 Chi Tiết Từng Bước

#### 1️⃣ **Khởi Tạo Modal Bản Đồ**

**Cấu Trúc HTML**:

```html
<button
  type="button"
  class="btn btn-info btn-sm me-2"
  onclick="openMapModal(this)"
  th:attr="data-address=${order.address}, data-order-id=${order.id}"
>
  <i class="bi bi-geo-alt"></i> Mở bản đồ chỉ đường
</button>
```

**Hàm JavaScript**:

```javascript
function openMapModal(button) {
  const address = button.getAttribute("data-address");
  const orderId = button.getAttribute("data-order-id");

  currentOrderAddress = address;
  document.getElementById("deliveryAddress").textContent = address;

  // Reset modal state
  document.getElementById("mapLoading").style.display = "flex";
  document.getElementById("map").style.display = "none";
  document.getElementById("routeInfo").style.display = "none";

  // Show modal
  const modal = new bootstrap.Modal(document.getElementById("mapModal"));
  modal.show();

  // Initialize map after modal is shown
  setTimeout(() => {
    initializeMap(address);
  }, 500);
}
```

**Tính Năng**:

- ✅ Trích xuất địa chỉ từ `data-attribute`
- ✅ Đặt lại trạng thái modal
- ✅ Quản lý trạng thái loading
- ✅ Khởi tạo trễ cho hoạt ảnh modal

#### 2️⃣ **Khởi Tạo Bản Đồ**

**Hàm Cốt Lõi**:

```javascript
function initializeMap(address) {
  // Default location (Vietnam center)
  const defaultLat = 16.0544;
  const defaultLng = 108.2022;

  // Initialize map
  if (map) {
    map.remove(); // Clean up existing map
  }

  map = L.map("map").setView([defaultLat, defaultLng], 13);

  // Add OpenStreetMap tiles
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap contributors",
  }).addTo(map);

  // Hide loading, show map
  document.getElementById("mapLoading").style.display = "none";
  document.getElementById("map").style.display = "block";

  // Resize map to fit container
  setTimeout(() => {
    map.invalidateSize();
  }, 100);

  // Geocode the delivery address
  geocodeAddress(address);

  // Try to get current location
  getCurrentLocationOnMap();
}
```

**Chi Tiết Kỹ Thuật**:

- **Nhà Cung Cấp Bản Đồ**: OpenStreetMap tiles
- **Góc Nhìn Ban Đầu**: Tọa độ trung tâm Việt Nam
- **Mức Zoom**: 13 (cấp thành phố)
- **Dọn Dẹp**: Xóa các instance bản đồ hiện có
- **Resize Container**: Xử lý thay đổi kích thước modal

#### 3️⃣ **Dịch Vụ Geocoding**

**Geocoding Đa Chiến Lược**:

```javascript
function geocodeAddress(address) {
  // Thử nhiều phương pháp geocoding để tăng độ chính xác
  const queries = [
    `${address}, Đà Nẵng, Vietnam`, // Đầy đủ nhất
    `${address}, Da Nang, Vietnam`, // Tiếng Anh
    `${address}, Vietnam`, // Cơ bản
    address, // Chỉ địa chỉ gốc
  ];

  // Thử từng query cho đến khi tìm được kết quả tốt
  tryGeocode(queries, 0);
}

function tryGeocode(queries, index) {
  if (index >= queries.length) {
    alert(
      "Không thể tìm thấy địa chỉ trên bản đồ. Vui lòng kiểm tra lại địa chỉ."
    );
    return;
  }

  const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(
    queries[index]
  )}&format=json&limit=3&countrycodes=vn`;

  console.log(`🔍 Tìm kiếm địa chỉ (lần ${index + 1}): ${queries[index]}`);

  fetch(url)
    .then((response) => response.json())
    .then((data) => {
      console.log(`📍 Kết quả geocoding:`, data);

      if (data && data.length > 0) {
        // Tìm kết quả tốt nhất (có chứa "Đà Nẵng" hoặc "Da Nang")
        let bestResult =
          data.find(
            (item) =>
              item.display_name.includes("Đà Nẵng") ||
              item.display_name.includes("Da Nang") ||
              item.display_name.includes("Hòa Khánh")
          ) || data[0];

        const lat = parseFloat(bestResult.lat);
        const lng = parseFloat(bestResult.lon);

        console.log(`✅ Địa chỉ tìm được: ${bestResult.display_name}`);
        console.log(`📍 Tọa độ: ${lat}, ${lng}`);

        // Add destination marker
        addDestinationMarker(lat, lng, bestResult);
      } else {
        // Thử query tiếp theo
        tryGeocode(queries, index + 1);
      }
    })
    .catch((error) => {
      console.error("Geocoding error:", error);
      // Thử query tiếp theo
      tryGeocode(queries, index + 1);
    });
}
```

**Tính Năng Geocoding**:

- **Chiến Lược Đa Tầng**: 4 mức độ chi tiết địa chỉ
- **Tập Trung Việt Nam**: Bộ lọc mã quốc gia
- **Chọn Lựa Kết Quả Thông Minh**: Ưu tiên cho các vị trí ở Đà Nẵng
- **Cơ Chế Dự Phòng**: Thử truy vấn tiếp theo nếu hiện tại thất bại
- **Xử Lý Lỗi**: Suy giảm ưa nhã

#### 4️⃣ **Marker Đích Đến**

```javascript
function addDestinationMarker(lat, lng, geocodeResult) {
  // Remove existing destination marker
  if (destinationMarker) {
    map.removeLayer(destinationMarker);
  }

  destinationMarker = L.marker([lat, lng], {
    icon: L.icon({
      iconUrl:
        "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",
      shadowUrl:
        "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png",
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41],
    }),
  }).addTo(map);

  destinationMarker
    .bindPopup(
      `<strong>Địa chỉ giao hàng:</strong><br>${currentOrderAddress}<br><br>
         <small><strong>Kết quả tìm được:</strong><br>${geocodeResult.display_name}</small>`
    )
    .openPopup();

  // Center map on destination
  map.setView([lat, lng], 15);
}
```

**Tính Năng Marker**:

- **Icon Tùy Chỉnh**: Marker đỏ cho đích đến
- **Hiệu Ứng Đổ Bóng**: Diện mạo chuyên nghiệp
- **Thông Tin Popup**: Địa chỉ gốc + kết quả geocoded
- **Tự Động Căn Giữa**: Bản đồ tập trung vào đích đến

#### 5️⃣ **Dịch Vụ Vị Trí Hiện Tại**

```javascript
function getCurrentLocationOnMap() {
  if (navigator.geolocation) {
    // Hiển thị loading
    document.getElementById("getCurrentLocation").innerHTML =
      '<i class="spinner-border spinner-border-sm me-2"></i>Đang lấy vị trí...';
    document.getElementById("getCurrentLocation").disabled = true;

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        const accuracy = position.coords.accuracy;

        console.log(
          `📍 Vị trí hiện tại: ${lat}, ${lng} (độ chính xác: ±${Math.round(
            accuracy
          )}m)`
        );

        // Add current location marker
        addCurrentLocationMarker(lat, lng, accuracy);

        // Reset button
        resetLocationButton();

        // If both markers exist, create route
        if (destinationMarker) {
          createRoute();
        }
      },
      (error) => {
        console.error("Geolocation error:", error);
        resetLocationButton();
        handleGeolocationError(error);
      },
      {
        enableHighAccuracy: true, // Yêu cầu độ chính xác cao
        timeout: 10000, // Timeout 10 giây
        maximumAge: 300000, // Cache 5 phút
      }
    );
  } else {
    alert("Trình duyệt không hỗ trợ Geolocation API.");
  }
}
```

**Cấu Hình Geolocation**:

- **Độ Chính Xác Cao**: `enableHighAccuracy: true`
- **Timeout**: Chờ tối đa 10 giây
- **Cache**: Thời gian tối đa 5 phút
- **Xử Lý Lỗi**: Thông báo lỗi toàn diện

#### 6️⃣ **Marker Vị Trí Hiện Tại**

```javascript
function addCurrentLocationMarker(lat, lng, accuracy) {
  // Remove existing current location marker
  if (currentLocationMarker) {
    map.removeLayer(currentLocationMarker);
  }

  currentLocationMarker = L.marker([lat, lng], {
    icon: L.icon({
      iconUrl:
        "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png",
      shadowUrl:
        "https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png",
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41],
    }),
  }).addTo(map);

  // Popup với thông tin chi tiết
  const accuracyText =
    accuracy > 100
      ? `⚠️ Độ chính xác thấp: ±${Math.round(accuracy)}m`
      : `✅ Độ chính xác tốt: ±${Math.round(accuracy)}m`;

  currentLocationMarker.bindPopup(
    `<strong>Vị trí hiện tại của bạn</strong><br>
         <small>${accuracyText}</small>`
  );

  // Zoom to current location
  map.setView([lat, lng], 16);
}
```

**Tính Năng**:

- **Marker Xanh**: Phân biệt với đích đến
- **Chỉ Báo Độ Chính Xác**: Phản hồi trực quan về độ chính xác GPS
- **Tự Động Zoom**: Tập trung vào vị trí hiện tại

#### 7️⃣ **Tính Toán Tuyến Đường**

```javascript
function createRoute() {
  if (!currentLocationMarker || !destinationMarker) {
    return;
  }

  // Remove existing route
  if (routeControl) {
    map.removeControl(routeControl);
  }

  const startLatLng = currentLocationMarker.getLatLng();
  const endLatLng = destinationMarker.getLatLng();

  // Tạo đường thẳng đơn giản thay vì routing phức tạp (tránh OSRM demo server)
  const routeLine = L.polyline(
    [
      [startLatLng.lat, startLatLng.lng],
      [endLatLng.lat, endLatLng.lng],
    ],
    {
      color: "#007bff",
      weight: 4,
      opacity: 0.7,
      dashArray: "10, 10", // Đường đứt nét để phân biệt với đường thật
    }
  ).addTo(map);

  // Tính khoảng cách trực tiếp (đường chim bay)
  const distance = startLatLng.distanceTo(endLatLng);
  const distanceKm = (distance / 1000).toFixed(1);

  // Ước lượng thời gian (30km/h trung bình trong thành phố)
  const estimatedTime = Math.round((distance / 1000) * 2); // phút

  document.getElementById("routeDistance").textContent =
    distanceKm + " km (đường chim bay)";
  document.getElementById("routeDuration").textContent =
    "~" + estimatedTime + " phút (ước lượng)";
  document.getElementById("routeInfo").style.display = "block";

  // Zoom để hiển thị cả 2 điểm
  const group = new L.featureGroup([currentLocationMarker, destinationMarker]);
  map.fitBounds(group.getBounds().pad(0.1));

  // Lưu reference để có thể xóa sau
  routeControl = {
    remove: () => map.removeLayer(routeLine),
    addTo: () => routeLine.addTo(map),
  };
}
```

**Tính Năng Tuyến Đường**:

- **Đường Thẳng**: Tính toán khoảng cách đường thẳng
- **Kiểu Dáng Trực Quan**: Đường màu xanh gạch ngang
- **Tính Toán Khoảng Cách**: Công thức Haversine
- **Ước Lượng Thời Gian**: Tốc độ trung bình 30 km/h trong thành phố
- **Tự Động Vừa Vặn**: Giới hạn bản đồ để hiển thị cả hai điểm

#### 8️⃣ **Điều Hướng Google Maps**

```javascript
document
  .getElementById("startNavigation")
  .addEventListener("click", function () {
    if (currentOrderAddress && currentOrderAddress.trim()) {
      let googleMapsUrl;

      // Nếu có vị trí hiện tại, truyền cả điểm xuất phát và đích đến
      if (currentLocationMarker) {
        const currentPos = currentLocationMarker.getLatLng();
        googleMapsUrl = `https://www.google.com/maps/dir/${currentPos.lat},${
          currentPos.lng
        }/${encodeURIComponent(currentOrderAddress)}`;
        console.log(
          `🗺️ Mở Google Maps với vị trí hiện tại: ${currentPos.lat}, ${currentPos.lng}`
        );
      } else {
        // Chỉ có địa chỉ đích, Google Maps sẽ dùng vị trí hiện tại của thiết bị
        googleMapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(
          currentOrderAddress
        )}`;
        console.log(`🗺️ Mở Google Maps chỉ với địa chỉ đích`);
      }

      console.log(`📍 Địa chỉ đích: ${currentOrderAddress}`);
      console.log(`🔗 URL: ${googleMapsUrl}`);

      window.open(googleMapsUrl, "_blank");
    } else {
      alert("Không tìm thấy địa chỉ giao hàng. Vui lòng thử lại.");
    }
  });
```

**Tính Năng Điều Hướng**:

- **URL Có Điều Kiện**: URL khác nhau dựa trên tính khả dụng của vị trí
- **Mã Hóa URL**: Xử lý đúng các địa chỉ tiếng Việt
- **Tab Mới**: Mở trong tab trình duyệt riêng biệt
- **Xử Lý Lỗi**: Cảnh báo nếu không tìm thấy địa chỉ

---

## 🎨 Thiết Kế Giao Diện Người Dùng

### 🖼️ Cấu Trúc Modal

```html
<!-- Map Modal -->
<div
  class="modal fade"
  id="mapModal"
  tabindex="-1"
  aria-labelledby="mapModalLabel"
  aria-hidden="true"
>
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="mapModalLabel">
          <i class="bi bi-geo-alt"></i> Chỉ đường giao hàng
        </h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="modal"
        ></button>
      </div>
      <div class="modal-body">
        <!-- Route Information Card -->
        <div class="route-info" id="routeInfo" style="display: none">
          <div class="row">
            <div class="col-md-6">
              <strong>Khoảng cách:</strong> <span id="routeDistance">--</span>
            </div>
            <div class="col-md-6">
              <strong>Thời gian:</strong> <span id="routeDuration">--</span>
            </div>
          </div>
          <div class="mt-2">
            <strong>Địa chỉ giao hàng:</strong>
            <span id="deliveryAddress">--</span>
          </div>
        </div>

        <!-- Accuracy Notice -->
        <div class="alert alert-info alert-sm p-2 mb-3">
          <i class="bi bi-info-circle me-1"></i>
          <strong>Lưu ý:</strong> Vị trí trên máy tính có thể không chính xác
          bằng điện thoại. Nếu vị trí hiển thị sai, hãy sử dụng nút
          <strong>"Bắt đầu điều hướng"</strong> để mở Google Maps với địa chỉ
          chính xác.
        </div>

        <!-- Loading Spinner -->
        <div class="loading-spinner" id="mapLoading">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Đang tải bản đồ...</span>
          </div>
        </div>

        <!-- Map Container -->
        <div id="map" style="display: none"></div>
      </div>

      <!-- Modal Footer with Controls -->
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
          Đóng
        </button>
        <button type="button" class="btn btn-primary" id="getCurrentLocation">
          <i class="bi bi-crosshair"></i> Vị trí hiện tại
        </button>
        <button type="button" class="btn btn-success" id="startNavigation">
          <i class="bi bi-navigation"></i> Bắt đầu điều hướng
        </button>
      </div>
    </div>
  </div>
</div>
```

### 🎭 Kiểu Dáng CSS

```css
/* Map Modal Styles */
#mapModal .modal-dialog {
  max-width: 90%;
  width: 900px;
}

#map {
  height: 400px;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid #dee2e6;
}

.route-info {
  background-color: #f8f9fa;
  border-radius: 0.5rem;
  padding: 15px;
  margin-bottom: 15px;
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  #mapModal .modal-dialog {
    max-width: 95%;
    margin: 1rem;
  }

  #map {
    height: 300px;
  }

  .modal-footer {
    flex-direction: column;
    gap: 0.5rem;
  }

  .modal-footer .btn {
    width: 100%;
  }
}
```

### 🎯 Các Phần Tử Tương Tác

#### **Các Nút Điều Khiển**

1. **Vị trí hiện tại**: Lấy vị trí GPS hiện tại
2. **Bắt đầu điều hướng**: Mở Google Maps
3. **Đóng**: Đóng modal và dọn dẹp tài nguyên

#### **Chỉ Báo Trực Quan**

- **Spinner Loading**: Trong quá trình khởi tạo bản đồ
- **Thông Tin Tuyến Đường**: Ước lượng khoảng cách và thời gian
- **Cảnh Báo Độ Chính Xác**: Phản hồi về độ chính xác GPS
- **Popup Marker**: Chi tiết địa chỉ và vị trí

---

## ⚡ Tối Ưu Hiệu Suất

### 🚀 Chiến Lược Loading

```javascript
// Lazy loading cho map libraries
function loadMapLibraries() {
  return new Promise((resolve, reject) => {
    if (typeof L !== "undefined") {
      resolve();
      return;
    }

    const script = document.createElement("script");
    script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });
}

// Initialize only when needed
async function openMapModal(button) {
  try {
    await loadMapLibraries();
    // Proceed with map initialization
  } catch (error) {
    console.error("Failed to load map libraries:", error);
    alert("Không thể tải bản đồ. Vui lòng thử lại.");
  }
}
```

### 💾 Chiến Lược Caching

```javascript
// Cache geocoding results
const geocodeCache = new Map();

function cachedGeocode(address) {
  if (geocodeCache.has(address)) {
    console.log("📦 Using cached geocode result for:", address);
    return Promise.resolve(geocodeCache.get(address));
  }

  return fetch(nominatimUrl)
    .then((response) => response.json())
    .then((data) => {
      geocodeCache.set(address, data);
      return data;
    });
}
```

### 🔄 Quản Lý Tài Nguyên

```javascript
// Clean up when modal is closed
document
  .getElementById("mapModal")
  .addEventListener("hidden.bs.modal", function () {
    if (map) {
      map.remove();
      map = null;
    }
    currentLocationMarker = null;
    destinationMarker = null;
    routeControl = null;

    // Clear any ongoing geolocation requests
    if (watchId) {
      navigator.geolocation.clearWatch(watchId);
      watchId = null;
    }
  });
```

---

## 🔐 Bảo Mật và Quyền Riêng Tư

### 🛡️ Quyền Riêng Tư Geolocation

```javascript
// Request location with user consent
function requestLocationPermission() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Geolocation not supported"));
      return;
    }

    // Check permission status
    if (navigator.permissions) {
      navigator.permissions.query({ name: "geolocation" }).then((result) => {
        if (result.state === "denied") {
          reject(new Error("Geolocation permission denied"));
        } else {
          resolve();
        }
      });
    } else {
      resolve(); // Fallback for older browsers
    }
  });
}
```

### 🔒 Bảo Vệ Dữ Liệu

- **Không Lưu Trữ**: Dữ liệu vị trí không được lưu vào server
- **Chỉ Phiên**: Tọa độ chỉ tồn tại trong phiên bản đồ
- **Kiểm Soát Người Dùng**: Yêu cầu vị trí thủ công, không tự động
- **Yêu Cầu HTTPS**: Geolocation API yêu cầu ngữ cảnh bảo mật

### 🚨 Xử Lý Lỗi

```javascript
function handleGeolocationError(error) {
  let errorMessage = "Không thể lấy vị trí hiện tại. ";

  switch (error.code) {
    case error.PERMISSION_DENIED:
      errorMessage += "Vui lòng cho phép truy cập vị trí trong trình duyệt.";
      break;
    case error.POSITION_UNAVAILABLE:
      errorMessage += "Vị trí không khả dụng. Hãy thử lại sau.";
      break;
    case error.TIMEOUT:
      errorMessage += "Hết thời gian chờ. Hãy thử lại.";
      break;
    default:
      errorMessage += "Lỗi không xác định.";
      break;
  }

  errorMessage +=
    "\n\nBạn có thể sử dụng tính năng 'Bắt đầu điều hướng' để mở Google Maps với địa chỉ chính xác.";

  alert(errorMessage);
}
```

---

## 📱 Tương Thích Mobile

### 📲 Thiết Kế Đáp Ứng

```css
/* Mobile-first approach */
@media (max-width: 768px) {
  .modal-dialog {
    margin: 0.5rem;
    max-width: calc(100% - 1rem);
  }

  #map {
    height: 280px;
    border-radius: 0.25rem;
  }

  .route-info {
    padding: 10px;
    font-size: 0.9rem;
  }

  .modal-footer {
    padding: 0.75rem;
  }

  .btn {
    font-size: 0.875rem;
    padding: 0.5rem 1rem;
  }
}

/* Touch-friendly controls */
.leaflet-control-container .leaflet-control {
  margin: 10px;
}

.leaflet-touch .leaflet-control-layers,
.leaflet-touch .leaflet-bar {
  border: 2px solid rgba(0, 0, 0, 0.2);
  background-clip: padding-box;
}
```

### 📱 Cử Chỉ Chạm

```javascript
// Hỗ trợ cảm ứng nâng cao cho mobile
function initializeMobileControls() {
  if ("ontouchstart" in window) {
    // Thêm điều khiển bản đồ riêng cho cảm ứng
    map.on("touchstart", function (e) {
      // Xử lý bắt đầu chạm
    });

    map.on("touchmove", function (e) {
      // Xử lý di chuyển chạm
    });

    map.on("touchend", function (e) {
      // Xử lý kết thúc chạm
    });
  }
}
```

### 🔄 Hỗ Trợ Hướng Màn Hình

```javascript
// Xử lý thay đổi hướng thiết bị
window.addEventListener("orientationchange", function () {
  setTimeout(() => {
    if (map) {
      map.invalidateSize();

      // Điều chỉnh lại giới hạn nếu cả hai marker tồn tại
      if (currentLocationMarker && destinationMarker) {
        const group = new L.featureGroup([
          currentLocationMarker,
          destinationMarker,
        ]);
        map.fitBounds(group.getBounds().pad(0.1));
      }
    }
  }, 100);
});
```

---

## 🧪 Chiến Lược Kiểm Thử

### 🔬 Unit Tests

```javascript
// Mock geolocation for testing
const mockGeolocation = {
  getCurrentPosition: jest.fn().mockImplementationOnce((success) =>
    Promise.resolve(
      success({
        coords: {
          latitude: 16.0544,
          longitude: 108.2022,
          accuracy: 10,
        },
      })
    )
  ),
};

Object.defineProperty(global.navigator, "geolocation", {
  value: mockGeolocation,
});

// Test geocoding function
describe("Geocoding Service", () => {
  test("should return coordinates for valid address", async () => {
    const mockResponse = [
      {
        lat: "16.0544",
        lon: "108.2022",
        display_name: "Test Address, Da Nang, Vietnam",
      },
    ];

    global.fetch = jest.fn(() =>
      Promise.resolve({
        json: () => Promise.resolve(mockResponse),
      })
    );

    const result = await geocodeAddress("Test Address");
    expect(result).toBeDefined();
    expect(result.lat).toBe("16.0544");
  });
});
```

### 🧪 Kiểm Thử Tích Hợp

```javascript
// Kiểm thử quy trình bản đồ hoàn chỉnh
describe("Tích Hợp Bản Đồ", () => {
  beforeEach(() => {
    document.body.innerHTML = `
            <div id="mapModal">
                <div id="map"></div>
                <div id="routeInfo"></div>
                <span id="deliveryAddress"></span>
            </div>
        `;
  });

  test("nên khởi tạo bản đồ và hiển thị markers", async () => {
    const testAddress = "Địa Chỉ Kiểm Thử, Đà Nẵng";

    await openMapModal({
      getAttribute: jest.fn((attr) => {
        if (attr === "data-address") return testAddress;
        if (attr === "data-order-id") return "123";
      }),
    });

    expect(map).toBeDefined();
    expect(destinationMarker).toBeDefined();
  });
});
```

### 📊 Kiểm Thử Hiệu Suất

```javascript
// Kiểm thử hiệu suất geocoding
describe("Kiểm Thử Hiệu Suất", () => {
  test("geocoding nên hoàn thành trong 5 giây", async () => {
    const startTime = Date.now();

    await geocodeAddress("Địa Chỉ Kiểm Thử");

    const endTime = Date.now();
    const duration = endTime - startTime;

    expect(duration).toBeLessThan(5000);
  });

  test("khởi tạo bản đồ nên dưới 2 giây", async () => {
    const startTime = Date.now();

    await initializeMap("Địa Chỉ Kiểm Thử");

    const endTime = Date.now();
    const duration = endTime - startTime;

    expect(duration).toBeLessThan(2000);
  });
});
```

---

## 🐛 Hướng Dẫn Khắc Phục Sự Cố

### ❌ Sự Cố Thường Gặp

#### **Sự Cố**: Bản đồ không hiển thị

**Triệu Chứng**: Container trống, lỗi console
**Giải Pháp**:

```javascript
// Check container exists
if (!document.getElementById("map")) {
  console.error("Map container not found");
  return;
}

// Verify Leaflet library loaded
if (typeof L === "undefined") {
  console.error("Leaflet library not loaded");
  await loadMapLibraries();
}

// Force container resize
setTimeout(() => {
  if (map) {
    map.invalidateSize();
  }
}, 500);
```

#### **Sự Cố**: Geolocation không hoạt động

**Triệu Chứng**: Quyền bị từ chối, lỗi timeout
**Giải Pháp**:

```javascript
// Check HTTPS requirement
if (location.protocol !== "https:" && location.hostname !== "localhost") {
  console.warn("Geolocation requires HTTPS");
  alert("Tính năng vị trí yêu cầu kết nối HTTPS");
  return;
}

// Check browser support
if (!navigator.geolocation) {
  console.error("Geolocation not supported");
  alert("Trình duyệt không hỗ trợ định vị");
  return;
}

// Implement fallback
const fallbackOptions = {
  enableHighAccuracy: false,
  timeout: 15000,
  maximumAge: 600000,
};
```

#### **Sự Cố**: Geocoding thất bại

**Triệu Chứng**: Không có kết quả, lỗi mạng
**Giải Pháp**:

```javascript
// Implement retry mechanism
async function geocodeWithRetry(address, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const result = await geocodeAddress(address);
      if (result && result.length > 0) {
        return result;
      }
    } catch (error) {
      console.warn(`Geocoding attempt ${i + 1} failed:`, error);
      if (i === maxRetries - 1) throw error;

      // Wait before retry
      await new Promise((resolve) => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}

// Fallback to Google Maps
function fallbackToGoogleMaps(address) {
  const googleUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
    address
  )}`;
  window.open(googleUrl, "_blank");
}
```

#### **Sự Cố**: Hiệu suất chậm

**Giải Pháp**:

```javascript
// Debounce geocoding requests
const debouncedGeocode = debounce(geocodeAddress, 500);

function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Preload critical resources
function preloadMapResources() {
  const link = document.createElement("link");
  link.rel = "prefetch";
  link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
  document.head.appendChild(link);
}
```

### 🔧 Công Cụ Debug

```javascript
// Debug mode for development
const DEBUG_MODE = process.env.NODE_ENV === "development";

function debugLog(message, data = null) {
  if (DEBUG_MODE) {
    console.log(`🗺️ [MAP DEBUG] ${message}`, data);
  }
}

// Debug geocoding results
function debugGeocoding(address, results) {
  if (DEBUG_MODE) {
    console.group(`🔍 Geocoding Results for: ${address}`);
    results.forEach((result, index) => {
      console.log(`Result ${index + 1}:`, {
        display_name: result.display_name,
        lat: result.lat,
        lon: result.lon,
        importance: result.importance,
      });
    });
    console.groupEnd();
  }
}

// Performance monitoring
function measurePerformance(name, fn) {
  return async (...args) => {
    const start = performance.now();
    const result = await fn(...args);
    const end = performance.now();

    debugLog(`Performance - ${name}: ${(end - start).toFixed(2)}ms`);
    return result;
  };
}
```

---

## 🔮 Cải Tiến Tương Lai

### 🚀 Tính Năng Đã Lên Kế Hoạch

#### 1. **Theo Dõi Thời Gian Thực**

```javascript
// WebSocket integration for live tracking
class RealTimeTracker {
  constructor(shipperId) {
    this.shipperId = shipperId;
    this.socket = new WebSocket(`wss://api.hoashop.com/tracking/${shipperId}`);
    this.watchId = null;
  }

  startTracking() {
    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        const data = {
          shipperId: this.shipperId,
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          timestamp: new Date().toISOString(),
          accuracy: position.coords.accuracy,
        };

        this.socket.send(JSON.stringify(data));
        this.updateMapPosition(data);
      },
      (error) => console.error("Tracking error:", error),
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 10000,
      }
    );
  }

  stopTracking() {
    if (this.watchId) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
    this.socket.close();
  }
}
```

#### 2. **Hỗ Trợ Offline**

```javascript
// Service Worker for offline maps
self.addEventListener("fetch", function (event) {
  if (event.request.url.includes("openstreetmap.org")) {
    event.respondWith(
      caches.match(event.request).then(function (response) {
        return (
          response ||
          fetch(event.request).then(function (response) {
            const responseClone = response.clone();
            caches.open("map-tiles").then(function (cache) {
              cache.put(event.request, responseClone);
            });
            return response;
          })
        );
      })
    );
  }
});

// Offline geocoding cache
class OfflineGeocoder {
  constructor() {
    this.cache = new Map();
    this.loadCache();
  }

  async geocode(address) {
    // Try cache first
    if (this.cache.has(address)) {
      return this.cache.get(address);
    }

    // Try online
    try {
      const result = await onlineGeocode(address);
      this.cache.set(address, result);
      this.saveCache();
      return result;
    } catch (error) {
      throw new Error("Offline and online geocoding failed");
    }
  }

  loadCache() {
    const cached = localStorage.getItem("geocode-cache");
    if (cached) {
      this.cache = new Map(JSON.parse(cached));
    }
  }

  saveCache() {
    localStorage.setItem(
      "geocode-cache",
      JSON.stringify(Array.from(this.cache.entries()))
    );
  }
}
```

#### 3. **Tối Ưu Tuyến Đường Nâng Cao**

```javascript
// Multi-stop route optimization
class RouteOptimizer {
  constructor(apiKey) {
    this.apiKey = apiKey;
  }

  async optimizeRoute(startLocation, destinations) {
    const waypoints = destinations
      .map((dest) => `${dest.lat},${dest.lng}`)
      .join("|");

    const url =
      `https://maps.googleapis.com/maps/api/directions/json?` +
      `origin=${startLocation.lat},${startLocation.lng}&` +
      `destination=${startLocation.lat},${startLocation.lng}&` +
      `waypoints=optimize:true|${waypoints}&` +
      `key=${this.apiKey}`;

    const response = await fetch(url);
    const data = await response.json();

    return {
      optimizedOrder: data.routes[0].waypoint_order,
      totalDistance: data.routes[0].legs.reduce(
        (sum, leg) => sum + leg.distance.value,
        0
      ),
      totalDuration: data.routes[0].legs.reduce(
        (sum, leg) => sum + leg.duration.value,
        0
      ),
    };
  }
}

// Usage in shipper interface
async function optimizeDeliveryRoute(orders) {
  const optimizer = new RouteOptimizer(GOOGLE_MAPS_API_KEY);
  const currentLocation = await getCurrentLocation();

  const destinations = orders.map((order) => ({
    id: order.id,
    lat: order.latitude,
    lng: order.longitude,
    address: order.address,
  }));

  const optimizedRoute = await optimizer.optimizeRoute(
    currentLocation,
    destinations
  );
  displayOptimizedRoute(optimizedRoute);
}
```

#### 4. **Điều Hướng Bằng Giọng Nói**

```javascript
// Text-to-Speech integration
class VoiceNavigator {
  constructor(lang = "vi-VN") {
    this.synth = window.speechSynthesis;
    this.lang = lang;
  }

  speak(text) {
    if ("speechSynthesis" in window) {
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = this.lang;
      utterance.rate = 0.8;
      utterance.pitch = 1;
      this.synth.speak(utterance);
    }
  }

  announceNavigation(instruction) {
    this.speak(instruction);
  }

  announceArrival(address) {
    this.speak(`Bạn đã đến ${address}. Chuẩn bị giao hàng.`);
  }
}

// Integration with geolocation
const voiceNav = new VoiceNavigator();

navigator.geolocation.watchPosition((position) => {
  const distance = calculateDistance(position.coords, destinationCoords);

  if (distance < 100) {
    // Within 100 meters
    voiceNav.announceArrival(currentOrderAddress);
  } else if (distance < 500) {
    // Within 500 meters
    voiceNav.speak("Bạn đang đến gần địa chỉ giao hàng");
  }
});
```

#### 5. **Gợi Ý Địa Chỉ Powered by AI**

```javascript
// Machine learning for address prediction
class AddressAI {
  constructor() {
    this.model = null;
    this.loadModel();
  }

  async loadModel() {
    // Load TensorFlow.js model for address prediction
    this.model = await tf.loadLayersModel("/models/address-predictor.json");
  }

  predictAddress(partialAddress, context) {
    if (!this.model) return [];

    // Preprocess input
    const input = this.preprocessAddress(partialAddress, context);

    // Make prediction
    const prediction = this.model.predict(input);

    // Convert to suggestions
    return this.postprocessPrediction(prediction);
  }

  preprocessAddress(address, context) {
    // Convert address to numerical representation
    // Include context like previous deliveries, area patterns
    return tf.tensor2d([
      /* processed data */
    ]);
  }

  postprocessPrediction(prediction) {
    // Convert model output to address suggestions
    return prediction.dataSync().map((score) => ({
      address: this.decodeAddress(score),
      confidence: score,
    }));
  }
}
```

---

## 📚 Tham Khảo API

### 🔗 API Bên Ngoài

#### **OpenStreetMap Nominatim**

```javascript
// Geocoding API
const NOMINATIM_BASE = "https://nominatim.openstreetmap.org";

const geocodingEndpoints = {
  search: `${NOMINATIM_BASE}/search`,
  reverse: `${NOMINATIM_BASE}/reverse`,
  details: `${NOMINATIM_BASE}/details`,
};

// Example usage
const searchParams = {
  q: encodeURIComponent(address),
  format: "json",
  limit: 3,
  countrycodes: "vn",
  addressdetails: 1,
  extratags: 1,
  namedetails: 1,
};
```

#### **API Google Maps**

```javascript
// Directions API
const GOOGLE_DIRECTIONS_BASE =
  "https://maps.googleapis.com/maps/api/directions";

const directionsParams = {
  origin: `${lat},${lng}`,
  destination: encodeURIComponent(address),
  mode: "driving",
  language: "vi",
  region: "vn",
  key: GOOGLE_MAPS_API_KEY,
};

// Places API for address validation
const GOOGLE_PLACES_BASE = "https://maps.googleapis.com/maps/api/place";

const placesParams = {
  input: encodeURIComponent(address),
  types: "address",
  components: "country:vn",
  language: "vi",
  key: GOOGLE_MAPS_API_KEY,
};
```

#### **Cấu Hình Leaflet**

```javascript
// Map initialization options
const mapOptions = {
  center: [16.0544, 108.2022], // Da Nang coordinates
  zoom: 13,
  minZoom: 5,
  maxZoom: 18,
  zoomControl: true,
  attributionControl: true,
  preferCanvas: false,
};

// Tile layer options
const tileLayerOptions = {
  attribution: "© OpenStreetMap contributors",
  maxZoom: 18,
  tileSize: 256,
  zoomOffset: 0,
  crossOrigin: true,
};

// Marker options
const markerOptions = {
  destination: {
    iconUrl:
      "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
  },
  currentLocation: {
    iconUrl:
      "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
  },
};
```

### 📋 Hằng Số Cấu Hình

```javascript
// Global configuration
const MAP_CONFIG = {
  DEFAULT_LOCATION: {
    lat: 16.0544,
    lng: 108.2022,
    name: "Da Nang, Vietnam",
  },
  ZOOM_LEVELS: {
    country: 6,
    city: 13,
    street: 16,
    building: 18,
  },
  TIMEOUTS: {
    geocoding: 10000,
    geolocation: 10000,
    route_calculation: 15000,
  },
  CACHE_DURATION: {
    geocoding: 24 * 60 * 60 * 1000, // 24 hours
    location: 5 * 60 * 1000, // 5 minutes
    tiles: 7 * 24 * 60 * 60 * 1000, // 7 days
  },
};
```

---

## 📞 Hỗ Trợ & Bảo Trì

### 🛠️ Bảng Điều Khiển Giám Sát

```javascript
// Map usage analytics
class MapAnalytics {
  constructor() {
    this.events = [];
    this.startTime = Date.now();
  }

  trackEvent(eventType, data = {}) {
    this.events.push({
      timestamp: Date.now(),
      type: eventType,
      data: data,
      sessionDuration: Date.now() - this.startTime,
    });

    // Send to analytics service
    this.sendAnalytics(eventType, data);
  }

  sendAnalytics(eventType, data) {
    fetch("/api/analytics/map", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        event: eventType,
        data: data,
        timestamp: new Date().toISOString(),
        userAgent: navigator.userAgent,
        page: window.location.pathname,
      }),
    }).catch((error) => console.warn("Analytics error:", error));
  }

  getUsageReport() {
    return {
      totalEvents: this.events.length,
      sessionDuration: Date.now() - this.startTime,
      eventsByType: this.groupEventsByType(),
      errorRate: this.calculateErrorRate(),
    };
  }
}

// Usage tracking
const analytics = new MapAnalytics();

// Track map interactions
function openMapModal(button) {
  analytics.trackEvent("map_opened", {
    orderId: button.getAttribute("data-order-id"),
    address: button.getAttribute("data-address"),
  });
  // ... rest of function
}

function getCurrentLocationOnMap() {
  analytics.trackEvent("location_requested");
  // ... rest of function
}
```

### 📊 Kiểm Tra Sức Khỏe

```javascript
// System health monitoring
class MapHealthMonitor {
  constructor() {
    this.checks = new Map();
    this.startMonitoring();
  }

  async startMonitoring() {
    setInterval(() => {
      this.runHealthChecks();
    }, 60000); // Every minute
  }

  async runHealthChecks() {
    const checks = [
      this.checkNominatimAPI(),
      this.checkLeafletLibrary(),
      this.checkGeolocationSupport(),
      this.checkLocalStorage(),
    ];

    const results = await Promise.allSettled(checks);

    results.forEach((result, index) => {
      const checkName = ["nominatim", "leaflet", "geolocation", "storage"][
        index
      ];
      this.checks.set(checkName, {
        status: result.status === "fulfilled" ? "healthy" : "unhealthy",
        timestamp: Date.now(),
        error: result.reason || null,
      });
    });

    this.reportHealth();
  }

  async checkNominatimAPI() {
    const response = await fetch(
      "https://nominatim.openstreetmap.org/search?q=test&format=json&limit=1"
    );
    if (!response.ok) throw new Error("Nominatim API unavailable");
    return true;
  }

  checkLeafletLibrary() {
    if (typeof L === "undefined") throw new Error("Leaflet library not loaded");
    return true;
  }

  checkGeolocationSupport() {
    if (!navigator.geolocation) throw new Error("Geolocation not supported");
    return true;
  }

  checkLocalStorage() {
    try {
      localStorage.setItem("test", "test");
      localStorage.removeItem("test");
      return true;
    } catch (error) {
      throw new Error("LocalStorage not available");
    }
  }

  reportHealth() {
    const healthReport = {
      timestamp: new Date().toISOString(),
      checks: Object.fromEntries(this.checks),
      overall: Array.from(this.checks.values()).every(
        (check) => check.status === "healthy"
      )
        ? "healthy"
        : "degraded",
    };

    console.log("Map Health Report:", healthReport);

    // Send to monitoring service
    fetch("/api/health/map", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(healthReport),
    }).catch((error) => console.warn("Health report failed:", error));
  }
}
```

### 📧 Thông Tin Liên Hệ

- **Đội Phát Triển**: tech@hoashop.com
- **Đội Hỗ Trợ**: support@hoashop.com
- **Liên Hệ Khẩn Cấp**: +84-xxx-xxx-xxx
- **Tài Liệu**: https://docs.hoashop.com/shipper/maps

---

## ✅ Danh Sách Kiểm Tra Triển Khai

### 🚀 Trước Triển Khai

- [ ] Thư viện Leaflet được tải đúng cách
- [ ] OpenStreetMap tiles có thể truy cập
- [ ] Endpoint API Geocoding đã được kiểm tra
- [ ] Quyền Geolocation đã được triển khai
- [ ] Tích hợp Google Maps đã được xác minh
- [ ] Xử lý lỗi toàn diện
- [ ] Khả năng đáp ứng mobile đã được kiểm tra
- [ ] Hiệu suất đã được tối ưu
- [ ] Các biện pháp bảo mật đã có
- [ ] Theo dõi phân tích đã được cấu hình

### 📱 Hỗ Trợ Trình Duyệt

- [ ] Chrome 90+ ✅
- [ ] Firefox 88+ ✅
- [ ] Safari 14+ ✅
- [ ] Edge 90+ ✅
- [ ] Mobile Chrome ✅
- [ ] Mobile Safari ✅
- [ ] Samsung Internet ✅

### 🔧 Xác Thực Tính Năng

- [ ] Bản đồ tải chính xác
- [ ] Geocoding địa chỉ hoạt động
- [ ] Phát hiện vị trí hiện tại
- [ ] Tính toán tuyến đường chính xác
- [ ] Điều hướng Google Maps
- [ ] Tương tác modal mượt mà
- [ ] Thông báo lỗi rõ ràng
- [ ] Trạng thái loading hiển thị
- [ ] Dọn dẹp khi đóng modal
- [ ] Đáp ứng trên mọi thiết bị

---

## 🎉 Kết Luận

Hệ thống chỉ đường bằng bản đồ cho module Shipper đã được thiết kế và triển khai với các tính năng toàn diện:

✅ **Tích hợp đa nền tảng**: Leaflet + OpenStreetMap + Google Maps  
✅ **Geocoding thông minh**: Multi-tier strategy với fallback  
✅ **Định vị chính xác**: HTML5 Geolocation với error handling  
✅ **Navigation seamless**: Tích hợp Google Maps Direction  
✅ **UX tối ưu**: Responsive design và intuitive controls  
✅ **Performance cao**: Caching, lazy loading, resource management  
✅ **Security đầy đủ**: Privacy protection và data security  
✅ **Monitoring complete**: Health checks và analytics  
✅ **Mobile-ready**: Touch-friendly và orientation support  
✅ **Future-proof**: Extensible architecture cho advanced features

Hệ thống đã sẵn sàng hỗ trợ shipper trong việc giao hàng hiệu quả và chính xác, cung cấp trải nghiệm người dùng mượt mà trên mọi thiết bị và platform.

---

_📝 Tài liệu được tạo bởi Đội Phát Triển - Hoa Shop_  
_🗓️ Cập nhật: August 27, 2025_  
_📋 Phiên bản: 1.0 - Hệ Thống Điều Hướng Bản Đồ_
