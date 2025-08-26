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

  // Xóa lỗi role
  const roleError = document.getElementById("role-error");
  if (roleError) roleError.remove();

  const roleContainer = document.querySelector('input[name="role"]');
  if (roleContainer) {
    const container = roleContainer.closest(".d-flex");
    if (container) {
      container.style.border = "";
      container.style.borderRadius = "";
      container.style.padding = "";
    }
  }
}

// Function validateImageUpload đã được tích hợp vào validateProductForm

// 3. VALIDATION FORM SẢN PHẨM
function validateProductForm() {
  let isValid = true;
  const missingFields = []; // Thiếu thông tin
  const invalidFields = []; // Thông tin không hợp lệ

  // Clear all previous errors
  clearAllFieldErrors();

  // Lấy tất cả giá trị
  const name = document.getElementById("name").value.trim();
  const price = document.getElementById("price").value.trim();
  const quantity = document.getElementById("quantity").value.trim();
  const productId = document.getElementById("id").value;
  const image1 = document.getElementById("image1").files[0];
  const discountCheckbox = document.getElementById("applyDiscount");
  const discountPercent = document
    .getElementById("discountPercent")
    .value.trim();
  const discountStart = document.getElementById("discountStart").value.trim();
  const discountEnd = document.getElementById("discountEnd").value.trim();

  // Kiểm tra xem có trường nào được điền không (bao gồm cả discount khi được tick)
  const hasBasicInput = name || price || quantity;
  const hasDiscountInput = discountPercent || discountStart || discountEnd;
  const hasImageInput = productId == "0" && image1;

  // Nếu chưa điền gì cả (kể cả khi tick discount nhưng không điền)
  if (
    !hasBasicInput &&
    !hasDiscountInput &&
    (productId != "0" || !hasImageInput)
  ) {
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
        showFieldError(
          "discountPercent",
          "Phần trăm giảm giá phải từ 1% đến 100%"
        );
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
          showFieldError(
            "discountStart",
            "Ngày bắt đầu không được là ngày trong quá khứ"
          );
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
    const allFieldsEmpty =
      !name &&
      !price &&
      !quantity &&
      (productId == "0" ? !image1 : true) &&
      (!discountCheckbox.checked ||
        (!discountPercent && !discountStart && !discountEnd));

    if (allFieldsEmpty) {
      showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    } else {
      showCustomAlert("Vui lòng kiểm tra lại thông tin", "error");
    }
  }

  return isValid;
}

// Xác nhận xóa sản phẩm
// function confirmDelete(element) {
//   const url = element.getAttribute("data-url");
//   if (confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) {
//     window.location.href = url;
//   }
// }

// Xóa bộ lọc sản phẩm
function clearProductFilter() {
  const form = document.getElementById("productFilterForm");
  form.querySelector('select[name="productCategoryId"]').value = "";
  form.querySelector('input[name="keyword"]').value = "";
  form.submit();
}

// Kiểm tra và hiển thị lỗi từ backend
function checkBackendValidationErrors() {
  const errors = [];
  const currentUrl = window.location.pathname;

  // Thu thập lỗi từ các element có class "error" (ProductCRUD style)
  const errorElements = document.querySelectorAll(".error, i.error, div.error");
  errorElements.forEach(function (element) {
    const errorText = element.textContent.trim();
    if (errorText && errorText !== "") {
      errors.push(errorText);
    }
  });

  // Thu thập lỗi từ các element có attribute th:if="${error}" (các CRUD khác)
  const errorDivs = document.querySelectorAll(
    "div[th\\:if*='error'], i[th\\:if*='error']"
  );
  errorDivs.forEach(function (element) {
    const errorText = element.textContent.trim();
    if (errorText && errorText !== "" && !errors.includes(errorText)) {
      errors.push(errorText);
    }
  });

  // Hiển thị alert phù hợp
  if (errors.length > 0) {
    // Xử lý thông báo lỗi đặc biệt
    for (let i = 0; i < errors.length; i++) {
      let error = errors[i];

      // Làm đẹp thông báo lỗi khóa ngoại và các lỗi thường gặp
      if (error.includes("đã có đơn hàng") || error.includes("giỏ hàng")) {
        error = "Không thể xóa vì đã có dữ liệu liên quan!";
      } else if (
        error.includes("foreign key") ||
        error.includes("constraint")
      ) {
        error = "Không thể xóa vì đã có dữ liệu liên quan!";
      } else if (error.includes("duplicate") || error.includes("trùng lặp")) {
        error = "Dữ liệu đã tồn tại!";
      } else if (
        error.includes("not found") ||
        error.includes("không tìm thấy")
      ) {
        error = "Không tìm thấy dữ liệu!";
      }

      errors[i] = error;
    }

    // Đặc biệt cho ProductCategory: thay đổi thông báo
    if (currentUrl.includes("/ProductCategory/") && errors.length === 1) {
      const error = errors[0];
      if (error.includes("tên danh mục") || error.includes("name")) {
        showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
        return;
      }
    }

    // Logic thông thường cho các CRUD khác
    if (errors.length === 1) {
      showCustomAlert(errors[0], "error");
    } else if (errors.length <= 3) {
      showCustomAlert("Có lỗi: " + errors.join(", "), "error");
    } else {
      showCustomAlert(
        "Vui lòng kiểm tra lại thông tin (" + errors.length + " lỗi)",
        "error"
      );
    }
  }
}

// 4. HIỂN THỊ THÔNG BÁO TỪ FLASH ATTRIBUTES (SUCCESS/ERROR)
function checkFlashMessages() {
  // Kiểm tra success message
  const successElements = document.querySelectorAll(".alert-success");
  successElements.forEach(function (element) {
    const messageSpan = element.querySelector("span");
    if (messageSpan) {
      let message = messageSpan.textContent.trim();
      if (message) {
        showCustomAlert(message, "success");
        element.style.display = "none"; // Ẩn alert Bootstrap gốc
      }
    }
  });

  // Kiểm tra error message
  const errorElements = document.querySelectorAll(".alert-danger");
  errorElements.forEach(function (element) {
    const messageSpan = element.querySelector("span");
    if (messageSpan) {
      let message = messageSpan.textContent.trim();
      if (message) {
        // Làm đẹp thông báo lỗi flash message
        if (
          message.includes("đã có đơn hàng") ||
          message.includes("giỏ hàng")
        ) {
          message = "Không thể xóa vì đã có dữ liệu liên quan!";
        } else if (
          message.includes("foreign key") ||
          message.includes("constraint")
        ) {
          message = "Không thể xóa vì đã có dữ liệu liên quan!";
        } else if (
          message.includes("duplicate") ||
          message.includes("trùng lặp")
        ) {
          message = "Dữ liệu đã tồn tại!";
        } else if (
          message.includes("not found") ||
          message.includes("không tìm thấy")
        ) {
          message = "Không tìm thấy dữ liệu!";
        }

        showCustomAlert(message, "error");
        element.style.display = "none"; // Ẩn alert Bootstrap gốc
      }
    }
  });

  // Kiểm tra warning message
  const warningElements = document.querySelectorAll(".alert-warning");
  warningElements.forEach(function (element) {
    const messageSpan = element.querySelector("span");
    if (messageSpan) {
      let message = messageSpan.textContent.trim();
      if (message) {
        showCustomAlert(message, "warning");
        element.style.display = "none"; // Ẩn alert Bootstrap gốc
      }
    }
  });
}

// 5. HÀM TIỆN ÍCH ĐỂ LÀM ĐẸP THÔNG BÁO LỖI
function beautifyErrorMessage(message) {
  if (!message) return message;

  // Làm đẹp các thông báo lỗi thường gặp
  if (message.includes("đã có đơn hàng") || message.includes("giỏ hàng")) {
    return "Không thể xóa vì đã có dữ liệu liên quan!";
  } else if (
    message.includes("foreign key") ||
    message.includes("constraint")
  ) {
    return "Không thể xóa vì đã có dữ liệu liên quan!";
  } else if (message.includes("duplicate") || message.includes("trùng lặp")) {
    return "Dữ liệu đã tồn tại!";
  } else if (
    message.includes("not found") ||
    message.includes("không tìm thấy")
  ) {
    return "Không tìm thấy dữ liệu!";
  } else if (message.includes("DataIntegrityViolationException")) {
    return "Không thể xóa vì đã có dữ liệu liên quan!";
  } else if (message.includes("ConstraintViolationException")) {
    return "Dữ liệu không hợp lệ!";
  }

  return message;
}

// 5. VALIDATION ĐỒNG BỘ CHO TẤT CẢ CRUD (trừ ProductCRUD)

// 5.1. VALIDATION CHO CATEGORY CRUD
function validateCategoryForm() {
  let isValid = true;
  const missingFields = [];

  // Clear all previous errors
  clearAllFieldErrors();

  // Lấy giá trị
  const name = document.getElementById("name").value.trim();
  // Mô tả không bắt buộc

  // Kiểm tra xem có trường nào được điền không
  if (!name) {
    showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    showFieldError("name", "Vui lòng nhập tên loại hoa");
    return false;
  }

  return isValid;
}

// 5.2. VALIDATION CHO PRODUCT CATEGORY CRUD
function validateProductCategoryForm() {
  let isValid = true;

  // Clear all previous errors
  clearAllFieldErrors();

  // Xóa luôn cả backend errors để tránh hiển thị chồng chéo
  const backendErrors = document.querySelectorAll("i.error");
  backendErrors.forEach((error) => {
    error.textContent = "";
    error.style.display = "none";
  });

  // Lấy giá trị
  const name = document.getElementById("name").value.trim();

  // Kiểm tra xem có trường nào được điền không
  if (!name) {
    showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    showFieldError("name", "Vui lòng nhập tên danh mục");
    return false;
  }

  return isValid;
}

// 5.3. VALIDATION CHO SERVICE CRUD
function validateServiceForm() {
  let isValid = true;
  const missingFields = [];

  // Clear all previous errors
  clearAllFieldErrors();

  // Lấy giá trị
  const name = document.getElementById("name").value.trim();
  const serviceId = document.getElementById("id").value;
  const image1 = document.getElementById("image1").files[0];

  // Kiểm tra xem có trường nào được điền không
  const isNewService = !serviceId || serviceId == "0" || serviceId == "";
  const hasBasicInput = name;
  const hasImageInput = isNewService && image1;

  if (!hasBasicInput && isNewService && !hasImageInput) {
    showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    showFieldError("name", "Vui lòng nhập tên dịch vụ");
    showFieldError("image1", "Vui lòng chọn ảnh chính");
    return false;
  }

  // Validate từng trường
  if (!name) {
    showFieldError("name", "Vui lòng nhập tên dịch vụ");
    missingFields.push("tên dịch vụ");
    isValid = false;
  }

  // Validate image upload cho dịch vụ mới
  if (isNewService && !image1) {
    showFieldError("image1", "Vui lòng chọn ảnh chính");
    missingFields.push("ảnh chính");
    isValid = false;
  }

  // Show summary alert
  if (missingFields.length > 0) {
    if (missingFields.length === 1) {
      showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    } else {
      showCustomAlert("Vui lòng kiểm tra lại thông tin", "error");
    }
  }

  return isValid;
}

// 5.4. VALIDATION CHO PROMOTION CRUD
function validatePromotionForm() {
  let isValid = true;
  const missingFields = [];
  const invalidFields = [];

  // Clear all previous errors
  clearAllFieldErrors();

  // Lấy giá trị
  const title = document.getElementById("title").value.trim();
  const description = document.getElementById("description").value.trim();
  const discountType = document.getElementById("discountType").value.trim();
  const discountValue = document.getElementById("discountValue").value.trim();
  const startDate = document.getElementById("startDate").value.trim();
  const endDate = document.getElementById("endDate").value.trim();
  const useCount = document.getElementById("useCount").value.trim();

  // Kiểm tra xem có trường nào được điền không (bỏ description vì không bắt buộc)
  if (
    !title &&
    !discountType &&
    !discountValue &&
    !startDate &&
    !endDate &&
    !useCount
  ) {
    showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    showFieldError("title", "Vui lòng nhập tên khuyến mãi");
    showFieldError("discountType", "Vui lòng chọn loại giảm giá");
    showFieldError("discountValue", "Vui lòng nhập giá trị giảm");
    showFieldError("startDate", "Vui lòng chọn ngày bắt đầu");
    showFieldError("endDate", "Vui lòng chọn ngày kết thúc");
    showFieldError("useCount", "Vui lòng nhập số lượng sử dụng");
    return false;
  }

  // Validate từng trường
  if (!title) {
    showFieldError("title", "Vui lòng nhập tên khuyến mãi");
    missingFields.push("tên khuyến mãi");
    isValid = false;
  }

  // Mô tả không bắt buộc - bỏ validation

  if (!discountType) {
    showFieldError("discountType", "Vui lòng chọn loại giảm giá");
    missingFields.push("loại giảm giá");
    isValid = false;
  }

  if (!discountValue) {
    showFieldError("discountValue", "Vui lòng nhập giá trị giảm");
    missingFields.push("giá trị giảm");
    isValid = false;
  } else {
    const value = parseFloat(discountValue);
    if (isNaN(value) || value <= 0) {
      showFieldError("discountValue", "Giá trị giảm phải là số dương");
      invalidFields.push("giá trị giảm");
      isValid = false;
    } else if (discountType === "percent" && value > 100) {
      showFieldError(
        "discountValue",
        "Phần trăm giảm giá không được vượt quá 100%"
      );
      invalidFields.push("phần trăm giảm giá");
      isValid = false;
    }
  }

  if (!startDate) {
    showFieldError("startDate", "Vui lòng chọn ngày bắt đầu");
    missingFields.push("ngày bắt đầu");
    isValid = false;
  } else {
    const start = new Date(startDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (start < today) {
      showFieldError(
        "startDate",
        "Ngày bắt đầu không được là ngày trong quá khứ"
      );
      invalidFields.push("ngày bắt đầu");
      isValid = false;
    }
  }

  if (!endDate) {
    showFieldError("endDate", "Vui lòng chọn ngày kết thúc");
    missingFields.push("ngày kết thúc");
    isValid = false;
  }

  if (!useCount) {
    showFieldError("useCount", "Vui lòng nhập số lượng sử dụng");
    missingFields.push("số lượng sử dụng");
    isValid = false;
  } else {
    const count = parseInt(useCount);
    if (isNaN(count) || count < 0) {
      showFieldError("useCount", "Số lượng sử dụng phải là số không âm");
      invalidFields.push("số lượng sử dụng");
      isValid = false;
    }
  }

  // Validate logic ngày
  if (startDate && endDate) {
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (end <= start) {
      showFieldError("endDate", "Ngày kết thúc phải sau ngày bắt đầu");
      invalidFields.push("ngày kết thúc");
      isValid = false;
    }
  }

  // Show summary alert
  if (missingFields.length > 0 || invalidFields.length > 0) {
    const allFieldsEmpty =
      !title &&
      !discountType &&
      !discountValue &&
      !startDate &&
      !endDate &&
      !useCount;

    if (allFieldsEmpty) {
      showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    } else {
      showCustomAlert("Vui lòng kiểm tra lại thông tin", "error");
    }
  }

  return isValid;
}

// 5.5. VALIDATION CHO USER CRUD
function validateUserForm() {
  let isValid = true;
  const missingFields = [];
  const invalidFields = [];

  // Clear all previous errors
  clearAllFieldErrors();

  // Lấy giá trị
  const name = document.getElementById("name").value.trim();
  const email = document.getElementById("email").value.trim();
  const sdt = document.getElementById("sdt").value.trim();
  const userId = document.getElementById("id").value;
  const password = document.getElementById("password")
    ? document.getElementById("password").value.trim()
    : "";
  const role = document.querySelector('input[name="role"]:checked');

  // Kiểm tra xem có trường nào được điền không
  const isNewUser = !userId || userId == "0" || userId == "";
  const hasBasicInput = name || email || sdt || (isNewUser && password) || role;

  if (!hasBasicInput) {
    showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    showFieldError("name", "Vui lòng nhập tên");
    showFieldError("email", "Vui lòng nhập email");
    showFieldError("sdt", "Vui lòng nhập số điện thoại");
    if (isNewUser) {
      showFieldError("password", "Vui lòng nhập mật khẩu");
    }

    // Hiển thị lỗi role ngay từ đầu
    const roleContainer = document
      .querySelector('input[name="role"]')
      .closest(".d-flex");
    if (roleContainer) {
      roleContainer.style.border = "1px solid #dc3545";
      roleContainer.style.borderRadius = "4px";
      roleContainer.style.padding = "5px";
    }

    const roleErrorElement = document.createElement("i");
    roleErrorElement.className = "error";
    roleErrorElement.textContent = "Vui lòng chọn vai trò";
    roleErrorElement.style.color = "#dc3545";
    roleErrorElement.style.fontSize = "0.875em";
    roleErrorElement.style.display = "block";
    roleErrorElement.style.marginTop = "5px";
    roleErrorElement.id = "role-error";

    const oldError = document.getElementById("role-error");
    if (oldError) oldError.remove();

    if (roleContainer) {
      roleContainer.parentNode.appendChild(roleErrorElement);
    }

    return false;
  }

  // Validate từng trường
  if (!name) {
    showFieldError("name", "Vui lòng nhập tên");
    missingFields.push("tên");
    isValid = false;
  }

  if (!email) {
    showFieldError("email", "Vui lòng nhập email");
    missingFields.push("email");
    isValid = false;
  } else {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showFieldError("email", "Email không hợp lệ");
      invalidFields.push("email");
      isValid = false;
    }
  }

  if (!sdt) {
    showFieldError("sdt", "Vui lòng nhập số điện thoại");
    missingFields.push("số điện thoại");
    isValid = false;
  } else {
    const phoneRegex = /^[0-9]{10,11}$/;
    if (!phoneRegex.test(sdt)) {
      showFieldError("sdt", "Số điện thoại phải có 10-11 chữ số");
      invalidFields.push("số điện thoại");
      isValid = false;
    }
  }

  // Validate password cho user mới
  if (isNewUser && !password) {
    showFieldError("password", "Vui lòng nhập mật khẩu");
    missingFields.push("mật khẩu");
    isValid = false;
  } else if (isNewUser && password && password.length < 6) {
    showFieldError("password", "Mật khẩu phải có ít nhất 6 ký tự");
    invalidFields.push("mật khẩu");
    isValid = false;
  }

  if (!role) {
    // Hiển thị lỗi cho tất cả radio button role
    const roleContainer = document
      .querySelector('input[name="role"]')
      .closest(".d-flex");
    if (roleContainer) {
      roleContainer.style.border = "1px solid #dc3545";
      roleContainer.style.borderRadius = "4px";
      roleContainer.style.padding = "5px";
    }

    // Tạo error message cho role
    const roleErrorElement = document.createElement("i");
    roleErrorElement.className = "error";
    roleErrorElement.textContent = "Vui lòng chọn vai trò";
    roleErrorElement.style.color = "#dc3545";
    roleErrorElement.style.fontSize = "0.875em";
    roleErrorElement.style.display = "block";
    roleErrorElement.style.marginTop = "5px";
    roleErrorElement.id = "role-error";

    // Xóa error cũ nếu có
    const oldError = document.getElementById("role-error");
    if (oldError) oldError.remove();

    // Thêm error message
    if (roleContainer) {
      roleContainer.parentNode.appendChild(roleErrorElement);
    }

    missingFields.push("vai trò");
    isValid = false;
  }

  // Show summary alert
  if (missingFields.length > 0 || invalidFields.length > 0) {
    const allFieldsEmpty =
      !name && !email && !sdt && (!isNewUser || !password) && !role;

    if (allFieldsEmpty) {
      showCustomAlert("Vui lòng nhập đầy đủ thông tin", "error");
    } else {
      showCustomAlert("Vui lòng kiểm tra lại thông tin", "error");
    }
  }

  return isValid;
}

// 6. VALIDATION CHO CÁC FORM KHÁC (từ posCRUD.js)

// Validation cho form lọc ngày (POS và các form khác)
function validateDateFilter(e) {
  const fromDate = document.getElementById("fromDate");
  const toDate = document.getElementById("toDate");

  if (!fromDate || !toDate) return true; // Không có form ngày thì bỏ qua

  const from = fromDate.value;
  const to = toDate.value;

  if (!from && !to) {
    e.preventDefault();
    showCustomAlert("Vui lòng chọn ít nhất một ngày để lọc!", "warning");
    return false;
  }

  if (from && to && from > to) {
    e.preventDefault();
    showCustomAlert("'Từ ngày' phải nhỏ hơn hoặc bằng 'Đến ngày'!", "warning");
    return false;
  }

  return true;
}

// Validation cho form tìm kiếm mã đơn hàng (POS)
function validateOrderCodeSearch(e) {
  const orderCodeInput = document.getElementById("orderCode");
  const fromDateInput = document.getElementById("fromDate");
  const toDateInput = document.getElementById("toDate");

  if (!orderCodeInput) return true; // Không có form orderCode thì bỏ qua

  const orderCode = orderCodeInput.value.trim();
  const fromDate = fromDateInput ? fromDateInput.value : "";
  const toDate = toDateInput ? toDateInput.value : "";

  // Nếu có mã đơn hàng thì không cần ngày
  if (orderCode) {
    return true;
  }

  // Nếu không có mã đơn hàng thì phải có ít nhất một ngày
  if (!fromDate && !toDate) {
    e.preventDefault();
    showCustomAlert(
      "Vui lòng nhập mã đơn hàng hoặc chọn ngày để lọc!",
      "warning"
    );
    return false;
  }

  return true;
}

// 6. KHỞI TẠO TRANG

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
document.addEventListener("DOMContentLoaded", function () {
  // 1. Validation khi submit form sản phẩm
  const productForm = document.getElementById("productForm");
  if (productForm) {
    productForm.addEventListener("submit", function (event) {
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

  // 2. Validation cho tất cả form CRUD
  const allForms = document.querySelectorAll("form");
  allForms.forEach(function (form) {
    // Skip form sản phẩm vì đã có validation riêng
    if (form.id === "productForm") return;

    const hasDateInputs = form.querySelector("#fromDate, #toDate");
    const hasOrderCode = form.querySelector("#orderCode");

    form.addEventListener("submit", function (e) {
      let isValid = true;

      // Kiểm tra xem có phải là click vào nút "Làm mới" không
      const isRefreshAction =
        e.submitter &&
        (e.submitter.textContent.includes("Làm Mới") ||
          e.submitter.querySelector(".fa-sync") ||
          (e.submitter.href && e.submitter.href.includes("index")));

      if (isRefreshAction) {
        // Không validate cho nút "Làm mới"
        clearAllFieldErrors();
        return true;
      }

      // Validation cho form lọc (POS)
      if (hasDateInputs || hasOrderCode) {
        if (!validateDateFilter(e) || !validateOrderCodeSearch(e)) {
          isValid = false;
        }
        if (!isValid) {
          e.preventDefault();
          return false;
        }
        return true;
      }

      // Validation cho các CRUD form
      const currentUrl = window.location.pathname;

      // Category CRUD
      if (
        currentUrl.includes("/Category/") &&
        form.querySelector("#name") &&
        form.querySelector("#description")
      ) {
        if (!validateCategoryForm()) {
          e.preventDefault();
          return false;
        }
      }

      // Product Category CRUD
      else if (
        currentUrl.includes("/ProductCategory/") &&
        form.querySelector("#name")
      ) {
        if (!validateProductCategoryForm()) {
          e.preventDefault();
          return false;
        }
      }

      // Service CRUD
      else if (
        currentUrl.includes("/Service/") &&
        form.querySelector("#name") &&
        form.querySelector("#image1")
      ) {
        if (!validateServiceForm()) {
          e.preventDefault();
          return false;
        }
      }

      // Promotion CRUD
      else if (
        currentUrl.includes("/Promotion/") &&
        form.querySelector("#title") &&
        form.querySelector("#discountType")
      ) {
        if (!validatePromotionForm()) {
          e.preventDefault();
          return false;
        }
      }

      // User CRUD
      else if (
        currentUrl.includes("/User/") &&
        form.querySelector("#name") &&
        form.querySelector("#email")
      ) {
        if (!validateUserForm()) {
          e.preventDefault();
          return false;
        }
      }

      return true;
    });
  });

  // 3. Validation real-time cho ngày
  const fromDateInput = document.getElementById("fromDate");
  const toDateInput = document.getElementById("toDate");

  if (fromDateInput && toDateInput) {
    function validateDatesRealTime() {
      const from = fromDateInput.value;
      const to = toDateInput.value;

      // Clear previous errors
      clearFieldError("fromDate");
      clearFieldError("toDate");

      if (from && to && from > to) {
        showFieldError("toDate", "'Đến ngày' phải sau 'Từ ngày'");
      }
    }

    fromDateInput.addEventListener("change", validateDatesRealTime);
    toDateInput.addEventListener("change", validateDatesRealTime);
  }

  // 4. Kiểm tra lỗi từ backend khi trang load
  checkBackendValidationErrors();

  // 5. Kiểm tra flash messages và chuyển đổi thành custom alerts
  checkFlashMessages();

  // 7. Tự động xóa lỗi khi user bắt đầu nhập (cho tất cả input)
  const allInputs = document.querySelectorAll("input, select, textarea");
  allInputs.forEach(function (input) {
    input.addEventListener("input", function () {
      if (this.classList.contains("is-invalid")) {
        clearFieldError(this.id);
      }
      // Xóa lỗi backend validation khi user nhập
      const errorElement = this.parentNode.querySelector("i.error");
      if (errorElement && errorElement.textContent.trim()) {
        errorElement.textContent = "";
        errorElement.style.display = "none";
      }
    });

    input.addEventListener("change", function () {
      if (this.classList.contains("is-invalid")) {
        clearFieldError(this.id);
      }
      // Xóa lỗi backend validation khi user thay đổi
      const errorElement = this.parentNode.querySelector("i.error");
      if (errorElement && errorElement.textContent.trim()) {
        errorElement.textContent = "";
        errorElement.style.display = "none";
      }

      // Xóa lỗi role khi user chọn role
      if (this.name === "role") {
        const roleError = document.getElementById("role-error");
        if (roleError) roleError.remove();

        const roleContainer = this.closest(".d-flex");
        if (roleContainer) {
          roleContainer.style.border = "";
          roleContainer.style.borderRadius = "";
          roleContainer.style.padding = "";
        }
      }
    });
  });

  // 8. Thêm tooltip cho các nút action
  const viewButtons = document.querySelectorAll(
    "a[href*='orderId='], a[href*='edit'], a[href*='delete']"
  );
  viewButtons.forEach(function (btn) {
    if (!btn.title) {
      if (btn.href.includes("edit")) {
        btn.title = "Chỉnh sửa";
      } else if (btn.href.includes("delete")) {
        btn.title = "Xóa";
      } else if (btn.href.includes("orderId")) {
        btn.title = "Xem chi tiết đơn hàng";
      }
    }
  });

  // 9. Thêm confirmation cho các action nguy hiểm
  const deleteButtons = document.querySelectorAll(
    ".btn-danger, .delete-btn, a[href*='delete']"
  );

  // 10. Xử lý nút "Làm mới" - không trigger validation
  const refreshButtons = document.querySelectorAll(
    "a[href*='index'], .btn-success[href*='index'], a.btn-success"
  );
  refreshButtons.forEach(function (btn) {
    // Kiểm tra nếu là nút "Làm mới" dựa vào text hoặc icon
    const btnText = btn.textContent.trim().toLowerCase();
    const hasRefreshIcon = btn.querySelector(".fa-sync, .fa-refresh");

    if (
      btnText.includes("làm mới") ||
      btnText.includes("refresh") ||
      hasRefreshIcon
    ) {
      btn.addEventListener("click", function (e) {
        // Xóa tất cả lỗi trước khi chuyển trang
        clearAllFieldErrors();
        // Không preventDefault - để link hoạt động bình thường
      });
    }
  });
});
