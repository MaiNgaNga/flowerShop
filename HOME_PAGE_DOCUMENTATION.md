# 🌸 TRANG CHỦ KARINA HANA - GIẢI THÍCH ĐƠN GIẢN BẰNG TIẾNG VIỆT

## 🏠 TRANG CHỦ LÀ GÌ?

**Trang chủ** là trang đầu tiên mà khách hàng nhìn thấy khi vào website bán hoa Karina Hana. Giống như cửa tiệm thật, trang chủ phải:

- **Đẹp mắt** để thu hút khách hàng
- **Dễ tìm** sản phẩm mình cần
- **Nhanh chóng** thêm hoa vào giỏ hàng
- **Tin cậy** với khuyến mãi và đánh giá

## 📁 CÁC FILE QUAN TRỌNG

### 1. 🎯 HomeController.java

- **Vai trò**: Như người phục vụ ở cửa hàng
- **Công việc**: Lấy thông tin hoa, giá cả, khuyến mãi từ kho dữ liệu
- **Kết quả**: Chuẩn bị sẵn để hiển thị cho khách

### 2. 🎨 home.html

- **Vai trò**: Như tờ rơi quảng cáo đẹp mắt
- **Công việc**: Sắp xếp, trình bày hoa một cách hấp dẫn
- **Kết quả**: Khách thấy được tất cả sản phẩm đẹp

### 3. ⚡ home-page.js

- **Vai trò**: Như người hỗ trợ tương tác với khách
- **Công việc**: Giúp khách lọc hoa, thêm vào giỏ không cần tải lại trang
- **Kết quả**: Trải nghiệm mượt mà, nhanh chóng

### 4. 💄 home.css

- **Vai trò**: Như người trang trí cửa hàng
- **Công việc**: Làm cho website đẹp, màu sắc hài hòa
- **Kết quả**: Giao diện chuyên nghiệp, bắt mắt

## 🎯 HOMECONTROLLER.JAVA - NÃO BỘ CỦA TRANG CHỦ

### Cách hoạt động đơn giản:

**Bước 1: Khách vào trang `/home`**

```
Khách hàng gõ: website.com/home
→ Hệ thống gọi: HomeController.home()
```

**Bước 2: Controller thu thập thông tin**

```java
// Giống như nhân viên chuẩn bị hàng hóa trước khi khách đến
List<Product> latestProducts = productService.findLatestProductsPerCategory();
List<Product> bestSellingProducts = productService.findBestSellingProductPerCategory();
List<Product> discountProducts = productService.findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(0);
```

**Những gì được chuẩn bị:**

- 🆕 **Sản phẩm mới nhất**: 6 bông hoa mới nhất từ các loại khác nhau
- 🔥 **Bán chạy nhất**: Những bông hoa khách mua nhiều nhất
- 💰 **Đang giảm giá**: Top 4 bông hoa giảm giá cao nhất
- 🎁 **Phụ kiện**: 10 món phụ kiện đi kèm (nơ, giấy gói...)
- 📝 **Bài viết**: Tin tức, hướng dẫn chăm sóc hoa
- ⭐ **Đánh giá**: 3 nhận xét tốt nhất từ khách hàng

**Bước 3: Gửi thông tin cho giao diện**

```java
model.addAttribute("latestProducts", latestProducts);
model.addAttribute("bestSellingProducts", bestSellingProducts);
model.addAttribute("discountProducts", discountProducts);
// ... và nhiều thông tin khác
```

**Bước 4: Hiển thị trang chủ hoàn chỉnh**

### 🔗 Các Service được sử dụng:

```java
@Controller
public class HomeController {
    // === CÁC DỊCH VỤ HỖ TRỢ ===
    @Autowired CategoryService categoryService;        // Quản lý loại hoa (bó, lãng, giỏ)
    @Autowired ProductService productService;          // Xử lý sản phẩm chính
    @Autowired ProductCategoryService productCategoryService; // Quản lý danh mục
    @Autowired PostService postService;                // Xử lý bài viết/blog
    @Autowired PromotionService promotionservice;      // Quản lý khuyến mãi
    @Autowired AuthService authService;                // Xác thực người dùng
    @Autowired CartItemService cartItemService;        // Quản lý giỏ hàng
    @Autowired CommentService commentService;          // Xử lý đánh giá
    @Autowired ServiceService serviceService;          // Quản lý dịch vụ bổ sung
}
```

**Giải thích từng Service:**

- `CategoryService`: Quản lý các loại hoa (bó, lãng, giỏ)
- `ProductService`: Dịch vụ chính xử lý sản phẩm
- `ProductCategoryService`: Quản lý danh mục sản phẩm
- `PostService`: Xử lý bài viết/blog
- `PromotionService`: Quản lý khuyến mãi và mã giảm giá
- `AuthService`: Xác thực và quản lý session người dùng
- `CartItemService`: Quản lý giỏ hàng
- `CommentService`: Xử lý đánh giá/nhận xét
- `ServiceService`: Quản lý dịch vụ bổ sung

### 🏠 Endpoint chính: Khi khách vào trang chủ

```java
@GetMapping("/home")
public String home(Model model) {
    // === THU THẬP DỮ LIỆU ===

    // 1. Lấy danh mục cơ bản
    List<ProductCategory> productCategories = productCategoryService.findAll();
    List<Category> categories = categoryService.findAll();

    // 2. Lấy sản phẩm theo từng tiêu chí
    List<Product> latestProducts = productService.findLatestProductsPerCategory();        // 6 sản phẩm mới nhất
    List<Product> bestSellingProducts = productService.findBestSellingProductPerCategory(); // Bán chạy nhất
    List<Product> top10PhuKien = productService.findTop10ByProductCategoryName("Phụ kiện đi kèm"); // 10 phụ kiện
    List<Product> discountProducts = productService
        .findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(0); // 4 sản phẩm giảm giá

    // 3. Lấy nội dung marketing
    List<Post> posts = postService.findAll();                    // Bài viết blog
    List<Comment> comments = commentService.getTop3Comments();   // 3 đánh giá tốt nhất
    List<ServiceEntity> latestServices = serviceService.findTop1ByAvailableTrueOrderByIdDesc(); // Dịch vụ mới nhất

    // 4. Xử lý giỏ hàng nếu khách đã đăng nhập
    int cartCount = 0;
    User user = authService.getUser();
    if (user != null) {
        Integer userId = user.getId();
        cartCount = cartItemService.getCartItemsByUserId(userId).size();
    }

    // === GỬI DỮ LIỆU CHO GIAO DIỆN ===
    model.addAttribute("cartCount", cartCount);
    model.addAttribute("comments", comments);
    model.addAttribute("productCategories", productCategories);
    model.addAttribute("categories", categories);
    model.addAttribute("latestProducts", latestProducts);
    model.addAttribute("promotionsCode", promotionservice.findValidPromotion());
    model.addAttribute("bestSellingProducts", bestSellingProducts);
    model.addAttribute("defaultBestSeller", productService.findBestSellerByCategory("Lãng hoa tươi "));
    model.addAttribute("posts", posts);
    model.addAttribute("discountProducts", discountProducts);
    model.addAttribute("top10PhuKien", top10PhuKien);
    model.addAttribute("latestServices", latestServices);
    model.addAttribute("view", "home");

    return "layouts/layout"; // Trả về template để hiển thị
}
```

**🔍 Chi tiết cách lấy dữ liệu:**

#### A. 🆕 Sản phẩm mới nhất (findLatestProductsPerCategory)

- **Mục đích**: Lấy 6 sản phẩm mới nhất từ 6 danh mục khác nhau
- **Cách hoạt động**: Tìm sản phẩm có ID lớn nhất (mới nhất) trong mỗi loại hoa
- **Kết quả**: Đảm bảo đa dạng, mỗi loại hoa đều có đại diện

#### B. 🔥 Sản phẩm bán chạy (findBestSellingProductPerCategory)

- **Mục đích**: Lấy sản phẩm bán chạy nhất từ mỗi category
- **Cách hoạt động**: Dựa trên số lượng đã bán (quantity field)
- **Kết quả**: Khách thấy được xu hướng mua sắm

#### C. 💰 Sản phẩm giảm giá (findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc)

- **Mục đích**: Lấy 4 sản phẩm giảm giá cao nhất
- **Điều kiện**: discountPercent > 0, available = true
- **Loại trừ**: Phụ kiện (chỉ focus vào hoa chính)

#### D. 🛒 Xử lý giỏ hàng người dùng

```java
User user = authService.getUser();
if (user != null) {
    cartCount = cartItemService.getCartItemsByUserId(userId).size();
}
```

**Giải thích:**

- **Kiểm tra đăng nhập**: Xem khách có đăng nhập chưa
- **Đếm giỏ hàng**: Tính số lượng items trong giỏ của khách
- **Hiển thị badge**: Show số lượng trên icon giỏ hàng ở header

### 🌐 Các API hỗ trợ tương tác động

#### A. 🎯 Lọc Best Seller động: `/api/best-seller`

**Mục đích**: Khi khách click "Lãng hoa", "Bó hoa", "Giỏ hoa" → không cần tải lại trang

```java
@GetMapping("/api/best-seller")
@ResponseBody
public List<Product> getBestSellerByType(@RequestParam String type) {
    switch (type.toLowerCase()) {
        case "lang":
            return productService.findBestSellerByCategory("Lãng hoa tươi");
        case "gio":
            return productService.findBestSellerByCategory("Giỏ hoa tươi");
        case "bo":
            return productService.findBestSellerByCategory("Bó hoa tươi");
        default:
            return productService.findBestSellingProductPerCategory();
    }
}
```

**Functionality:**

- **AJAX endpoint** cho filter buttons trong Best Seller section
- **Dynamic content loading** không cần reload trang
- **Category-specific** best sellers
- **Fallback mechanism** nếu type không match

#### B. Search Function: @GetMapping("/search")

```java
@GetMapping("/search")
public String search(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        Model model) {

    List<ProductCategory> productCategories = productCategoryService.findAll();
    Pageable pageable = PageRequest.of(page, size);

    if (keyword != null && !keyword.isEmpty()) {
        Page<Product> resultPage = Page.empty();

        // CASCADING SEARCH STRATEGY
        resultPage = productService.searchByCategoryName(keyword, pageable);

        if (resultPage.isEmpty()) {
            resultPage = productService.searchByProductCategoryName(keyword, pageable);
        }
        if (resultPage.isEmpty()) {
            resultPage = productService.searchByName(keyword, pageable);
        }

`        model.addAttribute("products", resultPage.getContent());
`    }

    model.addAttribute("searchKeyword", keyword);
    model.addAttribute("productCategories", productCategories);
    model.addAttribute("view", "search");

    return "layouts/layout";
}
```

**Search Logic Explanation:**

1. **Tier 1**: Search by Category Name (e.g., "Lãng hoa", "Bó hoa")
2. **Tier 2**: Search by Product Category Name (e.g., "Hoa sinh nhật")
3. **Tier 3**: Search by Product Name (e.g., "Hoa hồng đỏ")
4. **Pagination support** với default 12 items per page

#### C. Search Suggestions: @GetMapping("/api/search-suggestions")

```java
@GetMapping("/api/search-suggestions")
@ResponseBody
public List<String> getSearchSuggestions(@RequestParam String keyword) {
    return productService.findSearchSuggestionsByKeyword(keyword, 10);
}
```

**Auto-complete functionality:**

- **Real-time suggestions** khi user type
- **Combined results** từ product names, categories, và product categories
- **Limit 10 suggestions** để tránh overwhelming UI

#### D. Cart Count API: @GetMapping("/cart/count")

```java
@GetMapping("/cart/count")
@ResponseBody
public int getCartItemCount(HttpSession session) {
    Integer userId = (Integer) session.getAttribute("userId");
    if (userId != null) {
        return cartItemService.getCartItemsByUserId(userId).size();
    }
    return 0;
}
```

**Real-time cart updates:**

- **Session-based** user identification
- **AJAX calls** từ JavaScript
- **Dynamic badge updates** không cần reload

## 3. PHÂN TÍCH HOME.HTML TEMPLATE

### 3.1 Cấu trúc HTML và Sections

```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
  <head>
    <!-- META & TITLE -->
    <!-- EXTERNAL CSS LIBRARIES -->
    <!-- CUSTOM CSS -->
  </head>
  <body>
    <!-- === MAIN SECTIONS === -->
    <!-- 1. Hero Section -->
    <!-- 2. Features Section -->
    <!-- 3. New Products Section -->
    <!-- 4. Promotion Section -->
    <!-- 5. Discount Codes Section -->
    <!-- 6. Best Seller Section -->
    <!-- 7. Blog Posts Section -->
    <!-- 8. Accessories Section -->
    <!-- 9. Customer Reviews Section -->
    <!-- 10. Services Section -->
    <!-- === SCRIPTS === -->
  </body>
</html>
```

### 3.2 Thymeleaf Data Binding Chi tiết

#### A. Hero Section với Animation Effects

```html
<section class="hero-section">
  <div class="hero-overlay"></div>
  <div class="flower-particles" id="flower-particles"></div>
  <div class="hero-content">
    <h1 data-aos="fade-down">Karina Hana</h1>
    <p data-aos="fade-up" data-aos-delay="200">
      Tạo nên những khoảnh khắc đáng nhớ với những thiết kế hoa tinh tế
    </p>
    <a
      th:href="@{/about}"
      class="btn btn-primary"
      data-aos="fade-up"
      data-aos-delay="400"
      >Khám Phá Ngay</a
    >
  </div>
</section>
```

**Animation Framework:** AOS (Animate On Scroll)

- `data-aos="fade-down"`: Hero title animation
- `data-aos-delay="200"`: Staggered animation timing
- **Particle system**: JavaScript-generated floating flowers

#### B. Latest Products Per Category

```html
<section class="new-flowers-section text-center">
  <h2 class="mb-4" data-aos="fade-up">Sản phẩm mới theo từng danh mục</h2>
  <div class="new-flower-grid">
    <div
      th:each="product, iterStat : ${latestProducts}"
      class="new-flower-card"
      data-aos="zoom-in-up"
      th:data-aos-delay="${iterStat.index * 100}"
    >
      <a th:href="@{/detail(id=${product.id})}" class="text-decoration-none">
        <img
          th:src="${product.image_url != null and product.image_url != '' ? 
                             '/images/' + product.image_url : '/images/logo.png'}"
          th:alt="${product.name}"
          onerror="this.src='/images/logo.png'"
          style="width: 100%; height: 200px; object-fit: cover"
        />
      </a>

      <div
        class="product-category-name"
        th:text="${product.productCategory != null ? 
                           product.productCategory.name : 'Không có danh mục'}"
      >
        Danh mục
      </div>
    </div>
  </div>
</section>
```

**Thymeleaf Features Used:**

- `th:each="product, iterStat : ${latestProducts}"`: Iteration với index
- `th:data-aos-delay="${iterStat.index * 100}"`: Dynamic animation delay
- **Null-safe navigation**: `product.image_url != null and product.image_url != ''`
- **Fallback image**: `onerror="this.src='/images/logo.png'"`

#### C. Dynamic Discount Display

```html
<div
  th:each="product : ${#lists.size(discountProducts) > 4 ? 
                         discountProducts.subList(0, 4) : discountProducts}"
  th:if="${product.discountStart != null
     and T(java.time.LocalDate).now().compareTo(product.discountStart) >= 0
     and (product.productCategory == null or !product.productCategory.name.toLowerCase().contains('phụ kiện'))}"
>
  <span class="promo-tag" th:text="'-' + ${product.discountPercent} + '%'"
    >-10%</span
  >

  <a th:href="@{/detail(id=${product.id})}">
    <img
      th:src="${product.image_url != null and product.image_url != '' ? 
                     '/images/' + product.image_url : '/images/logo.png'}"
      alt="Sản phẩm giảm giá"
    />
  </a>
</div>
```

## th:each="product : ${#lists.size(discountProducts) > 4 ? discountProducts.subList(0, 4) : discountProducts}"

lists.size(discountProducts): Đếm số phần tử trong danh sách discountProducts
Điều kiện: Nếu danh sách có > 4 sản phẩm → chỉ lấy 4 sản phẩm đầu tiên
subList(0, 4): Cắt danh sách từ index 0 đến 4 (không bao gồm 4)
Nếu ≤ 4 sản phẩm: Hiển thị toàn bộ danh sách
**Complex Conditional Logic:**

## th:if="${product.discountStart != null

## and T(java.time.LocalDate).now().compareTo(product.discountStart) >= 0

## and (product.productCategory == null or !product.productCategory.name.toLowerCase().contains('phụ kiện'))}"

Điều kiện 1: product.discountStart != null

Kiểm tra sản phẩm có ngày bắt đầu giảm giá không
Loại bỏ sản phẩm chưa set thời gian giảm giá
Điều kiện 2: T(java.time.LocalDate).now().compareTo(product.discountStart) >= 0

T(java.time.LocalDate).now(): Lấy ngày hiện tại
compareTo(product.discountStart) >= 0: So sánh với ngày bắt đầu giảm giá
Ý nghĩa: Chỉ hiển thị sản phẩm đã BẮT ĐẦU giảm giá (hôm nay >= ngày bắt đầu)
Điều kiện 3: (product.productCategory == null or !product.productCategory.name.toLowerCase().contains('phụ kiện'))

Trường hợp 1: product.productCategory == null → Sản phẩm không có category → OK
Trường hợp 2: !...contains('phụ kiện') → Category không chứa từ "phụ kiện" → OK
Ý nghĩa: LOẠI TRỪ sản phẩm phụ kiện khỏi section giảm giá

- **List size check**: `${#lists.size(discountProducts) > 4 ? ...}`
- **Date comparison**: `T(java.time.LocalDate).now().compareTo(product.discountStart) >= 0`
- **Category exclusion**: `!product.productCategory.name.toLowerCase().contains('phụ kiện')`

#### D. Best Seller Section với Filter Buttons

```html
<section class="best-seller-section">
  <h2>Best Seller</h2>
  <div>
    <button class="filter-btn active" data-type="lang">Lãng hoa</button>
    <button class="filter-btn" data-type="gio">Giỏ hoa</button>
    <button class="filter-btn" data-type="bo">Bó hoa</button>
  </div>

  <div id="best-seller-grid" class="product-grid">
    <!-- THYMELEAF INITIAL RENDER -->
    <div
      th:each="product, iterStat : ${defaultBestSeller}"
      class="product-card"
    >
      <div class="product-image">
        <a th:href="@{/detail(id=${product.id})}">
          <img
            th:src="${product.image_url != null ? '/images/' + product.image_url : '/images/logo.png'}"
            class="default-img"
            th:alt="${product.name}"
          />
          <img
            th:src="${product.image_url2 != null ? '/images/' + product.image_url2 : '/images/logo.png'}"
            class="hover-img"
            th:alt="${product.name}"
          />
        </a>

        <!-- ADD TO CART FORM -->
        <form th:action="@{/cart/add}" method="post">
          <input type="hidden" name="productId" th:value="${product.id}" />
          <input type="hidden" name="quantity" value="1" />
          <button type="submit" class="cart-icon-wrapper">
            <div class="cart-icon"><i class="fas fa-shopping-cart"></i></div>
            <div class="add-to-cart-text">Add To Cart</div>
          </button>
        </form>
      </div>

      <!-- DYNAMIC PRICING DISPLAY -->
      <div class="product-info">
        <div class="product-name" th:text="${product.name}">Tên sản phẩm</div>

        <!-- DISCOUNTED PRICE LOGIC -->
        <div
          class="product-price"
          th:if="${product.discountPercent != null && product.discountPercent > 0
                     && product.discountStart != null
                     && T(java.time.LocalDate).now().compareTo(product.discountStart) >= 0
                     && (product.discountEnd == null or T(java.time.LocalDate).now().compareTo(product.discountEnd) <= 0)}"
        >
          <span
            class="discounted-price"
            th:text="${#numbers.formatDecimal(product.priceAfterDiscount, 0, 'COMMA', 0, 'POINT')} + '₫'"
          ></span>
          <span
            class="original-price"
            th:text="${#numbers.formatDecimal(product.price, 0, 'COMMA', 0, 'POINT')} + '₫'"
          ></span>
        </div>

        <!-- REGULAR PRICE -->
        <div
          class="product-price"
          th:if="${product.discountPercent == null || product.discountPercent <= 0
                     || product.discountStart == null
                     || T(java.time.LocalDate).now().compareTo(product.discountStart) < 0
                     || (product.discountEnd != null && T(java.time.LocalDate).now().compareTo(product.discountEnd) > 0)}"
          th:text="${#numbers.formatDecimal(product.price, 0, 'COMMA', 0, 'POINT')} + '₫'"
        >
          Giá sản phẩm
        </div>
      </div>
    </div>
  </div>
</section>

Đây là logic NGƯỢC LẠI với điều kiện hiển thị giá giảm. Sản phẩm sẽ hiển thị giá
thường khi: B. Từng điều kiện chi tiết: Điều kiện 1: product.discountPercent ==
null Ý nghĩa: Sản phẩm không có % giảm giá Ví dụ: Sản phẩm mới chưa được set
discount Kết quả: Hiển thị giá gốc Điều kiện 2: product.discountPercent <= 0 Ý
nghĩa: % giảm giá bằng 0 hoặc âm (invalid) Ví dụ: discountPercent = 0 hoặc -5
Kết quả: Không có giảm giá thực sự Điều kiện 3: product.discountStart == null Ý
nghĩa: Không có ngày bắt đầu giảm giá Ví dụ: Admin chưa set thời gian khuyến mãi
Kết quả: Chưa thể áp dụng giảm giá Điều kiện 4:
T(java.time.LocalDate).now().compareTo(product.discountStart) < 0 Ý nghĩa: Ngày
hiện tại CHƯA ĐẾN ngày bắt đầu giảm giá Ví dụ: Hôm nay 26/08, discount bắt đầu
30/08 Kết quả: Chưa đến thời gian khuyến mãi Điều kiện 5: (product.discountEnd
!= null && T(java.time.LocalDate).now().compareTo(product.discountEnd) > 0) Ý
nghĩa: Đã QUA ngày kết thúc giảm giá Logic: discountEnd != null AND now >
discountEnd Ví dụ: Khuyến mãi kết thúc 25/08, hôm nay 26/08 Kết quả: Hết thời
gian khuyến mãi
```

**Key Features:**

- **Hover effect images**: `default-img` và `hover-img`
- **Complex discount logic**: Date range validation
- **Number formatting**: `${#numbers.formatDecimal(...)}`
- **AJAX integration**: JavaScript sẽ replace content

#### E. Blog Posts với Swiper Integration

```html
<section class="post-slider-section">
  <h2 class="section-title">Bài viết</h2>
  <div class="swiper post-slider">
    <div class="swiper-wrapper">
      <div class="swiper-slide" th:each="post : ${posts}">
        <img
          th:src="${post.image != null && post.image != '' ? 
                             '/images/' + post.image : '/images/logo.png'}"
          class="avatar-img"
          style="object-fit: cover"
        />
        <div class="post-content">
          <h5 th:text="${post.title}">Tiêu đề</h5>
          <span th:utext="${post.summary}"></span>
          <div class="d-flex justify-content-between align-items-center mt-2">
            <small class="text-muted">
              <i class="fa fa-calendar"></i>
              <span th:text="${#temporals.format(post.postDate,'dd/MM/yyyy')}"
                >01/01/25</span
              >
            </small>
            <a th:href="@{/postDetail(id=${post.id})}" class="read-more-btn"
              >Xem chi tiết</a
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
```

**Swiper Configuration:**

- **Responsive breakpoints**: 1 slide mobile, 2 tablet, 3 desktop
- **Auto-play**: 3 second intervals
- **Loop**: Infinite scrolling

#### F. Customer Reviews Section

```html
<section class="testimonials-section">
  <div class="testimonials-grid">
    <div
      th:each="comment, iterStat : ${comments}"
      class="testimonial-card"
      data-aos="fade-up"
      th:data-aos-delay="${iterStat.index * 100}"
    >
      <div class="rating">
        <span
          th:each="i : ${#numbers.sequence(1, 5)}"
          th:class="${i <= comment.rating} ? 'star filled fas fa-star' : 'star fas fa-star'"
          th:classappend="${i <= comment.rating} ? 'filled' : ''"
        ></span>
      </div>

      <h3 class="user-name" th:text="${comment.user.name}">Tên người dùng</h3>
      <p class="user-comment" th:text="${comment.content}">
        Nội dung bình luận
      </p>
    </div>
  </div>
</section>
```

**Star Rating Logic:**

- `${#numbers.sequence(1, 5)}`: Generate numbers 1 to 5
- `${i <= comment.rating}`: Conditional class application
- **Dynamic CSS classes**: `filled` vs unfilled stars

## ⚡ PHẦN JAVASCRIPT - TƯƠNG TÁC ĐỘNG CHO TRANG CHỦ

### 🎬 Khởi tạo và chuẩn bị

#### A. Chờ trang web tải xong

```javascript
document.addEventListener("DOMContentLoaded", function () {
  // Code chạy sau khi trang web tải xong
});
```

**Giải thích đơn giản:**

- `DOMContentLoaded`: Sự kiện xảy ra khi trang web tải xong cấu trúc HTML
- **Thời điểm**: Xảy ra TRƯỚC khi hình ảnh, CSS tải xong
- **Mục đích**: Đảm bảo các thành phần trên trang đã sẵn sàng để sử dụng
- **Lý do**: Tránh lỗi khi cố gắng tương tác với thứ chưa tồn tại

#### B. Khởi tạo hiệu ứng cuộn trang (AOS)

```javascript
AOS.init({
  duration: 1000, // Hiệu ứng kéo dài 1 giây
  once: true, // Chỉ chạy 1 lần khi cuộn đến
});
```

**Cài đặt hiệu ứng:**

- **duration: 1000**: Mỗi hiệu ứng kéo dài 1 giây
- **once: true**: Hiệu ứng không lặp lại khi cuộn lên/xuống
- **Áp dụng toàn cục**: Cho tất cả phần tử có thuộc tính `data-aos`

**Các hiệu ứng trong HTML:**

```html
<h1 data-aos="fade-down">Karina Hana</h1>
<!-- Chữ từ từ xuất hiện từ trên -->
<p data-aos="fade-up" data-aos-delay="200">...</p>
<!-- Chữ từ dưới lên, chậm 0.2s -->
<div data-aos="zoom-in-up" data-aos-delay="300">...</div>
<!-- Phóng to + từ dưới lên -->
```

### 🌸 Hệ thống hoa bay (Particle System)

#### A. Tạo khu vực cho hoa bay

```javascript
const particleContainer = document.getElementById("flower-particles");
```

**Cấu trúc HTML tương ứng:**

```html
<div class="flower-particles" id="flower-particles"></div>
```

#### B. Tạo từng bông hoa bay

```javascript
function createFlower() {
  const flower = document.createElement("div");
  flower.classList.add("flower");

  // === VỊ TRÍ NGẪU NHIÊN ===
  flower.style.left = Math.random() * 100 + "vw";

  // === TỐC ĐỘ BAY NGẪU NHIÊN ===
  flower.style.animationDuration = Math.random() * 5 + 5 + "s";

  // === ĐỘ MỜ NGẪU NHIÊN ===
  flower.style.opacity = Math.random() * 0.5 + 0.3;

  // === THÊM VÀO TRANG WEB ===
  particleContainer.appendChild(flower);

  // === TỰ ĐỘNG XÓA SAU 10 GIÂY ===
  setTimeout(() => flower.remove(), 10000);
}
```

**Giải thích từng dòng:**

**Vị trí ngẫu nhiên:**

```javascript
flower.style.left = Math.random() * 100 + "vw";
```

- `Math.random()`: Số ngẫu nhiên từ 0 đến 1
- `* 100`: Nhân lên thành 0 đến 100
- `+ "vw"`: Đơn vị chiều rộng màn hình
- **Kết quả**: Hoa xuất hiện ở vị trí ngẫu nhiên từ trái sang phải

**Tốc độ bay ngẫu nhiên:**

```javascript
flower.style.animationDuration = Math.random() * 5 + 5 + "s";
```

- `Math.random() * 5`: Từ 0 đến 5 giây
- `+ 5`: Cộng thêm 5 giây → Tổng cộng 5-10 giây
- **Mục đích**: Mỗi bông hoa bay với tốc độ khác nhau

**Độ mờ ngẫu nhiên:**

```javascript
flower.style.opacity = Math.random() * 0.5 + 0.3;
```

- `Math.random() * 0.5`: Từ 0 đến 0.5
- `+ 0.3`: Cộng thêm 0.3 → Tổng cộng từ 0.3 đến 0.8
- **Hiệu ứng**: Hoa có độ trong suốt khác nhau, tạo chiều sâu

**Quản lý bộ nhớ:**

```javascript
setTimeout(() => flower.remove(), 10000);
```

- **10000ms = 10 giây**: Thời gian tối đa mỗi bông hoa tồn tại
- **flower.remove()**: Xóa hoàn toàn khỏi trang web
- **Mục đích**: Tránh lag do quá nhiều hoa bay cùng lúc

#### C. Tạo hoa bay liên tục

```javascript
setInterval(createFlower, 500);
```

**Phân tích thời gian:**

- **500ms = 0.5 giây**: Tạo hoa mới mỗi nửa giây
- **Liên tục**: Chạy khi trang web đang hoạt động
- **Kết quả**: 2 bông hoa mới mỗi giây, tối đa 20 bông hoa trên màn hình
- **Cân bằng**: Vừa đẹp mắt vừa không làm lag trang web

### 🎯 Hệ thống lọc Best Seller - Không cần tải lại trang

#### A. Chọn các phần tử cần thiết

```javascript
const filterBtns = document.querySelectorAll(".filter-btn");
const productGrid = document.querySelector("#best-seller-grid");
```

**Giải thích:**

- **filterBtns**: Danh sách tất cả nút có class "filter-btn"
- **productGrid**: Khu vực hiển thị sản phẩm có ID "best-seller-grid"
- **Cấu trúc HTML tương ứng**:

```html
<button class="filter-btn active" data-type="lang">Lãng hoa</button>
<button class="filter-btn" data-type="gio">Giỏ hoa</button>
<button class="filter-btn" data-type="bo">Bó hoa</button>
<div id="best-seller-grid" class="product-grid">...</div>
```

#### B. Gắn sự kiện cho từng nút

```javascript
filterBtns.forEach((btn) => {
  btn.addEventListener("click", async function () {
    // Logic xử lý khi click nút
  });
});
```

**Cách hoạt động:**

- **forEach**: Lặp qua từng nút trong danh sách
- **addEventListener**: Gắn sự kiện click cho mỗi nút
- **async function**: Cho phép sử dụng await bên trong

#### C. Quản lý trạng thái nút

```javascript
// Quản lý trạng thái nút
filterBtns.forEach((button) => button.classList.remove("active"));
this.classList.add("active");
```

**Quy trình cập nhật:**

1. **Bỏ "active"** từ tất cả các nút
2. **Thêm "active"** cho nút vừa được click
3. **Phản hồi trực quan**: CSS thay đổi dựa trên class "active"
4. **Chọn duy nhất**: Chỉ 1 nút được active tại 1 thời điểm

#### D. Lấy thông tin từ nút được click

```javascript
const type = this.dataset.type || "default";
```

**API Dataset:**

- **this.dataset.type**: Truy cập thuộc tính data-type
- **HTML**: `<button data-type="lang">` → `dataset.type = "lang"`
- **Fallback**: "default" nếu không có thuộc tính
- **Ánh xạ loại**: "lang" → "Lãng hoa tươi", "gio" → "Giỏ hoa tươi"

#### E. Gửi yêu cầu AJAX để lấy dữ liệu mới

```javascript
try {
  // Gửi yêu cầu đến server
  const response = await fetch(`/api/best-seller?type=${type}`);
  const products = await response.json();

  // Cập nhật giao diện
  updateProductGrid(products);
} catch (error) {
  console.error("Lỗi khi lấy sản phẩm:", error);
}
```

**Phân tích quy trình AJAX:**

**Bước 1: Gửi HTTP Request**

```javascript
const response = await fetch(`/api/best-seller?type=${type}`);
```

- **Phương thức**: GET request
- **URL**: `/api/best-seller?type=lang` (ví dụ)
- **await**: Chờ server trả về kết quả trước khi tiếp tục
- **Backend**: Gọi method HomeController.getBestSellerByType()

**Bước 2: Chuyển đổi JSON**

```javascript
const products = await response.json();
```

- **response.json()**: Chuyển JSON thành object JavaScript
- **await**: Thao tác không đồng bộ
- **Kết quả**: Mảng các object Product

**Bước 3: Cập nhật giao diện**

```javascript
updateProductGrid(products);
```

- **Gọi hàm helper**: Cập nhật DOM với dữ liệu mới
- **Không chặn**: Thread chính không bị đóng băng

**Bước 4: Xử lý lỗi**

```javascript
catch (error) {
  console.error("Lỗi khi lấy sản phẩm:", error);
}
```

- **Lỗi mạng**: Timeout, 404, 500, etc.
- **Lỗi phân tích**: JSON không hợp lệ
- **Xử lý nhẹ nhàng**: User vẫn thấy nội dung cũ

### 🏗️ Tạo danh sách sản phẩm động

#### A. Reset và kiểm tra khu vực hiển thị

```javascript
function updateProductGrid(products) {
  if (!productGrid) return;

  productGrid.innerHTML = "";

  // Tạo nội dung mới
}
```

**Kiểm tra an toàn:**

- **if (!productGrid)**: Kiểm tra null trước khi thao tác
- **Early return**: Tránh lỗi nếu phần tử không tồn tại
- **innerHTML = ""**: Xóa sạch nội dung cũ

#### B. Vòng lặp tạo thẻ sản phẩm

```javascript
products.forEach((product, index) => {
  const productCard = createProductCard(product, index);
  productGrid.appendChild(productCard);
});
```

**Tham số vòng lặp:**

- **product**: Object sản phẩm hiện tại từ mảng
- **index**: Vị trí (0, 1, 2...) cho hiệu ứng animation
- **createProductCard()**: Hàm helper tạo phần tử DOM
- **appendChild()**: Thêm vào container

#### C. Gắn lại sự kiện cho form giỏ hàng

```javascript
// Gắn lại sự kiện cho form giỏ hàng
const cartForms = productGrid.querySelectorAll('form[action="/cart/add"]');
cartForms.forEach((form) => {
  if (!form.dataset.listener) {
    attachCartFormListener(form);
    form.dataset.listener = "true";
  }
});
```

**Quản lý DOM quan trọng:**

- **Vấn đề**: Nội dung động bị mất sự kiện
- **Giải pháp**: Gắn lại sự kiện sau khi tạo HTML
- **Phòng ngừa**: Kiểm tra `form.dataset.listener` để tránh trùng lặp
- **Đánh dấu**: `dataset.listener = "true"` đánh dấu đã xử lý

#### D. Tạo HTML cho thẻ sản phẩm

```javascript
function createProductCard(product, index) {
  const productCard = document.createElement("div");
  productCard.className = "product-card";

  // === THIẾT LẬP ANIMATION ===
  productCard.setAttribute("data-aos", "fade-in");
  productCard.setAttribute("data-aos-delay", 300 + index * 100);
}
```

**Cấu hình Animation:**

- **data-aos="fade-in"**: Loại hiệu ứng AOS
- **Delay động**: `300 + index * 100` → 300ms, 400ms, 500ms...
- **Hiệu ứng bậc thang**: Thẻ xuất hiện tuần tự, không cùng lúc
- **index \* 100**: Tăng 100ms delay cho mỗi thẻ

#### E. Xử lý URL hình ảnh

```javascript
const imageUrl =
  product.image_url && product.image_url !== ""
    ? `/images/${product.image_url}`
    : "/images/logo.png";

const imageUrl2 =
  product.image_url2 && product.image_url2 !== ""
    ? `/images/${product.image_url2}`
    : "/images/logo.png";
```

**Lập trình phòng thủ:**

- **Kiểm tra null**: `product.image_url &&`
- **Kiểm tra chuỗi rỗng**: `product.image_url !== ""`
- **Cơ chế dự phòng**: Mặc định dùng logo.png
- **Hai hình ảnh**: Hình chính + hình hover cho hiệu ứng tương tác

### 🛒 Hệ thống giỏ hàng thông minh - AJAX

#### A. Chặn form gửi bình thường

```javascript
function attachCartFormListener(form) {
  form.addEventListener("submit", async function (e) {
    e.preventDefault(); // Ngăn form submit bình thường
    const formData = new FormData(form);

    // Xử lý AJAX...
  });
}
```

**Ngăn chặn sự kiện:**

- **e.preventDefault()**: Dừng việc gửi form theo cách thông thường
- **Tại sao cần**: Tránh tải lại trang, duy trì trải nghiệm SPA
- **FormData**: Trích xuất các trường form (productId, quantity)
- **Async function**: Cho phép await cho HTTP requests

#### B. Tạo dữ liệu form

```javascript
const formData = new FormData(form);
```

**Chi tiết FormData:**

- **Tự động trích xuất**: Lấy tất cả input (ẩn + hiện)
- **từ HTML**:

```html
<input type="hidden" name="productId" value="123" />
<input type="hidden" name="quantity" value="1" />
```

- **Kết quả**: Object FormData với productId=123, quantity=1
- **Content-Type**: Tự động đặt thành multipart/form-data

#### C. Thêm vào giỏ hàng không đồng bộ

```javascript
try {
  const response = await fetch("/cart/add", {
    method: "POST",
    body: formData,
  });

  if (response.ok) {
    updateCartCount(); // Cập nhật badge header
    showCartSuccessModal(); // Hiện popup xác nhận
  } else {
    alert("Có lỗi khi thêm vào giỏ hàng!");
  }
} catch (err) {
  alert("Có lỗi khi thêm vào giỏ hàng!");
}
```

**Chi tiết HTTP Request:**

- **Endpoint**: `/cart/add` → CartController.addToCart()
- **Method**: POST (cần thiết để thay đổi dữ liệu)
- **Body**: Object FormData với productId + quantity
- **response.ok**: HTTP status trong khoảng 200-299
- **Xử lý lỗi**: Lỗi mạng + HTTP errors

#### D. Cập nhật số lượng giỏ hàng real-time

```javascript
function updateCartCount() {
  fetch("/cart/count")
    .then((response) => response.json())
    .then((count) => {
      const cartCountSpan = document.getElementById("cart-count");
      if (cartCountSpan) cartCountSpan.textContent = count;
    });
}
```

**Quy trình cập nhật badge:**

1. **GET request** đến API `/cart/count`
2. **Phân tích JSON**: Trích xuất số lượng
3. **Cập nhật DOM**: Thay đổi text content của badge
4. **Phản hồi real-time**: User thấy thay đổi ngay lập tức

**HTML Badge:**

```html
<span id="cart-count">3</span>
<!-- Cập nhật thành 4 sau khi thêm -->
```

#### E. Hiển thị popup thành công

```javascript
function showCartSuccessModal() {
  const modal = new bootstrap.Modal(
    document.getElementById("cartSuccessModal")
  );
  modal.show();

  // Xử lý nút trong modal
  document.getElementById("continueShoppingBtn").onclick = function () {
    modal.hide();
  };
  document.getElementById("viewCartBtn").onclick = function () {
    window.location.href = "/cart";
  };
}
```

**Tích hợp Modal:**

- **Bootstrap Modal**: Sử dụng component modal của Bootstrap 5
- **HTML Modal**: Phải tồn tại trong DOM của trang

```html
<div id="cartSuccessModal" class="modal">
  <div class="modal-content">
    <h4>Thêm vào giỏ hàng thành công!</h4>
    <button id="continueShoppingBtn">Tiếp tục mua sắm</button>
    <button id="viewCartBtn">Xem giỏ hàng</button>
  </div>
</div>
```

**Hành động của nút:**

- **Tiếp tục mua sắm**: Ẩn modal, ở lại trang hiện tại
- **Xem giỏ hàng**: Chuyển đến trang `/cart`
- **Lựa chọn người dùng**: Workflow linh hoạt dựa trên ý định

### 🎠 Hệ thống Carousel và Swiper

#### A. Carousel hình ảnh dịch vụ - JavaScript thuần

```javascript
// CAROUSEL HÌNH ẢNH DỊCH VỤ
const slides = document.querySelectorAll(".carousel-images img");
let index = 0;

function showSlide(i) {
  slides.forEach((img, idx) => {
    img.classList.remove("active");
    if (idx === i) img.classList.add("active");
  });
}

setInterval(() => {
  index = (index + 1) % slides.length;
  showSlide(index);
}, 4000); // 4 giây mỗi hình ảnh
```

**Logic Carousel tự tạo:**

- **slides**: Danh sách tất cả hình ảnh carousel
- **index**: Slide đang hiển thị (bắt đầu từ 0)
- **showSlide()**: Hàm helper cập nhật trạng thái active
- **Toán tử modulo**: `(index + 1) % slides.length` để xoay vòng
- **setInterval**: Tự động chuyển sau mỗi 4 giây

**CSS Active State:**

```css
.carousel-images img {
  opacity: 0;
  transition: opacity 0.5s;
}
.carousel-images img.active {
  opacity: 1;
}
```

#### B. Swiper cho bài đăng blog - Thư viện bên ngoài

```javascript
// SWIPER CHO BÀI ĐĂNG BLOG
const swiper = new Swiper(".post-slider", {
  slidesPerView: 1, // Mobile: 1 slide
  spaceBetween: 30, // Khoảng cách 30px giữa các slide
  loop: true, // Vòng lặp vô hạn
  centeredSlides: true, // Căn giữa slide đang hoạt động
  autoplay: {
    delay: 3000, // 3 giây mỗi slide
    disableOnInteraction: false, // Tiếp tục sau khi user tương tác
  },
  breakpoints: {
    768: { slidesPerView: 2 }, // Tablet: 2 slides
    1024: { slidesPerView: 3 }, // Desktop: 3 slides
  },
});
```

**Phân tích cấu hình Swiper:**

**Thiết kế responsive:**

- **Mobile (< 768px)**: 1 slide mỗi lần hiển thị
- **Tablet (768px+)**: 2 slides mỗi lần hiển thị
- **Desktop (1024px+)**: 3 slides mỗi lần hiển thị
- **Tự động thích ứng**: Dựa trên chiều rộng màn hình

**Cài đặt tự động phát:**

- **delay: 3000**: 3 giây giữa các slide
- **disableOnInteraction: false**: Tiếp tục auto-play sau khi user thao tác
- **loop: true**: Cuộn vô hạn mượt mà
- **centeredSlides**: Slide active được căn giữa

### 💻 Tính năng JavaScript nâng cao

#### A. Sử dụng ES6+ hiện đại

```javascript
// HÀM MŨI TÊN (ARROW FUNCTIONS)
filterBtns.forEach((btn) => {
  btn.addEventListener("click", async function () {});
});

// ASYNC/AWAIT
const response = await fetch(`/api/best-seller?type=${type}`);
const products = await response.json();

// TEMPLATE LITERALS (CHUỖI MẪU)
const imageUrl = `/images/${product.image_url}`;
const priceHTML = `<div class="product-price">${formatPrice(
  product.price
)}₫</div>`;

// DESTRUCTURING ASSIGNMENT (PHÂN RÃ)
products.forEach((product, index) => {});

// TOÁN TỬ LOGIC OR
const type = this.dataset.type || "default";
```

**Giải thích ES6+ Features:**

- **Arrow functions**: Cú pháp ngắn gọn cho functions
- **Template literals**: Chuỗi với biến nhúng `${}`
- **Async/await**: Xử lý bất đồng bộ dễ đọc
- **Default values**: `||` cho giá trị mặc định

#### B. Tối ưu hóa hiệu suất

**Tránh Event Delegation problems:**

```javascript
// TRÁNH: Thêm listeners riêng cho nhiều phần tử
// products.forEach(product => {
//   product.addEventListener('click', handler);
// });

// TỐT HỊN: Gắn lại sau khi tạo động
const cartForms = productGrid.querySelectorAll('form[action="/cart/add"]');
cartForms.forEach((form) => {
  if (!form.dataset.listener) {
    attachCartFormListener(form);
    form.dataset.listener = "true";
  }
});
```

**Quản lý bộ nhớ:**

```javascript
// Dọn dẹp particles
setTimeout(() => flower.remove(), 10000);

// Ngăn listeners trùng lặp
if (!form.dataset.listener) {
  attachCartFormListener(form);
  form.dataset.listener = "true";
}
```

**Truy vấn DOM hiệu quả:**

```javascript
// Cache tham chiếu DOM
const filterBtns = document.querySelectorAll(".filter-btn");
const productGrid = document.querySelector("#best-seller-grid");

// Tránh truy vấn lặp lại trong vòng lặp
```

#### C. Chiến lược xử lý lỗi

**Xử lý lỗi mạng:**

```javascript
try {
  const response = await fetch(`/api/best-seller?type=${type}`);
  const products = await response.json();
  updateProductGrid(products);
} catch (error) {
  console.error("Lỗi khi tải sản phẩm:", error);
  // Giảm thiểu ảnh hưởng - giữ nội dung hiện tại
}
```

**Xử lý lỗi DOM:**

```javascript
// Kiểm tra element tồn tại
if (!productGrid) {
  console.warn("Không tìm thấy product grid");
  return;
}

// Xử lý lỗi hình ảnh
onerror = "this.src='/images/logo.png'";
```

### 🚀 Khởi tạo và lifecycle

#### A. Khởi tạo khi DOM sẵn sàng

```javascript
document.addEventListener("DOMContentLoaded", function () {
  // Tìm và khởi tạo các phần tử
  productGrid = document.querySelector(".product-grid");
  if (!productGrid) {
    console.warn("Không tìm thấy product grid");
    return;
  }

  // Gắn sự kiện cho bộ lọc
  initializeFilters();

  // Khởi tạo carousel
  initializeProductCarousel();

  // Gắn sự kiện giỏ hàng
  attachCartEvents();
});
```

#### B. Khởi tạo hệ thống bộ lọc

```javascript
function initializeFilters() {
  const filterButtons = document.querySelectorAll(".filter-btn");

  filterButtons.forEach((button) => {
    button.addEventListener("click", handleFilterClick);
  });

  // Tải sản phẩm mặc định
  loadDefaultProducts();
}
```

#### C. Xử lý sự kiện giỏ hàng cho sản phẩm có sẵn

```javascript
function attachCartEvents() {
  const existingCartForms = document.querySelectorAll(
    'form[action="/cart/add"]'
  );

  existingCartForms.forEach((form) => {
    if (!form.dataset.listener) {
      attachCartFormListener(form);
      form.dataset.listener = "true";
    }
  });
}
```

### 🔒 Bảo mật và validation

#### A. Làm sạch dữ liệu đầu vào

```javascript
function sanitizeProductData(product) {
  return {
    id: parseInt(product.id) || 0,
    name: String(product.name || "").trim(),
    price: parseFloat(product.price) || 0,
    image_url: String(product.image_url || "").trim(),
    // Escape HTML để tránh XSS
    description: escapeHtml(product.description || ""),
  };
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}
```

**Bảo mật dữ liệu:**

- **Validation kiểu**: Đảm bảo đúng kiểu dữ liệu
- **Giá trị mặc định**: Fallback cho null/undefined
- **HTML escaping**: Ngăn XSS attacks
- **Input sanitization**: Làm sạch dữ liệu không an toàn

#### B. Bảo vệ CSRF

```javascript
// Lấy CSRF token từ meta tag
function getCSRFToken() {
  return document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
}

// Thêm vào AJAX requests
fetch("/cart/add", {
  method: "POST",
  headers: {
    "X-CSRF-TOKEN": getCSRFToken(),
    "Content-Type": "application/x-www-form-urlencoded",
  },
  body: formData,
});
```

**Bảo vệ CSRF:**

- **CSRF Token**: Xác thực request hợp lệ
- **Meta tag**: Server render token vào HTML
- **Header injection**: Gửi token trong mỗi request
- **Security layer**: Ngăn chặn cross-site attacks
  }

````

**Null Safety:**

```javascript
// DOM element validation
if (!productGrid) return;

// Data validation
const imageUrl =
  product.image_url && product.image_url !== ""
    ? `/images/${product.image_url}`
    : "/images/logo.png";

// ID validation
if (product.id !== undefined && product.id !== null) {
  // Create link
} else {
  // Show error indicator
}
````

### 4.8 Integration với Backend APIs

#### A. API Endpoint Mapping

```javascript
// Filter best sellers
GET /api/best-seller?type=lang → HomeController.getBestSellerByType()

// Add to cart
POST /cart/add → CartController.addToCart()

// Get cart count
GET /cart/count → CartController.getCartItemCount()
```

#### B. Data Flow Architecture

```
Frontend JavaScript → HTTP Request → Spring Controller → Service Layer → Database
                   ← JSON Response ← JSON Serialization ← Business Logic ← SQL Query
```

#### C. State Management

```javascript
// UI State
filterBtns.forEach((button) => button.classList.remove("active"));
this.classList.add("active");

// Application State
updateCartCount(); // Sync with server state

// Visual State
showCartSuccessModal(); // User feedback
```

### 4.9 Browser Compatibility

#### A. Modern JavaScript Features

- **Fetch API**: IE không support, cần polyfill
- **async/await**: ES2017, modern browsers only
- **Template literals**: ES6, widely supported
- **Arrow functions**: ES6, modern browsers

#### B. Fallbacks và Polyfills

```javascript
// Fetch polyfill for older browsers
if (!window.fetch) {
  // Load fetch polyfill
}

// Event listener compatibility
document.addEventListener("DOMContentLoaded", function () {
  // Supported in IE9+
});
```

#### C. Progressive Enhancement

- **Core functionality**: Works without JavaScript
- **Enhanced experience**: JavaScript adds interactivity
- **Graceful degradation**: Basic cart still works nếu JS disabled

## 5. ĐẶC ĐIỂM BUSINESS LOGIC VÀ DATABASE

### 🗃️ Chiến lược truy vấn sản phẩm

#### A. Tìm sản phẩm mới nhất mỗi danh mục

```sql
-- Native SQL Query trong ProductDAO
SELECT TOP 6 p.* FROM products p
INNER JOIN (
    SELECT product_Category_Id, MAX(id) as max_id
    FROM products
    GROUP BY product_Category_Id
) latest ON p.product_Category_Id = latest.product_Category_Id
AND p.id = latest.max_id
ORDER BY p.product_Category_Id
```

**Giải thích logic:**

- **Subquery**: Tìm ID lớn nhất (mới nhất) trong mỗi category
- **JOIN**: Lấy full record của products có ID max
- **Kết quả**: 6 sản phẩm mới nhất từ 6 category khác nhau

#### B. Tìm sản phẩm bán chạy nhất mỗi danh mục

```sql
-- CÁCH ĐÚNG: Dựa trên số lượng đã bán từ order_details
SELECT p.* FROM products p
JOIN (
    SELECT p2.product_Category_Id,
           p2.id as product_id,
           SUM(od.quantity) AS total_sold,
           ROW_NUMBER() OVER (PARTITION BY p2.product_Category_Id ORDER BY SUM(od.quantity) DESC) as rn
    FROM products p2
    JOIN order_details od ON p2.id = od.product_id
    JOIN orders o ON od.order_id = o.id
    WHERE o.status = 'COMPLETED'
    GROUP BY p2.product_Category_Id, p2.id
) sales ON p.id = sales.product_id
WHERE sales.rn = 1
```

**Logic business đúng:**

- **Dựa trên order_details**: Số lượng thực tế đã bán
- **SUM(od.quantity)**: Tổng số lượng bán ra từ tất cả đơn hàng
- **ROW_NUMBER()**: Xếp hạng trong mỗi category
- **status = 'COMPLETED'**: Chỉ tính đơn hàng đã hoàn thành
- **WHERE rn = 1**: Lấy sản phẩm bán chạy nhất mỗi danh mục

**⚠️ Lưu ý**: Query cũ dùng `p.quantity` là **SAI** vì:

- `quantity` trong bảng `products` = **số lượng tồn kho**
- **Không phải** số lượng đã bán ra
- Cần dùng dữ liệu từ `order_details` để có số liệu bán hàng chính xác

#### C. Lọc theo danh mục động

```sql
-- Nếu muốn sắp xếp theo "bán chạy" thực sự:
SELECT TOP 10 p.* FROM products p
INNER JOIN product_categories pc ON p.product_Category_Id = pc.id
LEFT JOIN (
    SELECT od.product_id, SUM(od.quantity) as total_sold
    FROM order_details od
    JOIN orders o ON od.order_id = o.id
    WHERE o.status = 'COMPLETED'
    GROUP BY od.product_id
) sales ON p.id = sales.product_id
WHERE pc.name = :categoryName
ORDER BY COALESCE(sales.total_sold, 0) DESC

-- HOẶC nếu muốn sắp xếp theo tồn kho (quantity):
SELECT TOP 10 p.* FROM products p
INNER JOIN product_categories pc ON p.product_Category_Id = pc.id
WHERE pc.name = :categoryName
ORDER BY p.quantity DESC  -- quantity = số lượng tồn kho
```

**Lọc danh mục linh hoạt:**

- **Parameter binding**: Ngăn SQL injection
- **TOP 10 limit**: Tối ưu hiệu suất
- **ORDER BY sales.total_sold**: Sắp xếp theo số lượng **đã bán**
- **ORDER BY p.quantity**: Sắp xếp theo số lượng **tồn kho**
- **COALESCE(sales.total_sold, 0)**: Xử lý sản phẩm chưa bán lần nào

### 💰 Logic khuyến mãi và giảm giá

#### A. Tìm promotion hợp lệ

```java
// PromotionService implementation
List<Promotion> validPromotions = promotionDAO.findByStatusTrueAndEndDateAfter(LocalDate.now());
return validPromotions.stream()
    .filter(p -> p.getStartDate().isBefore(LocalDate.now()) || p.getStartDate().isEqual(LocalDate.now()))
    .collect(Collectors.toList());
```

**Quy tắc validation:**

- `status = true`: Chỉ promotion đang hoạt động
- `endDate > now()`: Chưa hết hạn
- `startDate <= now()`: Đã bắt đầu

#### B. Lọc sản phẩm giảm giá

```java
List<Product> findTop4ByDiscountPercentGreaterThanAndAvailableIsTrueOrderByDiscountPercentDesc(int minDiscount) {
    List<Product> allDiscountProducts = dao.findDiscountProductsExcludeCategory(minDiscount, "phụ kiện");
    return allDiscountProducts.stream().limit(4).toList();
}
```

**Lọc nhiều lớp:**

1. **discountPercent > 0**: Có giảm giá
2. **available = true**: Còn hàng
3. **exclude "phụ kiện"**: Quy tắc business
4. **ORDER BY discountPercent DESC**: Giảm giá cao nhất trước
5. **LIMIT 4**: Tối ưu hiệu suất + giới hạn UI

### 🔍 Hệ thống tìm kiếm thông minh

#### A. Chiến lược tìm kiếm cascade

```java
@GetMapping("/search")
public String search(@RequestParam String keyword, Pageable pageable, Model model) {
    Page<Product> resultPage = Page.empty();

    // TIER 1: Tìm theo tên category
    resultPage = productService.searchByCategoryName(keyword, pageable);

    if (resultPage.isEmpty()) {
        // TIER 2: Tìm theo tên product category
        resultPage = productService.searchByProductCategoryName(keyword, pageable);
    }

    if (resultPage.isEmpty()) {
        // TIER 3: Tìm theo tên sản phẩm
        resultPage = productService.searchByName(keyword, pageable);
    }

    return resultPage;
}
```

**Ưu tiên tìm kiếm:**

1. **Category Match**: "Lãng hoa", "Bó hoa" → Độ liên quan cao
2. **Product Category Match**: "Hoa sinh nhật" → Độ liên quan trung bình
3. **Product Name Match**: "Hoa hồng đỏ" → Độ liên quan thấp

## 6. TỐI ƯU HÓA HIỆU SUẤT

### ⚡ Tối ưu Database

#### A. Chiến lược indexing

```sql
-- Indexes quan trọng cho performance
CREATE INDEX idx_product_category_id ON products(product_Category_Id);
CREATE INDEX idx_product_quantity ON products(quantity);
CREATE INDEX idx_product_discount ON products(discountPercent);
CREATE INDEX idx_product_available ON products(available);
```

#### B. Caching cho dữ liệu tĩnh

```java
// Có thể implement Redis caching
@Cacheable("bestSellingProducts")
public List<Product> findBestSellingProductPerCategory() {
    return dao.findBestSellingProductPerCategory();
}

@Cacheable("latestProducts")
public List<Product> findLatestProductsPerCategory() {
    return dao.findLatestProductsPerCategory();
}
```

### 🖼️ Tối ưu Frontend

#### A. Lazy loading cho hình ảnh

```html
<img
  th:src="${product.image_url}"
  loading="lazy"
  onerror="this.src='/images/logo.png'"
  style="width: 100%; height: 200px; object-fit: cover"
/>
```

#### B. Debouncing cho filter

```javascript
let filterTimeout;
function handleFilterClick(e) {
  clearTimeout(filterTimeout);
  filterTimeout = setTimeout(() => {
    doActualFilter(e.target);
  }, 250);
}
```

## 7. BẢO MẬT VÀ VALIDATION

### 🔒 Xác thực đầu vào

```java
@GetMapping("/search")
public String search(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        Model model) {

    // Validate và sanitize keyword
    if (keyword != null) {
        keyword = keyword.trim();
        if (keyword.length() > 100) {
            keyword = keyword.substring(0, 100);
        }
    }
}
```

### 🛡️ Ngăn chặn XSS

```html
<!-- th:text ngăn XSS -->
<div th:text="${product.name}">Tên sản phẩm</div>

<!-- th:utext chỉ dùng cho nội dung tin cậy -->
<span th:utext="${post.summary}"></span>
```

## 8. TÍCH HỢP VỚI CÁC MODULE KHÁC

### 👤 Tích hợp Authentication

```java
User user = authService.getUser();
if (user != null) {
    Integer userId = user.getId();
    cartCount = cartItemService.getCartItemsByUserId(userId).size();
}
```

### 🛒 Tích hợp hệ thống giỏ hàng

```javascript
// Cập nhật giỏ hàng real-time
function updateCartCount() {
  fetch("/cart/count")
    .then((response) => response.json())
    .then((count) => {
      document.getElementById("cart-count").textContent = count;
    });
}
```

## 9. XỬ LÝ LỖI VÀ FALLBACKS

### 🖼️ Fallback cho hình ảnh

```html
<img
  th:src="${product.image_url != null and product.image_url != '' ? 
             '/images/' + product.image_url : '/images/logo.png'}"
  onerror="this.src='/images/logo.png'; this.onerror=null;"
/>
```

### 📊 Fallback cho dữ liệu

```html
<div
  th:text="${product.productCategory != null ? 
               product.productCategory.name : 'Không có danh mục'}"
>
  Danh mục
</div>
```

### ⚠️ Xử lý lỗi JavaScript

```javascript
try {
  const response = await fetch(`/api/best-seller?type=${type}`);
  const products = await response.json();
  updateProductGrid(products);
} catch (error) {
  console.error("Lỗi khi tải sản phẩm:", error);
  // Fallback: Giữ sản phẩm hiện tại
}
```

## 10. GIÁM SÁT VÀ ANALYTICS

### 📊 Theo dõi hiệu suất

- **Thời gian tải trang**: Mục tiêu < 3 giây
- **Thời gian query database**: < 100ms mỗi query
- **Thời gian phản hồi API**: < 500ms
- **Tải hình ảnh**: Progressive loading

### 📈 Theo dõi tương tác người dùng

```javascript
// Theo dõi clicks trên filter
filterBtns.forEach((btn) => {
  btn.addEventListener("click", function () {
    // Analytics tracking
    gtag("event", "filter_click", {
      filter_type: this.dataset.type,
      section: "best_seller",
    });
  });
});
```

## 11. HƯỚNG PHÁT TRIỂN TƯƠNG LAI

### 🎯 Cá nhân hóa

- **Theo dõi hành vi user**: Track sản phẩm đã xem
- **Hệ thống gợi ý**: Đề xuất dựa trên lịch sử
- **Nội dung động**: Best sellers cá nhân hóa

### 🚀 Tính năng nâng cao

- **Infinite scroll**: Thay thế pagination
- **Cập nhật tồn kho real-time**: Live stock updates
- **Progressive Web App**: Khả năng offline
- **Tìm kiếm nâng cao**: Filters, sorting, facets

---

## 📝 TÓM TẮT KIẾN TRÚC

Trang chủ Karina Hana được xây dựng theo mô hình **MVC hiện đại** với:

- **Backend**: Spring Boot + Thymeleaf cho server-side rendering
- **Frontend**: JavaScript ES6+ cho tương tác động
- **Database**: SQL Server với queries tối ưu
- **Security**: CSRF protection, XSS prevention, input validation
- **Performance**: Lazy loading, caching, debouncing
- **User Experience**: Smooth animations, real-time updates, responsive design

Hệ thống đảm bảo **hiệu suất cao**, **bảo mật tốt** và **trải nghiệm người dùng mượt mà** thông qua việc tích hợp chặt chẽ giữa backend và frontend components.

- **Microservices**: Split monolith if needed

## 12. KẾT LUẬN

Trang chủ Karina Hana là một **landing page phức tạp và toàn diện** với:

### 12.1 Điểm mạnh

- **Rich content showcase**: Nhiều loại sản phẩm và category
- **Dynamic interactions**: AJAX, filters, real-time updates
- **Responsive design**: Mobile-first approach
- **SEO optimization**: Proper meta tags và structure
- **Performance**: Optimized queries và caching strategy

### 12.2 Architecture highlights

- **MVC pattern**: Clean separation of concerns
- **Service layer**: Business logic encapsulation
- **Data layer**: Optimized database queries
- **Frontend**: Modern JavaScript với progressive enhancement

### 12.3 Business value

- **User engagement**: Interactive features keep users on site
- **Conversion optimization**: Easy add-to-cart flows
- **Marketing integration**: Promotions và discount codes
- **Content marketing**: Blog posts và customer reviews

Trang chủ này đại diện cho một **e-commerce homepage hiện đại** với đầy đủ features cần thiết cho business thành công trong lĩnh vực bán hoa online.
