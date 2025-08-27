# TÀI LIỆU TRANG CHỦ - KARINA HANA FLOWER SHOP

## TỔNG QUAN HỆ THỐNG TRANG CHỦ

Trang chủ của Karina Hana Flower Shop là một ứng dụng web Spring Boot với Thymeleaf, được thiết kế để hiển thị các sản phẩm hoa tươi, dịch vụ, bài viết và các tính năng ecommerce.

---

## CẤU TRÚC THÀNH PHẦN CHÍNH

### 1. **CONTROLLER LAYER**

#### HomeController.java

**Đường dẫn**: `src/main/java/com/datn/Controller/user/HomeController.java`

**Chức năng chính**:

- Xử lý routing cho trang chủ (`/home`)
- Cung cấp API cho filter sản phẩm bán chạy (`/api/best-seller`)
- Xử lý tìm kiếm sản phẩm (`/search`)
- API gợi ý tìm kiếm (`/api/search-suggestions`)
- API đếm số lượng giỏ hàng (`/cart/count`)

**Dependencies được inject**:

```java
@Autowired CategoryService categoryService;
@Autowired ProductService productService;
@Autowired ProductCategoryService productCategoryService;
@Autowired PostService postService;
@Autowired PromotionService promotionservice;
@Autowired AuthService authService;
@Autowired CartItemService cartItemService;
@Autowired CommentService commentService;
@Autowired ServiceService serviceService;
```

---

### 2. **VIEW LAYER**

#### home.html

**Đường dẫn**: `src/main/resources/templates/home.html`

**Các section chính**:

1. **Hero Section**: Banner chính với animation particles
2. **Features Section**: 3 tính năng nổi bật (giao hàng nhanh, chụp hình, cam kết chất lượng)
3. **New Products Section**: Sản phẩm mới theo từng danh mục
4. **Promotion Section**: Hiển thị sản phẩm giảm giá
5. **Promotion Codes Section**: Mã giảm giá hiện có
6. **Best Seller Section**: Sản phẩm bán chạy với filter
7. **Blog Section**: Bài viết slider
8. **Accessory Section**: Sản phẩm phụ kiện
9. **Testimonials Section**: Đánh giá khách hàng
10. **Service Section**: Dịch vụ của shop

---

### 3. **SERVICE LAYER & BUSINESS LOGIC**

#### ProductService Interface

**Các method quan trọng cho trang chủ**:

```java
// Top 10 sản phẩm bán chạy toàn shop
List<Product> findBestSellingProductPerCategory();

// Top 10 sản phẩm bán chạy theo danh mục
List<Product> findBestSellerByCategory(String categoryName);

// 6 sản phẩm mới nhất từ 6 danh mục khác nhau
List<Product> findLatestProductsPerCategory();

// Top 10 phụ kiện theo tên danh mục
List<Product> findTop10ByProductCategoryName(String productCategoryName);

// Top 4 sản phẩm giảm giá
List<Product> findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(int minDiscount);
```

---

### 4. **DATA ACCESS LAYER**

#### ProductDAO Interface

**Các truy vấn SQL quan trọng**:

##### 1. Sản phẩm bán chạy toàn shop

```sql
@Query(value = "SELECT TOP 10 p.* FROM products p " +
    "LEFT JOIN ( " +
    "    SELECT od.product_id, SUM(od.quantity) as total_sold " +
    "    FROM order_details od " +
    "    JOIN orders o ON od.order_id = o.id " +
    "    WHERE o.status = N'Hoàn tất' " +
    "    GROUP BY od.product_id " +
    ") sales ON p.id = sales.product_id " +
    "ORDER BY COALESCE(sales.total_sold, 0) DESC", nativeQuery = true)
List<Product> findBestSellingProductPerCategory();
```

**💡 Giải thích truy vấn:**

- **SELECT TOP 10 p.\***: Lấy 10 sản phẩm bán chạy nhất
- **Subquery `sales`**: Tính tổng số lượng đã bán cho mỗi sản phẩm
  - `SUM(od.quantity) as total_sold`: Tổng số lượng bán ra từ chi tiết đơn hàng
  - `JOIN orders o ON od.order_id = o.id`: Liên kết với bảng đơn hàng
  - `WHERE o.status = N'Hoàn tất'`: **CHỈ** tính đơn hàng đã hoàn thành (không tính đơn hủy/chờ xử lý)
  - `GROUP BY od.product_id`: Nhóm theo sản phẩm để tính tổng
- **LEFT JOIN**: Đảm bảo hiển thị cả sản phẩm chưa bán (total_sold = NULL)
- **COALESCE(sales.total_sold, 0)**: Chuyển NULL thành 0 cho sản phẩm chưa bán
- **ORDER BY ... DESC**: Sắp xếp từ bán chạy nhất đến ít nhất

##### 2. Sản phẩm bán chạy theo danh mục

```sql
@Query(value = "SELECT TOP 10 p.* FROM products p " +
    "INNER JOIN product_categories pc ON p.product_Category_Id = pc.id " +
    "LEFT JOIN ( " +
    "    SELECT od.product_id, SUM(od.quantity) as total_sold " +
    "    FROM order_details od " +
    "    JOIN orders o ON od.order_id = o.id " +
    "    WHERE o.status = N'Hoàn tất' " +
    "    GROUP BY od.product_id " +
    ") sales ON p.id = sales.product_id " +
    "WHERE pc.name = :categoryName " +
    "ORDER BY COALESCE(sales.total_sold, 0) DESC", nativeQuery = true)
List<Product> findBestSellerByCategory(@Param("categoryName") String categoryName);
```

**💡 Giải thích truy vấn:**

- **INNER JOIN product_categories**: Liên kết sản phẩm với danh mục của nó
- **WHERE pc.name = :categoryName**: Lọc theo tên danh mục cụ thể (VD: "Lãng hoa tươi", "Giỏ hoa tươi")
- **:categoryName**: Parameter truyền vào từ phương thức (@Param)
- **Subquery `sales`**: Tương tự truy vấn trên, tính tổng số lượng bán
- **Logic**: Lấy 10 sản phẩm bán chạy nhất TRONG MỘT danh mục cụ thể
- **Ứng dụng**: Dùng cho filter buttons (Lãng hoa, Giỏ hoa, Bó hoa)

##### 3. Sản phẩm mới nhất theo danh mục

```sql
@Query(value = "SELECT TOP 6 p.* FROM products p " +
    "INNER JOIN ( " +
    "    SELECT product_Category_Id, MAX(id) as max_id " +
    "    FROM products " +
    "    GROUP BY product_Category_Id " +
    ") latest ON p.product_Category_Id = latest.product_Category_Id " +
    "AND p.id = latest.max_id " +
    "ORDER BY p.product_Category_Id", nativeQuery = true)
List<Product> findLatestProductsPerCategory();
```

**💡 Giải thích truy vấn:**

- **Mục tiêu**: Lấy 1 sản phẩm MỚI NHẤT từ mỗi danh mục (tối đa 6 danh mục)
- **Subquery `latest`**:
  - `GROUP BY product_Category_Id`: Nhóm sản phẩm theo danh mục
  - `MAX(id) as max_id`: Lấy ID lớn nhất trong mỗi nhóm (= sản phẩm mới nhất vì ID auto-increment)
- **INNER JOIN điều kiện kép**:
  - `p.product_Category_Id = latest.product_Category_Id`: Cùng danh mục
  - `p.id = latest.max_id`: Chính xác sản phẩm có ID lớn nhất
- **Kết quả**: 6 sản phẩm mới nhất, mỗi danh mục 1 sản phẩm
- **Ứng dụng**: Section "Sản phẩm mới" trên trang chủ

##### 4. Top 8 sản phẩm bán chạy tổng quát

```sql
@Query(value = """
    SELECT TOP 8
        p.id, p.name, p.description, p.price, p.quantity,
        p.image_url, p.image_url2, p.image_url3,
        p.product_category_id, p.color_id, p.category_id,
        p.discount_percent, p.discount_start, p.discount_end, p.available
    FROM products p
    JOIN order_details od ON p.id = od.product_id
    GROUP BY
        p.id, p.name, p.description, p.price, p.quantity,
        p.image_url, p.image_url2, p.image_url3,
        p.product_category_id, p.color_id, p.category_id,
        p.discount_percent, p.discount_start, p.discount_end, p.available
    ORDER BY SUM(od.quantity) DESC
    """, nativeQuery = true)
List<Product> findSellingProducts();
```

**💡 Giải thích truy vấn:**

- **SELECT TOP 8 với các column cụ thể**: Lấy 8 sản phẩm với đầy đủ thông tin cần thiết
- **JOIN order_details**: Chỉ lấy sản phẩm ĐÃ TỪNG ĐƯỢC BÁN (khác với LEFT JOIN ở trên)
- **GROUP BY tất cả columns**: Nhóm theo sản phẩm để tính tổng quantity
- **ORDER BY SUM(od.quantity) DESC**: Sắp xếp theo tổng số lượng bán (cao nhất trước)
- **Khác biệt với truy vấn #1**:
  - Không filter theo status đơn hàng → tính cả đơn chưa hoàn tất
  - INNER JOIN thay vì LEFT JOIN → loại bỏ sản phẩm chưa bán
  - Ít hơn kết quả (8 vs 10)
- **Ứng dụng**: Widget "Trending Products" hoặc section phụ

---

#### 🔍 **SO SÁNH CÁC TRUY VẤN SQL**

| Tiêu chí             | Truy vấn #1 (Top 10 toàn shop) | Truy vấn #2 (Top 10 theo danh mục) | Truy vấn #3 (Mới nhất) | Truy vấn #4 (Top 8 tổng quát) |
| -------------------- | ------------------------------ | ---------------------------------- | ---------------------- | ----------------------------- |
| **Số lượng**         | TOP 10                         | TOP 10                             | TOP 6                  | TOP 8                         |
| **Logic chính**      | Bán chạy toàn bộ               | Bán chạy theo danh mục             | Mới nhất mỗi danh mục  | Từng bán (không care status)  |
| **JOIN type**        | LEFT JOIN                      | LEFT JOIN + INNER JOIN             | INNER JOIN             | INNER JOIN                    |
| **Filter orders**    | ✅ `status = 'Hoàn tất'`       | ✅ `status = 'Hoàn tất'`           | ❌ Không care          | ❌ Không care                 |
| **Include chưa bán** | ✅ (COALESCE)                  | ✅ (COALESCE)                      | ❌                     | ❌                            |
| **GROUP BY**         | Subquery                       | Subquery                           | Subquery               | Main query                    |
| **Mục đích**         | Section chính "Best Seller"    | Filter buttons                     | "Sản phẩm mới"         | Widget phụ                    |

#### 📋 **BUSINESS LOGIC GIẢI THÍCH**

1. **Tại sao filter `status = 'Hoàn tất'`?**

   - Chỉ tính đơn hàng thực sự thành công
   - Loại bỏ đơn hủy, đơn chờ xử lý, đơn thất bại
   - Đảm bảo dữ liệu thống kê chính xác

2. **Tại sao dùng LEFT JOIN?**

   - Hiển thị cả sản phẩm mới chưa ai mua
   - Tránh missing data trong danh sách
   - COALESCE(NULL, 0) để xử lý sản phẩm chưa bán

3. **Tại sao MAX(id) cho sản phẩm mới nhất?**

   - ID auto-increment → ID lớn hơn = tạo sau
   - Đơn giản hơn so với dùng created_date
   - Performance tốt hơn với index trên primary key

4. **Tại sao GROUP BY nhiều columns?**
   - SQL Server yêu cầu GROUP BY tất cả non-aggregate columns
   - Đảm bảo query syntax đúng
   - Lấy được full thông tin sản phẩm

---

### 5. **FRONTEND LOGIC**

#### home-page.js

**Đường dẫn**: `src/main/resources/static/js/home-page.js`

**Các tính năng chính**:

##### 1. Filter Best Seller Products

```javascript
filterBtns.forEach((btn) => {
  btn.addEventListener("click", async function () {
    const type = this.dataset.type || "default";

    try {
      const response = await fetch(`/api/best-seller?type=${type}`);
      const products = await response.json();
      updateProductGrid(products);
    } catch (error) {
      console.error("Error fetching products:", error);
    }
  });
});
```

##### 2. Animation & Effects

- **AOS (Animate On Scroll)**: Hiệu ứng animation khi scroll
- **Flower Particles**: Hiệu ứng hoa rơi trên hero section
- **Service Carousel**: Slider ảnh dịch vụ
- **Post Slider**: Swiper.js cho bài viết

##### 3. Cart Management

```javascript
// Thêm sản phẩm vào giỏ hàng
form.addEventListener("submit", async function (e) {
  e.preventDefault();
  const formData = new FormData(form);

  const response = await fetch("/cart/add", {
    method: "POST",
    body: formData,
  });

  if (response.ok) {
    updateCartCount();
    showSuccessModal();
  }
});
```

---

## FLOW HOẠT ĐỘNG TRANG CHỦ

### 1. **Luồng Load Trang Chủ**

```
User Request → HomeController.home() → Service Layer → DAO Layer → Database
                     ↓
Model Attributes ← Return Data ← Business Logic ← SQL Queries
                     ↓
home.html Template Engine (Thymeleaf) → Rendered HTML → User Browser
```

### 2. **Chi tiết các bước**:

1. **User truy cập `/home`**
2. **HomeController.home()** được trigger
3. **Service Layer** gọi các methods:

   - `findLatestProductsPerCategory()` - Lấy 6 sản phẩm mới
   - `findBestSellingProductPerCategory()` - Lấy 10 sản phẩm bán chạy
   - `findBestSellerByCategory("Lãng hoa tươi")` - Default best seller
   - `findTop10ByProductCategoryName("Phụ kiện đi kèm")` - Phụ kiện
   - `findTop4ByDiscountPercentGreaterThan...()` - Sản phẩm giảm giá
   - Các service khác: categories, posts, comments, promotions, services

4. **DAO Layer** thực thi các truy vấn SQL phức tạp
5. **Database** trả về kết quả
6. **Controller** đưa data vào Model và render view
7. **Thymeleaf** compile template với data
8. **Browser** nhận HTML + CSS + JS và render giao diện

### 3. **Luồng Filter Best Seller**

```
User Click Filter Button → JavaScript Event → AJAX Request → /api/best-seller
                                                                      ↓
HomeController.getBestSellerByType() → Switch Case Logic → Appropriate Service Method
                                                                      ↓
JSON Response ← ProductList ← Database Query ← Service/DAO Layer
        ↓
JavaScript updateProductGrid() → DOM Manipulation → Updated UI
```

---

## CẤU TRÚC DATABASE LIÊN QUAN

### **Các bảng chính**:

1. **products**: Bảng sản phẩm chính
2. **product_categories**: Danh mục sản phẩm (Lãng hoa, Giỏ hoa, Bó hoa, Phụ kiện)
3. **categories**: Loại hoa (Hoa hồng, Hoa lan, etc.)
4. **order_details**: Chi tiết đơn hàng (để tính sản phẩm bán chạy)
5. **orders**: Đơn hàng (status = 'Hoàn tất')
6. **posts**: Bài viết
7. **comments**: Bình luận đánh giá
8. **promotions**: Mã giảm giá
9. **services**: Dịch vụ

### **Quan hệ chính**:

- `products.product_Category_Id → product_categories.id`
- `products.category_id → categories.id`
- `order_details.product_id → products.id`
- `order_details.order_id → orders.id`

---

## TÍNH NĂNG ĐẶC BIỆT

### 1. **Logic Tính Sản Phẩm Bán Chạy**

- Dựa trên **SUM(order_details.quantity)** từ các đơn hàng **"Hoàn tất"**
- **KHÔNG** dựa trên số lượng tồn kho (`products.quantity`)
- Sử dụng `COALESCE(sales.total_sold, 0)` để xử lý sản phẩm chưa bán

### 2. **Giá Sản Phẩm Động**

```html
<!-- Nếu đang trong thời gian giảm giá -->
<div
  th:if="${product.discountPercent != null && product.discountPercent > 0
    && product.discountStart != null
    && T(java.time.LocalDate).now().compareTo(product.discountStart) >= 0
    && (product.discountEnd == null or T(java.time.LocalDate).now().compareTo(product.discountEnd) <= 0)}"
>
  <span class="discounted-price">Giá sau giảm</span>
  <span class="original-price">Giá gốc</span>
</div>
```

### 3. **Responsive Design**

- Bootstrap 5 framework
- Mobile-first approach
- Swiper.js cho mobile sliding
- AOS animation library

### 4. **Performance Optimization**

- Lazy loading images với `onerror` fallback
- AJAX pagination cho search
- Debounced search suggestions
- CSS/JS minification ready

---

## CÁC API ENDPOINTS

| Method | Endpoint                                      | Mô tả                    |
| ------ | --------------------------------------------- | ------------------------ |
| GET    | `/home`                                       | Trang chủ chính          |
| GET    | `/api/best-seller?type={lang/gio/bo/default}` | Filter sản phẩm bán chạy |
| GET    | `/search?keyword={}&page={}&size={}`          | Tìm kiếm sản phẩm        |
| GET    | `/api/search-suggestions?keyword={}`          | Gợi ý tìm kiếm           |
| GET    | `/cart/count`                                 | Đếm số lượng giỏ hàng    |
| POST   | `/cart/add`                                   | Thêm sản phẩm vào giỏ    |

---

## SECURITY & SESSION MANAGEMENT

- **AuthService**: Quản lý authentication
- **Session**: Lưu userId và cart data
- **CSRF Protection**: Spring Security default
- **XSS Protection**: Thymeleaf auto-escape

---

## DEPLOYMENT & CONFIGURATION

### **Application Properties**:

```properties
# Database configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BanHang;encrypt=false;
spring.datasource.username=sa
spring.datasource.password=123
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
spring.jpa.show-sql=true

# Email configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# File upload settings
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Thymeleaf settings
spring.thymeleaf.cache=false (development)
```

### **Dependencies chính**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

---

## ENTITY MODELS & DATA STRUCTURE

### **Product Entity**

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "NVARCHAR(55)", nullable = false)
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private double price;

    @PositiveOrZero(message = "Số lượng không được âm")
    private Integer quantity;

    // 3 URLs cho hình ảnh sản phẩm
    private String image_url;
    private String image_url2;
    private String image_url3;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "Category_Id")
    private Category category; // Loại hoa (Hoa hồng, Hoa lan...)

    @ManyToOne
    @JoinColumn(name = "product_Category_Id")
    private ProductCategory productCategory; // Danh mục (Lãng hoa, Giỏ hoa...)

    @ManyToOne
    @JoinColumn(name = "color_Id")
    private Color color;

    // Discount fields
    @PositiveOrZero
    private Integer discountPercent;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate discountStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate discountEnd;

    private Boolean available = true;

    // Calculated field - giá sau giảm
    public double getPriceAfterDiscount() {
        if (discountPercent != null && discountPercent > 0
            && discountStart != null
            && LocalDate.now().compareTo(discountStart) >= 0
            && (discountEnd == null || LocalDate.now().compareTo(discountEnd) <= 0)) {
            return price * (100 - discountPercent) / 100.0;
        }
        return price;
    }
}
```

### **Related Entities**:

#### ProductCategory (Danh mục chính)

```java
@Entity
@Table(name = "product_categories")
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String name; // "Lãng hoa tươi", "Giỏ hoa tươi", "Bó hoa tươi", "Phụ kiện đi kèm"

    @OneToMany(mappedBy = "productCategory")
    private List<Product> products;
}
```

#### Category (Loại hoa)

```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String name; // "Hoa hồng", "Hoa lan", "Hoa ly"...

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
```

---

## LAYOUT & TEMPLATE STRUCTURE

### **Main Layout Template**

**File**: `src/main/resources/templates/layouts/layout.html`

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
  <head>
    <!-- Meta tags -->
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />

    <!-- External CSS Libraries -->
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <link
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"
      rel="stylesheet"
    />
    <link
      href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Poppins:wght@300;400&display=swap"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/aos@2.3.4/dist/aos.css"
      rel="stylesheet"
    />

    <!-- Custom CSS -->
    <link th:href="@{/css/chat-float.css}" rel="stylesheet" />
    <link th:href="@{/css/nav.css}" rel="stylesheet" />
    <link th:href="@{/css/footer.css}" rel="stylesheet" />
  </head>

  <body>
    <!-- Header/Navigation -->
    <div th:replace="~{fragments/header}"></div>

    <!-- Main Content -->
    <main>
      <!-- Dynamic content based on 'view' attribute -->
      <div th:if="${view == 'home'}" th:include="~{home}"></div>
      <div th:if="${view == 'search'}" th:include="~{search}"></div>
      <!-- Other views... -->
    </main>

    <!-- Footer -->
    <div th:replace="~{fragments/footer}"></div>

    <!-- JavaScript Libraries -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/aos@2.3.4/dist/aos.js"></script>
  </body>
</html>
```

### **Fragments Structure**:

- `fragments/header.html` - Navigation, search bar, cart icon
- `fragments/footer.html` - Company info, links, contact
- `fragments/cart-success-modal.html` - Add to cart success popup
- `fragments/chatbot.html` - Customer support chat

---

## CSS ARCHITECTURE

### **Main Styles**: `src/main/resources/static/css/home.css`

#### Key CSS Classes:

```css
/* Hero Section with Parallax Effect */
.hero-section {
  height: 100vh;
  background: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)),
    url("/images/hero-bg.jpg");
  background-attachment: fixed;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Animated Flower Particles */
.flower-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.flower {
  position: absolute;
  width: 20px;
  height: 20px;
  background: url("/images/flower-particle.png");
  animation: fall linear infinite;
}

@keyframes fall {
  0% {
    transform: translateY(-100vh) rotate(0deg);
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
  }
}

/* Product Grid - Responsive Design */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  padding: 2rem 0;
}

.product-card {
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
}

.product-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* Image Hover Effect */
.product-image {
  position: relative;
  overflow: hidden;
}

.product-image .default-img {
  transition: opacity 0.3s ease;
}

.product-image .hover-img {
  position: absolute;
  top: 0;
  left: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.product-card:hover .default-img {
  opacity: 0;
}

.product-card:hover .hover-img {
  opacity: 1;
}

/* Filter Buttons */
.filter-btn {
  padding: 10px 20px;
  border: 2px solid #e91e63;
  background: transparent;
  color: #e91e63;
  border-radius: 25px;
  margin: 0 5px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.filter-btn.active,
.filter-btn:hover {
  background: #e91e63;
  color: white;
  transform: translateY(-2px);
}

/* Price Display */
.product-price {
  font-weight: bold;
  color: #e91e63;
}

.discounted-price {
  color: #e91e63;
  font-size: 1.1em;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 0.9em;
}

/* Responsive Breakpoints */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }

  .hero-section {
    height: 70vh;
    background-attachment: scroll;
  }
}
```

---

## JAVASCRIPT FUNCTIONALITY

### **Core JavaScript Features**:

#### 1. **Animation Initialization**

```javascript
// AOS (Animate On Scroll) Setup
AOS.init({
  duration: 1000,
  once: true,
  offset: 100,
});
```

#### 2. **Particle System**

```javascript
// Flower particles animation
function createFlower() {
  const flower = document.createElement("div");
  flower.classList.add("flower");
  flower.style.left = Math.random() * 100 + "vw";
  flower.style.animationDuration = Math.random() * 5 + 5 + "s";
  flower.style.opacity = Math.random() * 0.5 + 0.3;
  particleContainer.appendChild(flower);

  // Auto cleanup after animation
  setTimeout(() => flower.remove(), 10000);
}

// Create new particle every 500ms
setInterval(createFlower, 500);
```

#### 3. **Dynamic Product Rendering**

```javascript
function createProductCard(product, index) {
  const productCard = document.createElement("div");
  productCard.className = "product-card";
  productCard.setAttribute("data-aos", "fade-in");

  const imageUrl =
    product.image_url && product.image_url !== ""
      ? `/images/${product.image_url}`
      : "/images/logo.png";

  const priceHTML =
    product.discountPercent && product.discountPercent > 0
      ? `<div class="product-price">
             <span class="discounted-price">${formatPrice(
               product.priceAfterDiscount
             )}₫</span>
             <span class="original-price">${formatPrice(product.price)}₫</span>
           </div>`
      : `<div class="product-price">${formatPrice(product.price)}₫</div>`;

  productCard.innerHTML = `
        <div class="product-image">
            <a href="/detail?id=${product.id}">
                <img src="${imageUrl}" class="default-img" alt="${product.name}"/>
                <img src="${imageUrl2}" class="hover-img" alt="${product.name}"/>
            </a>
            <form action="/cart/add" method="post">
                <input type="hidden" name="productId" value="${product.id}" />
                <input type="hidden" name="quantity" value="1" />
                <button type="submit" class="cart-icon-wrapper">
                    <div class="cart-icon"><i class="fas fa-shopping-cart"></i></div>
                    <div class="add-to-cart-text">Add To Cart</div>
                </button>
            </form>
        </div>
        <div class="product-info">
            <div class="product-name">${product.name}</div>
            ${priceHTML}
        </div>
    `;

  return productCard;
}
```

#### 4. **Cart Management**

```javascript
// Add to cart with AJAX
async function addToCart(formData) {
  try {
    const response = await fetch("/cart/add", {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      updateCartCount(); // Update header cart icon
      showSuccessModal(); // Show success popup
    } else {
      throw new Error("Server error");
    }
  } catch (err) {
    alert("Có lỗi khi thêm vào giỏ hàng!");
  }
}

// Update cart count in header
function updateCartCount() {
  fetch("/cart/count")
    .then((response) => response.json())
    .then((count) => {
      const cartCountSpan = document.getElementById("cart-count");
      if (cartCountSpan) cartCountSpan.textContent = count;
    });
}
```

---

## ERROR HANDLING & VALIDATION

### **Server-side Validation**:

```java
// Product entity validation
@NotBlank(message = "Tên sản phẩm không được để trống")
@Size(max = 55, message = "Tên sản phẩm tối đa 55 ký tự")
private String name;

@Positive(message = "Giá sản phẩm phải lớn hơn 0")
private double price;

@PositiveOrZero(message = "Số lượng không được âm")
private Integer quantity;
```

### **Client-side Error Handling**:

```javascript
// Image loading fallback
<img
  src="${imageUrl}"
  alt="${product.name}"
  onerror="this.src='/images/logo.png'; this.onerror=null;"
/>;

// AJAX error handling
try {
  const response = await fetch("/api/best-seller?type=" + type);
  if (!response.ok) throw new Error("Network response was not ok");
  const products = await response.json();
  updateProductGrid(products);
} catch (error) {
  console.error("Error fetching products:", error);
  // Show user-friendly error message
  showErrorMessage("Không thể tải sản phẩm. Vui lòng thử lại.");
}
```

---

## PERFORMANCE OPTIMIZATIONS

### **1. Database Query Optimization**:

- Sử dụng `LEFT JOIN` thay vì `INNER JOIN` để tránh bỏ sót sản phẩm chưa bán
- `TOP 10/6/8` limiting để giảm data transfer
- Index trên các column thường query: `product_id`, `order_id`, `status`

### **2. Frontend Optimizations**:

- **Lazy Loading**: Images load khi cần thiết
- **AJAX Pagination**: Không reload toàn trang
- **CSS/JS Minification**: Giảm file size
- **CDN Usage**: Bootstrap, FontAwesome từ CDN
- **Debounced Search**: Giảm số lần gọi API

### **3. Caching Strategy**:

```properties
# Development - no cache for easier debugging
spring.thymeleaf.cache=false

# Production - enable cache
spring.thymeleaf.cache=true
```

---

## SECURITY CONSIDERATIONS

### **1. Input Validation**:

- Bean Validation với `@NotBlank`, `@Positive`
- Thymeleaf auto-escaping XSS
- SQL Injection prevention với JPA parameterized queries

### **2. Authentication Flow**:

```java
// Check user authentication before cart operations
User user = authService.getUser();
if (user != null) {
    Integer userId = user.getId();
    cartCount = cartItemService.getCartItemsByUserId(userId).size();
}
```

### **3. CSRF Protection**:

- Spring Security default CSRF tokens
- Form-based submissions protected

---

## MONITORING & DEBUGGING

### **1. SQL Logging**:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### **2. Application Logging**:

```java
// Add logging to service methods
@Service
public class ProductServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    public List<Product> findBestSellingProductPerCategory() {
        logger.info("Fetching best selling products");
        List<Product> products = dao.findBestSellingProductPerCategory();
        logger.info("Found {} best selling products", products.size());
        return products;
    }
}
```

### **3. Client-side Debugging**:

```javascript
// Console logging for debugging
console.log("Filter button clicked:", type);
console.log("Fetched products:", products);
console.error("Error fetching products:", error);
```

---

## DEPLOYMENT CHECKLIST

### **Production Readiness**:

- [ ] Database connection string updated
- [ ] Email credentials configured
- [ ] File upload limits set
- [ ] Thymeleaf cache enabled
- [ ] SSL certificates installed
- [ ] Error pages customized
- [ ] Logging levels configured
- [ ] Performance monitoring setup
- [ ] Backup strategy implemented
- [ ] CDN configuration for static assets

---

## KẾT LUẬN

Trang chủ Karina Hana được thiết kế như một hệ thống ecommerce hoàn chỉnh với:

- **Architecture**: MVC pattern rõ ràng
- **Database**: Các truy vấn SQL tối ưu cho business logic
- **Frontend**: Modern UI/UX với animations
- **Performance**: AJAX, lazy loading, caching
- **Security**: Authentication, authorization, data validation

Hệ thống đảm bảo khả năng mở rộng, bảo trì và hiệu suất cao cho một website bán hoa chuyên nghiệp.
