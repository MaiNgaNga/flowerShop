// ==========================================
// HỆ THỐNG THÔNG BÁO ALERT ĐẸP
// ==========================================
function showCustomAlert(message, type = "success") {
  // Xóa các alert cũ
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

  // Tự động ẩn sau 6 giây
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

// Xử lý mở bill ở cửa sổ mới khi thanh toán tiền mặt và thông báo phương thức thanh toán
document.addEventListener("DOMContentLoaded", function () {
  const posForm = document.getElementById("posForm");
  if (posForm) {
    posForm.addEventListener("submit", function (e) {
      const paymentMethod = posForm.querySelector(
        'select[name="paymentMethod"]'
      ).value;
      if (paymentMethod === "cash") {
        e.preventDefault();
        const formData = new FormData(posForm);
        fetch(posForm.action, {
          method: "POST",
          body: formData,
        })
          .then((response) => {
            if (
              response.redirected &&
              response.url.includes("/pos/bill?orderCode=")
            ) {
              const { url } = response;
              const w = 700,
                h = 600;
              const left = window.screenX + (window.outerWidth - w) / 2;
              const top = window.screenY + (window.outerHeight - h) / 2;
              window.open(
                url,
                "_blank",
                `width=${w},height=${h},left=${left},top=${top},resizable=yes,scrollbars=yes`
              );
              window.location.href = "/pos?success=payment_completed";
            } else {
              window.location.href = response.url;
            }
          })
          .catch(() => {
            showCustomAlert("Có lỗi khi xác nhận bán hàng!", "error");
          });
      }
    });
  }

  // Cập nhật thông báo khi chọn phương thức thanh toán
  const paymentSelect = document.getElementById("paymentMethodSelect");
  const paymentNote = document.getElementById("paymentNote");

  if (paymentSelect && paymentNote) {
    paymentSelect.addEventListener("change", function () {
      if (this.value === "cash") {
        paymentNote.textContent =
          "Thanh toán tiền mặt sẽ hoàn tất đơn hàng ngay lập tức.";
        paymentNote.className = "form-text text-success";
      } else if (this.value === "qr_code") {
        paymentNote.textContent =
          "Chuyển khoản cần nhân viên xác nhận sau khi khách thanh toán.";
        paymentNote.className = "form-text text-warning";
      }
    });
  }
});
function setColorFilter(color) {
  document.getElementById("colorInput").value = color;
  document.getElementById("filterForm").submit();
}
function setTypeFilter(type) {
  document.getElementById("typeInput").value = type;
  document.getElementById("filterForm").submit();
}
function setPriceFilter(min, max) {
  document.getElementById("minPriceInput").value = min;
  document.getElementById("maxPriceInput").value = max;
  document.getElementById("filterForm").submit();
}
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
        <td><button type="button" onclick="removeFromCart(${
          item.productId
        })" class="btn btn-link p-0" title="Xóa" style="text-decoration: none;"><span style="color: red; font-size: 18px; text-decoration: none;">&#10006;</span></button></td>
      `;
      cartRows.appendChild(row);
      total += item.price * item.quantity;
    });
  } else {
    console.error("cart không phải là mảng:", cart);
  }
  document.getElementById("totalAmount").innerText = total.toLocaleString();
}

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

function resetFilterForm() {
  const form = document.getElementById("filterForm");
  form.reset();
  form.querySelectorAll("select").forEach((sel) => (sel.selectedIndex = 0));
  document.getElementById("filterKeyword").value = "";
  form.submit();
}

window.onload = function () {
  // Xử lý thông báo từ backend (Thymeleaf)
  const successElements = document.querySelectorAll(".alert-success");
  successElements.forEach(function (element) {
    const message = element.textContent.trim();
    if (message) {
      showCustomAlert(message, "success");
      element.style.display = "none"; // Ẩn alert Bootstrap gốc
    }
  });

  const errorElements = document.querySelectorAll(".alert-danger");
  errorElements.forEach(function (element) {
    const message = element.textContent.trim();
    if (message) {
      showCustomAlert(message, "error");
      element.style.display = "none"; // Ẩn alert Bootstrap gốc
    }
  });

  const warningElements = document.querySelectorAll(".alert-warning");
  warningElements.forEach(function (element) {
    const message = element.textContent.trim();
    if (message) {
      showCustomAlert(message, "warning");
      element.style.display = "none"; // Ẩn alert Bootstrap gốc
    }
  });

  // Load giỏ hàng
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

  // Xử lý thông báo từ URL parameters
  const urlParams = new URLSearchParams(window.location.search);

  // Xử lý success messages
  if (urlParams.get("success") === "payment_completed") {
    showCustomAlert("Thanh toán thành công!", "success");
    window.history.replaceState({}, document.title, window.location.pathname);
  }

  // Xử lý error messages
  const errorParam = urlParams.get("error");
  if (errorParam) {
    let errorMessage = "Có lỗi xảy ra";

    switch (errorParam) {
      case "empty_cart":
        errorMessage =
          "Giỏ hàng trống! Vui lòng chọn sản phẩm trước khi thanh toán.";
        break;
      case "qr_generation_failed":
        errorMessage =
          "Không thể tạo mã QR thanh toán. Vui lòng thử lại hoặc liên hệ hỗ trợ.";
        break;
      case "system_error":
        errorMessage = "Lỗi hệ thống! Vui lòng thử lại sau.";
        break;
      case "invalid_request":
        errorMessage = "Yêu cầu không hợp lệ!";
        break;
      default:
        errorMessage = "Có lỗi xảy ra: " + errorParam;
    }

    showCustomAlert(errorMessage, "error");
    window.history.replaceState({}, document.title, window.location.pathname);
  }
};
