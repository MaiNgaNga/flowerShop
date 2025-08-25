// Initialize AOS
AOS.init({
  duration: 800,
  once: true,
  offset: 100,
});

document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("contact-form");
  const submitBtn = document.getElementById("submitBtn");

  if (form) {
    form.addEventListener("submit", function (e) {
      e.preventDefault(); // Ngăn reload trang

      // Kiểm tra đăng nhập trước
      const fullNameInput = form.querySelector('input[name="fullName"]');
      const emailInput = form.querySelector('input[name="email"]');
      const phoneInput = form.querySelector('input[name="phone"]');

      // Nếu các trường thông tin cá nhân trống, có thể chưa đăng nhập
      if (
        !fullNameInput ||
        !fullNameInput.value.trim() ||
        !emailInput ||
        !emailInput.value.trim() ||
        !phoneInput ||
        !phoneInput.value.trim()
      ) {
        showErrorToast("Vui lòng đăng nhập để sử dụng dịch vụ!");
        setTimeout(() => {
          window.location.href = "/login?redirect=/services&loginRequired=true";
        }, 2000);
        return;
      }

      // Kiểm tra validation dịch vụ
      const serviceSelect = form.querySelector('select[name="service"]');
      if (!serviceSelect || !serviceSelect.value) {
        showErrorToast("Vui lòng chọn dịch vụ trước khi gửi yêu cầu!");
        if (serviceSelect) serviceSelect.focus();
        return;
      }

      // Chỉ disable nút để tránh double click
      if (submitBtn) submitBtn.disabled = true;

      const formData = new FormData(form);

      fetch(form.action, {
        method: "POST",
        body: formData,
      })
        .then((response) => {
          // Kiểm tra nếu server redirect đến login
          if (response.url.includes("/login")) {
            showErrorToast("Vui lòng đăng nhập để sử dụng dịch vụ!");
            setTimeout(() => {
              window.location.href =
                "/login?redirect=/services&loginRequired=true";
            }, 2000);
            return;
          }

          if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
          }
          return response.text();
        })
        .then((data) => {
          if (data) {
            // Chỉ xử lý nếu có data (không phải redirect)
            // Hiển thị thông báo thành công
            showSuccessToast(
              "Cảm ơn bạn đã đặt dịch vụ! Chúng tôi sẽ liên hệ với bạn trong thời gian sớm nhất."
            );

            // Reset form nhưng giữ lại thông tin người dùng
            resetFormKeepUserInfo();

            // Scroll to form để người dùng thấy form đã reset
            form.scrollIntoView({ behavior: "smooth", block: "center" });
          }
        })
        .catch((error) => {
          console.error("Error occurred:", error);
          showErrorToast("Đã xảy ra lỗi khi gửi yêu cầu. Vui lòng thử lại!");
        })
        .finally(() => {
          // Luôn enable lại nút
          if (submitBtn) submitBtn.disabled = false;
        });
    });
  }

  function resetFormKeepUserInfo() {
    // Chỉ reset dropdown dịch vụ, giữ lại thông tin cá nhân
    const serviceSelect = form.querySelector('select[name="service"]');
    if (serviceSelect) {
      serviceSelect.value = "";
    }
  }

  function showSuccessToast(message) {
    const toastHtml = `
      <div class="toast toast-elegant toast-success show" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-progress"></div>
        <div class="toast-content">
          <div class="toast-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="toast-message">${message}</div>
          <button type="button" class="toast-close" aria-label="Close">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
    `;
    showToast(toastHtml);
  }

  function showErrorToast(message) {
    console.log("Showing error toast:", message); // Debug
    const toastHtml = `
      <div class="toast toast-elegant toast-error show" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-progress"></div>
        <div class="toast-content">
          <div class="toast-icon">
            <i class="fas fa-exclamation-circle"></i>
          </div>
          <div class="toast-message">${message}</div>
          <button type="button" class="toast-close" aria-label="Close">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
    `;
    showToast(toastHtml);
  }

  function showToast(toastHtml) {
    console.log("Creating toast"); // Debug

    // Xóa toast cũ nếu có
    const existingContainer = document.getElementById("customToastContainer");
    if (existingContainer) {
      existingContainer.remove();
    }

    // Tạo container mới
    const container = document.createElement("div");
    container.id = "customToastContainer";
    document.body.appendChild(container);

    container.innerHTML = toastHtml;
    const toastElement = container.querySelector(".toast");
    const progressBar = toastElement.querySelector(".toast-progress");

    console.log("Toast element created:", toastElement); // Debug

    // Thêm hiệu ứng vào
    toastElement.classList.add("animate__animated", "animate__slideInRight");

    // Animate progress bar
    setTimeout(() => {
      if (progressBar) {
        progressBar.style.width = "0%";
      }
    }, 100);

    // Xử lý sự kiện đóng toast
    const closeBtn = toastElement.querySelector(".toast-close");
    if (closeBtn) {
      closeBtn.addEventListener("click", (e) => {
        console.log("Close button clicked"); // Debug
        e.preventDefault();
        e.stopPropagation();
        hideToast();
      });
    }

    // Hàm ẩn toast
    function hideToast() {
      console.log("Hiding toast"); // Debug
      toastElement.classList.remove("animate__slideInRight");
      toastElement.classList.add("animate__slideOutRight");

      setTimeout(() => {
        if (container && container.parentNode) {
          container.remove();
          console.log("Toast removed"); // Debug
        }
      }, 500);
    }

    // Auto hide sau 5 giây
    setTimeout(() => {
      if (container && container.parentNode) {
        hideToast();
      }
    }, 5000);
  }

  // Smooth scroll cho pagination
  document.querySelectorAll(".pagination a").forEach((link) => {
    link.addEventListener("click", function (e) {
      e.preventDefault();
      const href = this.getAttribute("href");

      // Thêm hiệu ứng loading cho pagination
      document.body.style.opacity = "0.7";

      setTimeout(() => {
        window.location.href = href;
      }, 200);
    });
  });

  // Flower Petal Animation (from banner_service.html)
  const particleContainer = document.getElementById("flower-particles");
  if (particleContainer) {
    function createPetal() {
      const petal = document.createElement("div");
      petal.classList.add("petal");
      petal.style.left = Math.random() * 100 + "vw";
      petal.style.animationDuration = Math.random() * 5 + 5 + "s";
      petal.style.opacity = Math.random() * 0.4 + 0.3;
      petal.style.setProperty("--drift", Math.random() > 0.5 ? 1 : -1);
      particleContainer.appendChild(petal);
      setTimeout(() => petal.remove(), 10000);
    }
    setInterval(createPetal, 300);
  }
});
