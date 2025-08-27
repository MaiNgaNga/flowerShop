# HỆ THỐNG ĐÁNH GIÁ ĐƠN HÀNG - PHÂN TÍCH TOÀN DIỆN

## 📋 TỔNG QUAN HỆ THỐNG

Hệ thống đánh giá đơn hàng là một **module độc lập** cho phép khách hàng đưa ra feedback về chất lượng dịch vụ sau khi đơn hàng được giao thành công. Hệ thống này bao gồm **rating 1-5 sao**, **bình luận chi tiết** và **quy trình kiểm duyệt của admin**.

### 🎯 Mục tiêu chính:

- Thu thập feedback từ khách hàng về chất lượng dịch vụ
- Tạo độ tin cậy thông qua review công khai
- Cung cấp công cụ quản lý chất lượng cho admin
- Hiển thị top reviews tại homepage để tăng conversion

---

## 🏗️ I. KIẾN TRÚC DATABASE

### 1. Entity Comment.java

```java
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User relationship - ai đánh giá
    @NotNull(message = "Đăng nhập để bình luận")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nội dung đánh giá
    @NotBlank(message = "Nội dung không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String content;

    // Rating 1-5 sao với validation
    @NotNull(message = "Vui lòng chọn số sao")
    @Min(value = 1, message = "Số sao phải từ 1 đến 5")
    @Max(value = 5, message = "Số sao phải từ 1 đến 5")
    private Integer rating;

    // Timestamp tự động
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Trạng thái kiểm duyệt
    @Column(name = "status")
    private String status; // "Active" hoặc "Inactive"

    // Liên kết với đơn hàng
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Constructors, getters, setters...
}
```

### 2. Relationship trong Order.java

```java
@Entity
@Table(name = "orders")
public class Order {
    // ... other fields

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Comment> comments;

    // Helper method
    public boolean hasComments() {
        return comments != null && !comments.isEmpty();
    }
}
```

### 3. Database Schema

```sql
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content NVARCHAR(255) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'Inactive',
    order_id BIGINT NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

---

## ⚙️ II. SERVICE LAYER

### 1. Interface CommentService.java

```java
public interface CommentService {
    // CRUD Operations
    List<Comment> getCommentsByOrder(Long orderId);
    Comment saveComment(String content, Long orderID, Integer rating);
    void deleteComment(Long id);
    void updateComment(Long id, Comment comment);
    Comment findById(Long id);

    // Business Logic
    List<Comment> getTop3Comments();
    Page<Comment> findByStatusInactive(Pageable pageable);

    // Validation
    boolean canUserReviewOrder(Long userId, Long orderId);
    boolean hasUserReviewedOrder(Long userId, Long orderId);
}
```

### 2. Implementation CommentServiceImpl.java

```java
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private OrderService orderService;

    @Override
    public Comment saveComment(String content, Long orderID, Integer rating) {
        // Lấy user hiện tại từ session
        User user = authService.getUser();
        if (user == null) {
            throw new RuntimeException("User chưa đăng nhập");
        }

        // Kiểm tra order tồn tại
        Order order = orderService.findByID(orderID);
        if (order == null) {
            throw new RuntimeException("Đơn hàng không tồn tại");
        }

        // Kiểm tra user có quyền đánh giá order này không
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này");
        }

        // Kiểm tra order đã giao thành công chưa
        if (!"Đã giao".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể đánh giá đơn hàng đã giao thành công");
        }

        // Kiểm tra đã đánh giá chưa
        if (hasUserReviewedOrder(user.getId(), orderID)) {
            throw new RuntimeException("Bạn đã đánh giá đơn hàng này rồi");
        }

        // Tạo comment mới
        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setUser(user);
        comment.setOrder(order);
        comment.setRating(rating);
        comment.setStatus("Inactive"); // Cần admin duyệt
        comment.setCreatedAt(LocalDateTime.now());

        return commentDAO.save(comment);
    }

    @Override
    public List<Comment> getCommentsByOrder(Long orderId) {
        Order order = orderService.findByID(orderId);
        return commentDAO.findByOrderAndStatus(order, "Active");
    }

    @Override
    public List<Comment> getTop3Comments() {
        return commentDAO.findTop3ByStatusOrderByCreatedAtDescRatingDesc("Active");
    }

    @Override
    public Page<Comment> findByStatusInactive(Pageable pageable) {
        return commentDAO.findByStatus("Inactive", pageable);
    }

    @Override
    public boolean hasUserReviewedOrder(Long userId, Long orderId) {
        return commentDAO.existsByUserIdAndOrderId(userId, orderId);
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentDAO.findById(id)
            .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));

        User currentUser = authService.getUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn chỉ có thể xóa comment của mình");
        }

        commentDAO.delete(comment);
    }

    @Override
    public void updateComment(Long id, Comment updatedComment) {
        Comment existingComment = commentDAO.findById(id)
            .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));

        existingComment.setStatus(updatedComment.getStatus());
        commentDAO.save(existingComment);
    }
}
```

---

## 🗃️ III. DATA ACCESS LAYER

### CommentDAO.java

```java
@Repository
public interface CommentDAO extends JpaRepository<Comment, Long> {

    // Lấy comments của 1 order theo status
    @Query("SELECT c FROM Comment c WHERE c.order = ?1 AND c.status = ?2 ORDER BY c.createdAt DESC")
    List<Comment> findByOrderAndStatus(Order order, String status);

    // Lấy tất cả comments của 1 order
    @Query("SELECT c FROM Comment c WHERE c.order = ?1 ORDER BY c.createdAt DESC")
    List<Comment> findByOrder(Order order);

    // Top 3 comments active cho homepage (ưu tiên rating cao + mới nhất)
    @Query("SELECT c FROM Comment c WHERE c.status = ?1 ORDER BY c.rating DESC, c.createdAt DESC LIMIT 3")
    List<Comment> findTop3ByStatusOrderByCreatedAtDescRatingDesc(String status);

    // Comments chưa duyệt cho admin (có phân trang)
    @Query("SELECT c FROM Comment c WHERE c.status = ?1 ORDER BY c.createdAt ASC")
    Page<Comment> findByStatus(String status, Pageable pageable);

    // Kiểm tra user đã review order chưa
    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.user.id = ?1 AND c.order.id = ?2")
    boolean existsByUserIdAndOrderId(Long userId, Long orderId);

    // Thống kê số lượng comments theo status
    @Query("SELECT c.status, COUNT(c) FROM Comment c GROUP BY c.status")
    List<Object[]> countByStatus();

    // Lấy comments theo user
    @Query("SELECT c FROM Comment c WHERE c.user.id = ?1 ORDER BY c.createdAt DESC")
    List<Comment> findByUserId(Long userId);

    // Tìm comments theo nội dung (cho admin search)
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %?1% ORDER BY c.createdAt DESC")
    Page<Comment> findByContentContaining(String keyword, Pageable pageable);
}
```

---

## 🎮 IV. CONTROLLER LAYER

### 1. HistoryController.java - User Side

```java
@Controller
public class HistoryController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private OrderService orderService;

    /**
     * Hiển thị trang lịch sử đơn hàng với form đánh giá
     */
    @GetMapping("/history")
    public String history(Model model, @RequestParam(value = "id", required = false) Long orderId) {
        User user = authService.getUser();

        if (orderId != null) {
            // Hiển thị chi tiết 1 đơn hàng
            Order order = orderService.findByID(orderId);
            if (order != null && order.getUser().getId().equals(user.getId())) {
                model.addAttribute("order", order);

                // Load comments của order này
                List<Comment> comments = commentService.getCommentsByOrder(orderId);
                model.addAttribute("comments", comments);

                // Kiểm tra có thể đánh giá không
                boolean canReview = "Đã giao".equals(order.getStatus()) &&
                                  !commentService.hasUserReviewedOrder(user.getId(), orderId);
                model.addAttribute("canReview", canReview);
            }
        } else {
            // Hiển thị tất cả đơn hàng
            List<Order> orders = orderService.findByUser(user);
            model.addAttribute("orders", orders);
        }

        return "history";
    }

    /**
     * Submit đánh giá đơn hàng
     */
    @PostMapping("/history/comment")
    public String submitComment(Model model,
                               @RequestParam("comment") String content,
                               @RequestParam("orderId") Long orderId,
                               @RequestParam("rating") Integer rating,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validation cơ bản
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nội dung bình luận không được để trống.");
                return "redirect:/history?id=" + orderId;
            }

            if (rating == null || rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn số sao từ 1 đến 5.");
                return "redirect:/history?id=" + orderId;
            }

            // Kiểm tra đơn hàng tồn tại
            if (orderService.findByID(orderId) == null) {
                redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại");
                return "redirect:/history?id=" + orderId;
            }

            // Lưu comment
            commentService.saveComment(content, orderId, rating);
            redirectAttributes.addFlashAttribute("message", "Cảm ơn bạn đã đánh giá sản phẩm.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
            e.printStackTrace();
        }

        return "redirect:/history?id=" + orderId;
    }    /**
     * Xóa comment của user
     */
    @GetMapping("/history/comment/delete/{id}")
    public String deleteComment(@PathVariable("id") Long commentId,
                               @RequestParam(value = "orderId", required = false) Long orderId,
                               RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(commentId);
            redirectAttributes.addFlashAttribute("message", "Đã xóa bình luận thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa bình luận: " + e.getMessage());
        }

        if (orderId != null) {
            return "redirect:/history?id=" + orderId;
        }
        return "redirect:/history";
    }
}
```

### 2. ReviewController.java - Admin Side

```java
@RequestMapping("/admin/reviews")
@Controller
public class ReviewController {

    @Autowired
    private CommentService commentService;

    /**
     * Trang quản lý đánh giá chờ duyệt
     */
    @GetMapping("/index")
    public String reviewIndex(Model model,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "status", defaultValue = "Inactive") String status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage;

        if ("Inactive".equals(status)) {
            commentPage = commentService.findByStatusInactive(pageable);
        } else {
            // Có thể mở rộng để xem cả Active comments
            commentPage = commentService.findByStatusInactive(pageable);
        }

        model.addAttribute("commentPage", commentPage.getContent());
        model.addAttribute("totalPages", commentPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", commentPage.getTotalElements());
        model.addAttribute("status", status);

        // Breadcrumb cho admin layout
        model.addAttribute("pageTitle", "Quản lý đánh giá");
        model.addAttribute("currentView", "admin/review");

        return "admin/layout";
    }

    /**
     * Duyệt comment (chuyển từ Inactive → Active)
     */
    @GetMapping("/approve/{id}")
    public String approveComment(@PathVariable("id") Long commentId,
                                @RequestParam(value = "status", defaultValue = "Inactive") String status,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                RedirectAttributes redirectAttributes) {
        try {
            Comment comment = commentService.findById(commentId);
            if (comment != null) {
                comment.setStatus("Active");
                commentService.updateComment(comment.getId(), comment);
                redirectAttributes.addFlashAttribute("success",
                    "Đã phê duyệt đánh giá của " + comment.getUser().getName());
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi duyệt đánh giá: " + e.getMessage());
        }

        return "redirect:/admin/reviews/index?status=" + status + "&page=" + page;
    }

    /**
     * Từ chối comment (xóa luôn)
     */
    @GetMapping("/reject/{id}")
    public String rejectComment(@PathVariable("id") Long commentId,
                               @RequestParam(value = "status", defaultValue = "Inactive") String status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               RedirectAttributes redirectAttributes) {
        try {
            Comment comment = commentService.findById(commentId);
            if (comment != null) {
                commentService.deleteComment(commentId);
                redirectAttributes.addFlashAttribute("success",
                    "Đã từ chối đánh giá của " + comment.getUser().getName());
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi từ chối đánh giá: " + e.getMessage());
        }

        return "redirect:/admin/reviews/index?status=" + status + "&page=" + page;
    }

    /**
     * Xem chi tiết đánh giá và đơn hàng liên quan
     */
    @GetMapping("/detail/{id}")
    public String reviewDetail(@PathVariable("id") Long commentId, Model model) {
        Comment comment = commentService.findById(commentId);
        if (comment != null) {
            model.addAttribute("comment", comment);
            model.addAttribute("order", comment.getOrder());
            model.addAttribute("user", comment.getUser());
        }

        model.addAttribute("pageTitle", "Chi tiết đánh giá");
        model.addAttribute("currentView", "admin/review-detail");

        return "admin/layout";
    }
}
```

---

## 🎨 V. FRONTEND IMPLEMENTATION

### 1. Star Rating Component trong history.html

```html
<div class="review-section" th:if="${order.status == 'Đã giao' and canReview}">
  <div class="card shadow-sm">
    <div class="card-header bg-primary text-white">
      <h5 class="mb-0"><i class="fas fa-star me-2"></i>Đánh giá đơn hàng</h5>
    </div>
    <div class="card-body">
      <form th:action="@{/history/comment}" method="post" class="review-form">
        <input type="hidden" name="orderId" th:value="${order.id}" />

        <!-- Star Rating -->
        <div class="mb-3">
          <label class="form-label fw-bold">Đánh giá chất lượng dịch vụ:</label>
          <div class="star-rating-container">
            <div class="star-rating-custom">
              <span data-value="1">&#9733;</span>
              <span data-value="2">&#9733;</span>
              <span data-value="3">&#9733;</span>
              <span data-value="4">&#9733;</span>
              <span data-value="5">&#9733;</span>
            </div>
            <span class="rating-text ms-3 text-muted"
              >Vui lòng chọn số sao</span
            >
          </div>
          <input type="hidden" name="rating" class="ratingInput" required />
          <div class="invalid-feedback rating-error" style="display: none;">
            Vui lòng chọn số sao đánh giá!
          </div>
        </div>

        <!-- Comment Content -->
        <div class="mb-3">
          <label for="comment" class="form-label fw-bold"
            >Nhận xét chi tiết:</label
          >
          <textarea
            class="form-control"
            name="comment"
            id="comment"
            rows="4"
            placeholder="Chia sẻ trải nghiệm của bạn về dịch vụ..."
            maxlength="255"
            required
          ></textarea>
          <div class="form-text">Tối đa 255 ký tự</div>
        </div>

        <!-- Submit Button -->
        <div class="d-grid">
          <button type="submit" class="btn btn-primary btn-lg">
            <i class="fas fa-paper-plane me-2"></i>Gửi đánh giá
          </button>
        </div>
      </form>
    </div>
  </div>
</div>

<!-- Hiển thị đánh giá đã có -->
<div class="existing-reviews mt-4" th:if="${not #lists.isEmpty(comments)}">
  <h5 class="mb-3"><i class="fas fa-comments me-2"></i>Đánh giá của bạn</h5>

  <div class="comment-list">
    <div class="comment-item card mb-3" th:each="item : ${comments}">
      <div class="card-body">
        <div class="d-flex justify-content-between align-items-start">
          <div class="comment-content flex-grow-1">
            <!-- User Info & Rating -->
            <div class="comment-header mb-2">
              <div class="d-flex align-items-center">
                <strong class="me-3" th:text="${item.user.name}"></strong>

                <!-- Display Stars -->
                <div class="stars-display me-3">
                  <span
                    th:each="star : ${#numbers.sequence(1, 5)}"
                    th:class="${star <= item.rating} ? 'star-filled' : 'star-empty'"
                  >
                    &#9733;
                  </span>
                </div>

                <small class="text-muted">
                  <i class="fas fa-clock me-1"></i>
                  <span
                    th:text="${#temporals.format(item.createdAt, 'dd/MM/yyyy HH:mm')}"
                  ></span>
                </small>
              </div>
            </div>

            <!-- Comment Text -->
            <p class="comment-text mb-2" th:text="${item.content}"></p>

            <!-- Status Badge -->
            <span th:if="${item.status == 'Inactive'}" class="badge bg-warning">
              <i class="fas fa-clock me-1"></i>Đang chờ duyệt
            </span>
            <span th:if="${item.status == 'Active'}" class="badge bg-success">
              <i class="fas fa-check me-1"></i>Đã duyệt
            </span>
          </div>

          <!-- Action Menu -->
          <div class="dropdown">
            <button
              class="btn btn-sm btn-outline-secondary dropdown-toggle"
              type="button"
              data-bs-toggle="dropdown"
            >
              <i class="fas fa-ellipsis-v"></i>
            </button>
            <ul class="dropdown-menu dropdown-menu-end">
              <li>
                <a
                  class="dropdown-item text-danger"
                  th:href="@{/history/comment/delete/{id}(id=${item.id}, orderId=${order.id})}"
                  onclick="return confirm('Bạn có chắc muốn xóa đánh giá này?')"
                >
                  <i class="fas fa-trash me-2"></i>Xóa
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

### 2. JavaScript cho Star Rating System

```javascript
document.addEventListener("DOMContentLoaded", function () {
  // Initialize all review forms
  initializeReviewForms();
});

function initializeReviewForms() {
  const reviewForms = document.querySelectorAll("form.review-form");

  reviewForms.forEach(function (form) {
    const stars = form.querySelectorAll(".star-rating-custom span");
    const ratingInput = form.querySelector(".ratingInput");
    const ratingText = form.querySelector(".rating-text");
    const ratingError = form.querySelector(".rating-error");

    let selectedRating = 0;

    // Rating text mapping
    const ratingTexts = {
      1: "Rất không hài lòng",
      2: "Không hài lòng",
      3: "Bình thường",
      4: "Hài lòng",
      5: "Rất hài lòng",
    };

    // Star hover effects
    stars.forEach((star, index) => {
      const value = parseInt(star.getAttribute("data-value"));

      // Mouse enter
      star.addEventListener("mouseenter", function () {
        highlightStars(stars, value);
        ratingText.textContent = ratingTexts[value];
        ratingText.className = "rating-text ms-3 text-warning fw-bold";
      });

      // Mouse leave
      star.addEventListener("mouseleave", function () {
        if (selectedRating > 0) {
          highlightStars(stars, selectedRating);
          ratingText.textContent = ratingTexts[selectedRating];
          ratingText.className = "rating-text ms-3 text-success fw-bold";
        } else {
          resetStars(stars);
          ratingText.textContent = "Vui lòng chọn số sao";
          ratingText.className = "rating-text ms-3 text-muted";
        }
      });

      // Click to select
      star.addEventListener("click", function () {
        selectedRating = value;
        ratingInput.value = selectedRating;
        highlightStars(stars, selectedRating);
        ratingText.textContent = ratingTexts[selectedRating];
        ratingText.className = "rating-text ms-3 text-success fw-bold";

        // Hide error if shown
        if (ratingError) {
          ratingError.style.display = "none";
        }
      });
    });

    // Form submission validation
    form.addEventListener("submit", function (e) {
      let isValid = true;

      // Check rating
      if (
        !ratingInput.value ||
        isNaN(ratingInput.value) ||
        ratingInput.value < 1 ||
        ratingInput.value > 5
      ) {
        e.preventDefault();
        isValid = false;

        if (ratingError) {
          ratingError.style.display = "block";
        }

        // Shake effect for stars
        const starContainer = form.querySelector(".star-rating-custom");
        starContainer.classList.add("shake");
        setTimeout(() => starContainer.classList.remove("shake"), 500);
      }

      // Check comment content
      const commentTextarea = form.querySelector('textarea[name="comment"]');
      if (!commentTextarea.value.trim()) {
        e.preventDefault();
        isValid = false;
        commentTextarea.focus();
      }

      if (!isValid) {
        // Show alert
        showAlert("Vui lòng điền đầy đủ thông tin đánh giá!", "warning");
        return false;
      }
    });
  });
}

function highlightStars(stars, rating) {
  stars.forEach((star, index) => {
    const value = parseInt(star.getAttribute("data-value"));
    if (value <= rating) {
      star.style.color = "#ffc107";
      star.classList.add("star-selected");
    } else {
      star.style.color = "#ddd";
      star.classList.remove("star-selected");
    }
  });
}

function resetStars(stars) {
  stars.forEach((star) => {
    star.style.color = "#ddd";
    star.classList.remove("star-selected");
  });
}

function showAlert(message, type = "info") {
  // Create alert element
  const alertDiv = document.createElement("div");
  alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
  alertDiv.style.cssText =
    "top: 20px; right: 20px; z-index: 9999; min-width: 300px;";
  alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

  document.body.appendChild(alertDiv);

  // Auto remove after 5 seconds
  setTimeout(() => {
    if (alertDiv.parentNode) {
      alertDiv.remove();
    }
  }, 5000);
}
```

### 3. CSS Styling

```css
/* Star Rating Styles */
.star-rating-custom {
  display: inline-flex;
  gap: 4px;
  font-size: 2rem;
  cursor: pointer;
}

.star-rating-custom span {
  color: #ddd;
  transition: all 0.3s ease;
  user-select: none;
}

.star-rating-custom span:hover,
.star-rating-custom span.star-selected {
  color: #ffc107;
  transform: scale(1.1);
}

.star-rating-custom span:active {
  transform: scale(0.95);
}

/* Stars Display (readonly) */
.stars-display {
  font-size: 1rem;
}

.star-filled {
  color: #ffc107;
}

.star-empty {
  color: #ddd;
}

/* Animation Effects */
@keyframes shake {
  0%,
  20%,
  40%,
  60%,
  80% {
    transform: translateX(-2px);
  }
  10%,
  30%,
  50%,
  70%,
  90% {
    transform: translateX(2px);
  }
}

.shake {
  animation: shake 0.5s ease-in-out;
}

/* Review Card Styles */
.review-section .card {
  border: none;
  border-radius: 12px;
}

.comment-item {
  border-left: 4px solid #007bff;
  transition: transform 0.2s ease;
}

.comment-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.comment-content {
  line-height: 1.6;
}

.rating-text {
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

/* Responsive Design */
@media (max-width: 768px) {
  .star-rating-custom {
    font-size: 1.5rem;
  }

  .rating-text {
    display: block;
    margin-top: 8px;
    margin-left: 0 !important;
  }
}
```

---

## 🔧 VI. ADMIN INTERFACE

### 1. Admin Review Management (admin/review.html)

```html
<div class="content-wrapper">
  <!-- Page Header -->
  <div class="content-header">
    <div class="container-fluid">
      <div class="row mb-2">
        <div class="col-sm-6">
          <h1 class="m-0">Quản lý đánh giá</h1>
        </div>
        <div class="col-sm-6">
          <ol class="breadcrumb float-sm-right">
            <li class="breadcrumb-item"><a href="/admin">Dashboard</a></li>
            <li class="breadcrumb-item active">Đánh giá</li>
          </ol>
        </div>
      </div>
    </div>
  </div>

  <!-- Main Content -->
  <section class="content">
    <div class="container-fluid">
      <!-- Stats Cards -->
      <div class="row mb-4">
        <div class="col-lg-3 col-6">
          <div class="small-box bg-warning">
            <div class="inner">
              <h3 th:text="${totalElements}">0</h3>
              <p>Đánh giá chờ duyệt</p>
            </div>
            <div class="icon">
              <i class="fas fa-clock"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- Filter & Search -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title"><i class="fas fa-filter mr-2"></i>Bộ lọc</h3>
        </div>
        <div class="card-body">
          <form method="get" action="/admin/reviews/index" class="row g-3">
            <div class="col-md-4">
              <label class="form-label">Trạng thái</label>
              <select name="status" class="form-select">
                <option value="Inactive" th:selected="${status == 'Inactive'}">
                  Chờ duyệt
                </option>
                <option value="Active" th:selected="${status == 'Active'}">
                  Đã duyệt
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label">Số sao</label>
              <select name="rating" class="form-select">
                <option value="">Tất cả</option>
                <option value="5">5 sao</option>
                <option value="4">4 sao</option>
                <option value="3">3 sao</option>
                <option value="2">2 sao</option>
                <option value="1">1 sao</option>
              </select>
            </div>
            <div class="col-md-4 d-flex align-items-end">
              <button type="submit" class="btn btn-primary">
                <i class="fas fa-search mr-1"></i>Lọc
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Reviews Table -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Danh sách đánh giá</h3>
        </div>
        <div class="card-body table-responsive p-0">
          <table class="table table-hover text-nowrap">
            <thead class="table-dark">
              <tr>
                <th>ID</th>
                <th>Khách hàng</th>
                <th>Đơn hàng</th>
                <th>Đánh giá</th>
                <th>Nội dung</th>
                <th>Ngày tạo</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr
                th:each="comment : ${commentPage}"
                th:if="${not #lists.isEmpty(commentPage)}"
              >
                <td th:text="${comment.id}"></td>
                <td>
                  <div>
                    <strong th:text="${comment.user.name}"></strong><br />
                    <small
                      class="text-muted"
                      th:text="${comment.user.email}"
                    ></small>
                  </div>
                </td>
                <td>
                  <a
                    th:href="@{/admin/orders/detail/{id}(id=${comment.order.id})}"
                    class="btn btn-sm btn-outline-info"
                  >
                    #<span th:text="${comment.order.id}"></span>
                  </a>
                </td>
                <td>
                  <div class="stars-display">
                    <span
                      th:each="star : ${#numbers.sequence(1, 5)}"
                      th:class="${star <= comment.rating} ? 'star-filled' : 'star-empty'"
                    >
                      &#9733;
                    </span>
                  </div>
                  <small th:text="${comment.rating + '/5'}"></small>
                </td>
                <td>
                  <div class="comment-preview" th:title="${comment.content}">
                    <span
                      th:text="${#strings.abbreviate(comment.content, 50)}"
                    ></span>
                  </div>
                </td>
                <td>
                  <span
                    th:text="${#temporals.format(comment.createdAt, 'dd/MM/yyyy')}"
                  ></span
                  ><br />
                  <small
                    class="text-muted"
                    th:text="${#temporals.format(comment.createdAt, 'HH:mm')}"
                  ></small>
                </td>
                <td>
                  <span
                    th:if="${comment.status == 'Active'}"
                    class="badge bg-success"
                  >
                    <i class="fas fa-check mr-1"></i>Đã duyệt
                  </span>
                  <span
                    th:if="${comment.status == 'Inactive'}"
                    class="badge bg-warning"
                  >
                    <i class="fas fa-clock mr-1"></i>Chờ duyệt
                  </span>
                </td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <!-- View Detail -->
                    <a
                      th:href="@{/admin/reviews/detail/{id}(id=${comment.id})}"
                      class="btn btn-info"
                      title="Xem chi tiết"
                    >
                      <i class="fas fa-eye"></i>
                    </a>

                    <!-- Approve Button -->
                    <a
                      th:if="${comment.status == 'Inactive'}"
                      onclick="return confirmApprove(this);"
                      th:href="@{/admin/reviews/approve/{id}(id=${comment.id}, status=${status}, page=${currentPage})}"
                      class="btn btn-success"
                      title="Duyệt"
                    >
                      <i class="fas fa-check"></i>
                    </a>

                    <!-- Reject Button -->
                    <a
                      th:if="${comment.status == 'Inactive'}"
                      onclick="return confirmReject(this);"
                      th:href="@{/admin/reviews/reject/{id}(id=${comment.id}, status=${status}, page=${currentPage})}"
                      class="btn btn-danger"
                      title="Từ chối"
                    >
                      <i class="fas fa-times"></i>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Empty State -->
              <tr th:if="${#lists.isEmpty(commentPage)}">
                <td colspan="8" class="text-center py-4">
                  <div class="text-muted">
                    <i class="fas fa-inbox fa-3x mb-3"></i>
                    <p>Không có đánh giá nào</p>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="card-footer" th:if="${totalPages > 1}">
          <nav aria-label="Reviews pagination">
            <ul class="pagination justify-content-center mb-0">
              <li
                class="page-item"
                th:classappend="${currentPage == 0} ? 'disabled'"
              >
                <a
                  class="page-link"
                  th:href="@{/admin/reviews/index(page=${currentPage - 1}, status=${status})}"
                >
                  Trước
                </a>
              </li>

              <li
                class="page-item"
                th:each="page : ${#numbers.sequence(0, totalPages - 1)}"
                th:classappend="${page == currentPage} ? 'active'"
              >
                <a
                  class="page-link"
                  th:href="@{/admin/reviews/index(page=${page}, status=${status})}"
                  th:text="${page + 1}"
                ></a>
              </li>

              <li
                class="page-item"
                th:classappend="${currentPage >= totalPages - 1} ? 'disabled'"
              >
                <a
                  class="page-link"
                  th:href="@{/admin/reviews/index(page=${currentPage + 1}, status=${status})}"
                >
                  Sau
                </a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </section>
</div>

<script>
  function confirmApprove(element) {
    return confirm(
      "Bạn có chắc muốn duyệt đánh giá này? Đánh giá sẽ được hiển thị công khai."
    );
  }

  function confirmReject(element) {
    return confirm(
      "Bạn có chắc muốn từ chối đánh giá này? Đánh giá sẽ bị xóa vĩnh viễn."
    );
  }
</script>
```

---

## 🔄 VII. BUSINESS LOGIC WORKFLOW

### 1. Complete Review Flow

```
KHÁCH HÀNG:
1. Đặt hàng → Order tạo với status "Chờ xác nhận"
2. Shipper giao hàng → Order chuyển thành "Đã giao"
3. Form đánh giá xuất hiện ở /history
4. User chọn 1-5 sao + viết comment
5. Submit → Comment tạo với status "Inactive"

ADMIN:
6. Vào /admin/reviews/index xem danh sách chờ duyệt
7. Click "Duyệt" → Comment status = "Active"
8. Hoặc "Từ chối" → Comment bị xóa

HIỂN THỊ:
9. Comment "Active" hiển thị ở /history
10. Top 3 comment hiển thị ở homepage
```

### 2. Validation Rules

```java
// Business Rules trong CommentService

1. USER AUTHENTICATION:
   - User phải đăng nhập
   - Chỉ đánh giá đơn hàng của chính mình

2. ORDER STATUS:
   - Chỉ đơn hàng "Đã giao" mới có thể đánh giá
   - Mỗi đơn hàng chỉ đánh giá 1 lần

3. RATING VALIDATION:
   - Rating từ 1-5 (integer)
   - Không được null hoặc rỗng

4. CONTENT VALIDATION:
   - Content không được rỗng hoặc chỉ có khoảng trắng
   - Tối đa 255 ký tự

5. MODERATION:
   - Mặc định status = "Inactive"
   - Admin duyệt → "Active"
   - Chỉ comment "Active" hiển thị công khai
```

### 3. Security Considerations

```java
// Security Implementation

1. AUTHORIZATION:
   @PreAuthorize("hasRole('USER')")
   public Comment saveComment(...) {
       // Chỉ user đã login
   }

2. OWNERSHIP CHECK:
   if (!order.getUser().getId().equals(currentUser.getId())) {
       throw new AccessDeniedException("Không có quyền");
   }

3. ADMIN ROLE:
   @PreAuthorize("hasRole('ADMIN')")
   public String approveComment(...) {
       // Chỉ admin mới duyệt được
   }

4. CSRF PROTECTION:
   <form th:action="@{/history/comment}" method="post">
       <!-- Thymeleaf tự động thêm CSRF token -->
   </form>

5. XSS PREVENTION:
   th:text="${comment.content}" // Thymeleaf escape HTML
```

---

## 📊 VIII. DATABASE QUERIES & PERFORMANCE

### 1. Key Queries với Performance Optimization

```sql
-- 1. Lấy comments của 1 order (có index trên order_id)
SELECT c.* FROM comments c
WHERE c.order_id = ? AND c.status = 'Active'
ORDER BY c.created_at DESC;

-- 2. Top 3 comments cho homepage (có index trên status, rating, created_at)
SELECT c.* FROM comments c
WHERE c.status = 'Active'
ORDER BY c.rating DESC, c.created_at DESC
LIMIT 3;

-- 3. Comments chờ duyệt cho admin (có pagination)
SELECT c.* FROM comments c
WHERE c.status = 'Inactive'
ORDER BY c.created_at ASC
LIMIT 10 OFFSET 0;

-- 4. Kiểm tra user đã review order chưa (có composite index)
SELECT COUNT(*) FROM comments c
WHERE c.user_id = ? AND c.order_id = ?;

-- 5. Thống kê số lượng reviews theo status
SELECT c.status, COUNT(*) as count
FROM comments c
GROUP BY c.status;
```

### 2. Index Strategy

```sql
-- Primary Indexes
CREATE INDEX idx_comments_order_id ON comments(order_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_status ON comments(status);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- Composite Indexes for complex queries
CREATE INDEX idx_comments_status_rating_created
ON comments(status, rating DESC, created_at DESC);

CREATE INDEX idx_comments_user_order
ON comments(user_id, order_id);

-- Full-text search index (nếu cần search comment content)
CREATE FULLTEXT INDEX idx_comments_content
ON comments(content);
```

### 3. Caching Strategy

```java
// Redis Caching cho performance

@Service
public class CommentServiceImpl {

    @Cacheable(value = "topComments", key = "'top3'")
    public List<Comment> getTop3Comments() {
        return commentDAO.findTop3ByStatusOrderByCreatedAtDescRatingDesc("Active");
    }

    @CacheEvict(value = "topComments", key = "'top3'")
    public void updateComment(Long id, Comment comment) {
        // Clear cache khi có comment mới được duyệt
        Comment existingComment = commentDAO.findById(id).orElse(null);
        if (existingComment != null) {
            existingComment.setStatus(comment.getStatus());
            commentDAO.save(existingComment);
        }
    }
}
```

---

## 🔗 IX. INTEGRATION POINTS

### 1. Homepage Integration

```java
// HomeController.java
@Controller
public class HomeController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/")
    public String index(Model model) {
        // Load top 3 reviews
        List<Comment> topReviews = commentService.getTop3Comments();
        model.addAttribute("topReviews", topReviews);

        // Other homepage data...
        return "home";
    }
}
```

```html
<!-- home.html - Homepage Reviews Section -->
<section class="testimonials py-5 bg-light">
  <div class="container">
    <h2 class="text-center mb-5">Khách hàng nói gì về chúng tôi</h2>

    <div class="row" th:if="${not #lists.isEmpty(topReviews)}">
      <div class="col-md-4 mb-4" th:each="review : ${topReviews}">
        <div class="testimonial-card card h-100 border-0 shadow-sm">
          <div class="card-body text-center">
            <!-- Stars -->
            <div class="stars mb-3">
              <span
                th:each="star : ${#numbers.sequence(1, 5)}"
                th:class="${star <= review.rating} ? 'star-filled' : 'star-empty'"
              >
                &#9733;
              </span>
            </div>

            <!-- Review Content -->
            <p class="card-text" th:text="${review.content}"></p>

            <!-- Customer Info -->
            <div class="customer-info mt-auto">
              <h6 class="mb-1" th:text="${review.user.name}"></h6>
              <small class="text-muted">Khách hàng verified</small>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Call to Action -->
    <div class="text-center mt-4">
      <a href="/product" class="btn btn-primary btn-lg"> Mua sắm ngay </a>
    </div>
  </div>
</section>
```

### 2. Order Detail Integration

```html
<!-- detail.html - Product Detail Page -->
<div class="product-reviews mt-5" th:if="${product.hasReviews()}">
  <h4>Đánh giá sản phẩm</h4>

  <div class="review-summary mb-4">
    <div class="row">
      <div class="col-md-6">
        <div class="average-rating">
          <span class="rating-number" th:text="${product.averageRating}"
            >4.5</span
          >
          <div class="stars">
            <span
              th:each="star : ${#numbers.sequence(1, 5)}"
              th:class="${star <= product.averageRating} ? 'star-filled' : 'star-empty'"
            >
              &#9733;
            </span>
          </div>
          <p><span th:text="${product.totalReviews}">25</span> đánh giá</p>
        </div>
      </div>
    </div>
  </div>

  <!-- Recent Reviews -->
  <div class="recent-reviews">
    <div class="review-item mb-3" th:each="review : ${product.recentReviews}">
      <!-- Review display similar to history.html -->
    </div>
  </div>
</div>
```

---

## 🚀 X. DEPLOYMENT & MONITORING

### 1. Production Configuration

```properties
# application-prod.properties

# Database optimizations
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Redis caching
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000ms

# File upload limits
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```

### 2. Monitoring & Analytics

```java
// Metrics for review system
@Component
public class ReviewMetrics {

    private final MeterRegistry meterRegistry;

    public ReviewMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementReviewSubmitted() {
        Counter.builder("reviews.submitted")
               .description("Number of reviews submitted")
               .register(meterRegistry)
               .increment();
    }

    public void incrementReviewApproved() {
        Counter.builder("reviews.approved")
               .description("Number of reviews approved")
               .register(meterRegistry)
               .increment();
    }
}
```

---

## 📈 XI. FUTURE ENHANCEMENTS

### 1. Advanced Features

```java
// Planned enhancements

1. REVIEW PHOTOS:
   - Upload ảnh kèm review
   - Image validation & resize
   - CDN storage

2. REVIEW RESPONSES:
   - Shop owner reply to reviews
   - Customer can reply back
   - Threaded conversations

3. REVIEW ANALYTICS:
   - Rating trends over time
   - Customer satisfaction metrics
   - Product quality insights

4. ADVANCED MODERATION:
   - AI content filtering
   - Spam detection
   - Sentiment analysis

5. REVIEW INCENTIVES:
   - Points for writing reviews
   - Discount codes for reviewers
   - Review badges/levels
```

### 2. API Endpoints

```java
// REST API for mobile app

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    @PostMapping("/submit")
    public ResponseEntity<CommentDTO> submitReview(@RequestBody ReviewRequest request) {
        // Submit review via API
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<CommentDTO>> getOrderReviews(@PathVariable Long orderId) {
        // Get reviews for an order
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ReviewSummaryDTO> getProductReviews(@PathVariable Long productId) {
        // Get product review summary
    }
}
```

---

## 🎯 KẾT LUẬN

Hệ thống đánh giá đơn hàng là một **component quan trọng** giúp:

### ✅ **Lợi ích cho Business:**

- **Tăng độ tin cậy**: Reviews thật từ khách hàng
- **Cải thiện chất lượng**: Feedback để cải thiện dịch vụ
- **Marketing**: Top reviews hiển thị ở homepage
- **Customer engagement**: Khuyến khích tương tác

### ✅ **Lợi ích cho Customer:**

- **Chia sẻ trải nghiệm**: Voice được lắng nghe
- **Giúp người khác**: Review giúp khách hàng khác quyết định
- **Feedback loop**: Cảm thấy được quan tâm

### ✅ **Lợi ích cho Admin:**

- **Quality control**: Kiểm soát nội dung hiển thị
- **Customer insights**: Hiểu được customer satisfaction
- **Risk management**: Ngăn chặn spam/fake reviews

### 🔄 **Integration hoàn hảo:**

Hệ thống review tích hợp seamless với:

- **Order lifecycle**: Chỉ review đơn "Đã giao"
- **User authentication**: Security đảm bảo
- **Admin workflow**: Tools quản lý hiệu quả
- **Homepage display**: Marketing tự động

**Đây là một hệ thống review đầy đủ, chuyên nghiệp với UX tốt và admin tools mạnh mẽ!** 🌟
