// Giảm giá
function toggleDiscountFields() {
  const checkbox = document.getElementById("applyDiscount");
  const discountSection = document.getElementById("discountFields");

  if (checkbox.checked) {
    discountSection.classList.remove("d-none");
    discountSection.style.display = "block";
  } else {
    discountSection.style.display = "none";

    // Clear values khi bỏ tick
    document.getElementById("discountPercent").value = "";
    document.getElementById("discountStart").value = "";
    document.getElementById("discountEnd").value = "";
  }
}

// === CÁC FUNCTION KHÁC ===

// Function validateDiscountForm đã được tích hợp vào validateProductForm

window.addEventListener("DOMContentLoaded", () => {
  const checkbox = document.getElementById("applyDiscount");
  toggleDiscountFields();

  // Kiểm tra xem có lỗi validation nào cho các trường giảm giá không
  const discountPercentError = document.querySelector(
    "i.error[th\\:errors='*{discountPercent}']"
  );
  const discountStartError = document.querySelector(
    "i.error[th\\:errors='*{discountStart}']"
  );
  const discountEndError = document.querySelector(
    "i.error[th\\:errors='*{discountEnd}']"
  );

  const hasDiscountError =
    (discountPercentError && discountPercentError.textContent.trim()) ||
    (discountStartError && discountStartError.textContent.trim()) ||
    (discountEndError && discountEndError.textContent.trim());

  // Nếu có lỗi validation hoặc có giá trị trong các trường giảm giá, hiển thị section
  if (hasDiscountError || checkbox.checked) {
    checkbox.checked = true;
    toggleDiscountFields();
  }
});

/* ==========================================
   ADMIN CRUD - JAVASCRIPT FUNCTIONS
   ========================================== */

// 1. HỆ THỐNG THÔNG BÁO
function showCustomAlert(message, type = "success") {
  document.querySelectorAll(".custom-alert").forEach((alert) => alert.remove());

  const alertDiv = document.createElement("div");
  alertDiv.className = `custom-alert alert-${type}`;

  const icon =
    type === "success"
      ? "bi-check-circle-fill"
      : type === "error"
      ? "bi-x-circle-fill"
      : "bi-exclamation-triangle-fill";

  alertDiv.innerHTML = `
    <i class="alert-icon bi ${icon}"></i>
    <span class="alert-message">${message}</span>
    <button class="alert-close" onclick="this.parentElement.remove()">×</button>
  `;

  document.body.appendChild(alertDiv);
  setTimeout(() => {
    if (alertDiv.parentElement) {
      alertDiv.style.animation = "slideOutRight 0.3s ease-in";
      setTimeout(() => alertDiv.remove(), 300);
    }
  }, 6000);
}

function showAlert(message, type = "info") {
  showCustomAlert(message, type);
}

// 2. HIỂN THỊ LỖI VALIDATION
function showFieldError(elementId, message) {
  const element = document.getElementById(elementId);
  if (!element) return;
  
  clearFieldError(elementId);
  element.classList.add("is-invalid");
  
  const feedback = document.createElement("div");
  feedback.className = "invalid-feedback";
  feedback.textContent = message;
  element.parentNode.appendChild(feedback);
}

// Xóa lỗi của một field cụ thể
function clearFieldError(elementId) {
  const element = document.getElementById(elementId);
  if (!element) return;
  
  // Xóa class báo lỗi
  element.classList.remove("is-invalid");
  
  // Xóa thông báo lỗi
  const feedback = element.parentNode.querySelector(".invalid-feedback");
  if (feedback) {
    feedback.remove();
  }
}

// Xóa tất cả lỗi validation
function clearAllFieldErrors() {
  // Xóa tất cả class báo lỗi
  document.querySelectorAll(".is-invalid").forEach((el) => {
    el.classList.remove("is-invalid");
  });
  
  // Xóa tất cả thông báo lỗi
  document.querySelectorAll(".invalid-feedback").forEach((el) => {
    el.remove();
  });
}

// Function validateImageUpload đã được tích hợp vào validateProductForm

// 3. VALIDATION FORM SẢN PHẨM
function validateProductForm() {
  let isValid = true;
  const missingFields = [];  // Thiếu thông tin
  const invalidFields = [];  // Thông tin không hợp lệ

  // Clear all previous errors
  clearAllFieldErrors();

  // Lấy tất cả giá trị
  const name = document.getElementById("name").value.trim();
  const price = document.getElementById("price").value.trim();
  const quantity = document.getElementById("quantity").value.trim();
  const productId = document.getElementById("id").value;
  const image1 = document.getElementById("image1").files[0];
  const discountCheckbox = document.getElementById("applyDiscount");
  const discountPercent = document.getElementById("discountPercent").value.trim();
  const discountStart = document.getElementById("discountStart").value.trim();
  const discountEnd = document.getElementById("discountEnd").value.trim();

  // Kiểm tra xem có trường nào được điền không (bao gồm cả discount khi được tick)
  const hasBasicInput = name || price || quantity;
  const hasDiscountInput = discountPercent || discountStart || discountEnd;
  const hasImageInput = productId == "0" && image1;
  
  // Nếu chưa điền gì cả (kể cả khi tick discount nhưng không điền)
  if (!hasBasicInput && !hasDiscountInput && (productId != "0" || !hasImageInput)) {
    showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    // Hiển thị lỗi dưới field cho các trường bắt buộc
    showFieldError("name", "Vui lòng nhập tên sản phẩm");
    showFieldError("price", "Vui lòng nhập giá sản phẩm");
    showFieldError("quantity", "Vui lòng nhập số lượng");
    if (productId == "0") {
      showFieldError("image1", "Vui lòng chọn ảnh chính");
    }
    // Nếu tick discount mà không điền gì thì cũng hiển thị lỗi discount
    if (discountCheckbox.checked) {
      showFieldError("discountPercent", "Vui lòng nhập phần trăm giảm giá");
      showFieldError("discountStart", "Vui lòng chọn ngày bắt đầu");
      showFieldError("discountEnd", "Vui lòng chọn ngày kết thúc");
    }
    return false;
  }

  // Validate từng trường và phân loại lỗi
  if (!name) {
    showFieldError("name", "Vui lòng nhập tên sản phẩm");
    missingFields.push("tên sản phẩm");
    isValid = false;
  }

  if (!price) {
    showFieldError("price", "Vui lòng nhập giá sản phẩm");
    missingFields.push("giá sản phẩm");
    isValid = false;
  } else if (isNaN(price) || parseFloat(price) <= 0) {
    showFieldError("price", "Giá sản phẩm phải là số dương");
    invalidFields.push("giá sản phẩm");
    isValid = false;
  }

  if (!quantity) {
    showFieldError("quantity", "Vui lòng nhập số lượng");
    missingFields.push("số lượng");
    isValid = false;
  } else if (isNaN(quantity) || parseInt(quantity) < 0) {
    showFieldError("quantity", "Số lượng phải là số không âm");
    invalidFields.push("số lượng");
    isValid = false;
  }

  // Validate image upload
  if (productId == "0" && !image1) {
    showFieldError("image1", "Vui lòng chọn ảnh chính");
    missingFields.push("ảnh chính");
    isValid = false;
  }

  // Validate discount form nếu checkbox được tick
  if (discountCheckbox.checked) {
    if (!discountPercent) {
      showFieldError("discountPercent", "Vui lòng nhập phần trăm giảm giá");
      missingFields.push("phần trăm giảm giá");
      isValid = false;
    } else {
      const percent = parseInt(discountPercent);
      if (isNaN(percent) || percent < 1 || percent > 100) {
        showFieldError("discountPercent", "Phần trăm giảm giá phải từ 1% đến 100%");
        invalidFields.push("phần trăm giảm giá");
        isValid = false;
      }
    }

    if (!discountStart) {
      showFieldError("discountStart", "Vui lòng chọn ngày bắt đầu");
      missingFields.push("ngày bắt đầu");
      isValid = false;
    } else {
      // Chỉ validate ngày quá khứ khi tạo mới
      // Khi cập nhật: để backend xử lý logic so sánh ngày
      if (productId == "0") {
        const startDate = new Date(discountStart);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        if (startDate < today) {
          showFieldError("discountStart", "Ngày bắt đầu không được là ngày trong quá khứ");
          invalidFields.push("ngày bắt đầu");
          isValid = false;
        }
      }
      // Khi cập nhật (productId != "0"): không validate ngày ở frontend
      // Để backend xử lý logic so sánh và validate
    }

    if (!discountEnd) {
      showFieldError("discountEnd", "Vui lòng chọn ngày kết thúc");
      missingFields.push("ngày kết thúc");
      isValid = false;
    }

    // Validate logic ngày nếu có đầy đủ thông tin
    if (discountStart && discountEnd) {
      const startDate = new Date(discountStart);
      const endDate = new Date(discountEnd);

      if (endDate <= startDate) {
        showFieldError("discountEnd", "Ngày kết thúc phải sau ngày bắt đầu");
        invalidFields.push("ngày kết thúc");
        isValid = false;
      }
    }
  }

  // Show summary alert ngắn gọn và đẹp
  if (missingFields.length > 0 || invalidFields.length > 0) {
    const totalErrors = missingFields.length + invalidFields.length;
    const totalFields = missingFields.length + invalidFields.length;
    
    // Kiểm tra xem có phải tất cả field đều trống không
    const allFieldsEmpty = !name && !price && !quantity && 
                          (productId == "0" ? !image1 : true) &&
                          (!discountCheckbox.checked || (!discountPercent && !discountStart && !discountEnd));
    
    if (allFieldsEmpty) {
      showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    } else {
      showCustomAlert("Vui lòng kiểm tra lại thông tin", "error");
    }
  }

  return isValid;
}

// Xác nhận xóa sản phẩm
function confirmDelete(element) {
  const url = element.getAttribute("data-url");
  if (confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) {
    window.location.href = url;
  }
}

// Xóa bộ lọc sản phẩm
function clearProductFilter() {
  const form = document.getElementById("productFilterForm");
  form.querySelector('select[name="productCategoryId"]').value = "";
  form.querySelector('input[name="keyword"]').value = "";
  form.submit();
}

// Kiểm tra và hiển thị lỗi từ backend
function checkBackendValidationErrors() {
  const errorElements = document.querySelectorAll(".error");
  const errors = [];
  
  // Thu thập tất cả lỗi từ Thymeleaf
  errorElements.forEach(function(element) {
    const errorText = element.textContent.trim();
    if (errorText && errorText !== "") {
      errors.push(errorText);
    }
  });
  
  // Hiển thị alert phù hợp
  if (errors.length > 0) {
    if (errors.length === 1) {
      showCustomAlert(errors[0], "error");
    } else if (errors.length <= 3) {
      showCustomAlert("Có lỗi: " + errors.join(", "), "error");
    } else {
      showCustomAlert("Vui lòng kiểm tra lại thông tin (" + errors.length + " lỗi)", "error");
    }
  }
}

// 5. KHỞI TẠO TRANG

// Chuyển tab theo URL
const params = new URLSearchParams(window.location.search);
const activeTab = params.get("tab");
if (activeTab) {
  const triggerEl = document.querySelector(`#${activeTab}-tab`);
  if (triggerEl) {
    new bootstrap.Tab(triggerEl).show();
  }
}

// Khởi tạo các sự kiện khi DOM đã sẵn sàng
document.addEventListener("DOMContentLoaded", function() {
  // Validation khi submit form
  const productForm = document.getElementById("productForm");
  if (productForm) {
    productForm.addEventListener("submit", function(event) {
      // Xóa lỗi cũ trước khi validate
      clearAllFieldErrors();
      
      // Chạy validation
      if (!validateProductForm()) {
        event.preventDefault();
        return false;
      }
      
      return true;
    });
  }
  
  // Kiểm tra lỗi từ backend khi trang load
  checkBackendValidationErrors();
  
  // Tự động xóa lỗi khi user bắt đầu nhập
  const formInputs = document.querySelectorAll("#productForm input, #productForm select, #productForm textarea");
  formInputs.forEach(function(input) {
    input.addEventListener("input", function() {
      if (this.classList.contains("is-invalid")) {
        clearFieldError(this.id);
      }
    });
    
    input.addEventListener("change", function() {
      if (this.classList.contains("is-invalid")) {
        clearFieldError(this.id);
      }
    });
  });
});
