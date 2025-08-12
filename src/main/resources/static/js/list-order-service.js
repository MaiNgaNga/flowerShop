// JavaScript cho trang list-order-service.html

// Filter Functions
function clearRequestFilter() {
  const form = document.getElementById("requestFilterForm");
  form.querySelector('select[name="status"]').value = "";
  form.querySelector('input[name="keyword"]').value = "";
  form.querySelector('input[name="requestMonth"]').value = "";
  form.submit();
}

function clearOrderFilter() {
  const form = document.getElementById("orderFilterForm");
  form.querySelector('select[name="orderStatus"]').value = "";
  form.querySelector('input[name="orderKeyword"]').value = "";
  form.querySelector('input[name="month"]').value = "";
  form.submit();
}

// Tab Management
document.addEventListener("DOMContentLoaded", function () {
  const tabs = document.querySelectorAll('#adminTabs button[data-bs-toggle="tab"]');
  tabs.forEach((tab) => {
    tab.addEventListener("shown.bs.tab", function (event) {
      const targetTab = event.target.getAttribute("data-bs-target");
      if (targetTab === "#orders") {
        localStorage.setItem("currentTab", "orders");
      } else {
        localStorage.setItem("currentTab", "requests");
      }
    });
  });

  if (localStorage.getItem("switchToOrdersTab") === "true") {
    localStorage.removeItem("switchToOrdersTab");
    setTimeout(() => {
      document.getElementById("orders-tab").click();
    }, 100);
  } else {
    const currentTab = localStorage.getItem("currentTab");
    if (currentTab === "orders") {
      setTimeout(() => {
        document.getElementById("orders-tab").click();
      }, 100);
    }
  }
});

// Filter Results Update
function updateFilterResults() {
  const requestTotalElements = /*[[${requestTotalElements}]]*/ 0;
  const orderTotalElements = /*[[${orderTotalElements}]]*/ 0;

  const requestTab = document.getElementById("requests-tab");
  if (requestTotalElements > 0) {
    requestTab.innerHTML = `Danh sách yêu cầu (${requestTotalElements})`;
  } else {
    requestTab.innerHTML = "Danh sách yêu cầu";
  }

  const orderTab = document.getElementById("orders-tab");
  if (orderTotalElements > 0) {
    orderTab.innerHTML = `Đơn hàng dịch vụ (${orderTotalElements})`;
  } else {
    orderTab.innerHTML = "Đơn hàng dịch vụ";
  }
}

// Filter Summary Update
function updateFilterSummary() {
  const urlParams = new URLSearchParams(window.location.search);

  // Update Request Filter Summary
  const requestStatus = urlParams.get("status");
  const requestKeyword = urlParams.get("keyword");
  const requestMonth = urlParams.get("requestMonth");
  const requestFilterSummary = document.getElementById("requestFilterSummary");
  const requestFilterTags = document.getElementById("requestFilterTags");
  const requestFilterSection = document.querySelector("#requests .filter-section");

  let requestTags = [];
  if (requestStatus) {
    const statusText = document.querySelector(`#requests select[name="status"] option[value="${requestStatus}"]`)?.textContent || requestStatus;
    requestTags.push(`<span class="filter-tag">Trạng thái: ${statusText}</span>`);
  }
  if (requestKeyword) {
    requestTags.push(`<span class="filter-tag">Tìm kiếm: ${requestKeyword}</span>`);
  }
  if (requestMonth) {
    const monthText = new Date(requestMonth + "-01").toLocaleDateString("vi-VN", { year: "numeric", month: "long" });
    requestTags.push(`<span class="filter-tag">Tháng: ${monthText}</span>`);
  }

  if (requestTags.length > 0) {
    requestFilterTags.innerHTML = requestTags.join("");
    requestFilterSummary.style.display = "block";
    requestFilterSection.classList.add("filter-active");
  } else {
    requestFilterSummary.style.display = "none";
    requestFilterSection.classList.remove("filter-active");
  }

  // Update Order Filter Summary
  const orderStatus = urlParams.get("orderStatus");
  const orderKeyword = urlParams.get("orderKeyword");
  const month = urlParams.get("month");
  const orderFilterSummary = document.getElementById("orderFilterSummary");
  const orderFilterTags = document.getElementById("orderFilterTags");
  const orderFilterSection = document.querySelector("#orders .filter-section");

  let orderTags = [];
  if (orderStatus) {
    const statusText = document.querySelector(`#orders select[name="orderStatus"] option[value="${orderStatus}"]`)?.textContent || orderStatus;
    orderTags.push(`<span class="filter-tag">Trạng thái: ${statusText}</span>`);
  }
  if (orderKeyword) {
    orderTags.push(`<span class="filter-tag">Tìm kiếm: ${orderKeyword}</span>`);
  }
  if (month) {
    const monthText = new Date(month + "-01").toLocaleDateString("vi-VN", { year: "numeric", month: "long" });
    orderTags.push(`<span class="filter-tag">Tháng: ${monthText}</span>`);
  }

  if (orderTags.length > 0) {
    orderFilterTags.innerHTML = orderTags.join("");
    orderFilterSummary.style.display = "block";
    orderFilterSection.classList.add("filter-active");
  } else {
    orderFilterSummary.style.display = "none";
    orderFilterSection.classList.remove("filter-active");
  }
}

// Initialize on DOM load
document.addEventListener("DOMContentLoaded", function () {
  setTimeout(() => {
    updateFilterResults();
    updateFilterSummary();
  }, 200);
});

// Global variables
let currentOrderId = null;

// Custom Alert System
function showCustomAlert(message, type = "success") {
  document.querySelectorAll(".custom-alert").forEach((alert) => alert.remove());

  const alertDiv = document.createElement("div");
  alertDiv.className = `custom-alert alert-${type}`;

  const icon = type === "success" ? "bi-check-circle-fill" : type === "error" ? "bi-x-circle-fill" : "bi-exclamation-triangle-fill";

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

// Modal Loading System
function showModalLoading(modal, show) {
  const modalBody = modal.querySelector(".modal-body");
  const modalFooter = modal.querySelector(".modal-footer");

  if (show) {
    if (!modal.querySelector(".loading-overlay")) {
      const loadingOverlay = document.createElement("div");
      loadingOverlay.className = "loading-overlay";
      loadingOverlay.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
        border-radius: 15px;
      `;
      loadingOverlay.innerHTML = `
        <div style="text-align: center;">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <div style="margin-top: 10px; color: #666;">Đang tải dữ liệu...</div>
        </div>
      `;
      modal.querySelector(".modal-content").style.position = "relative";
      modal.querySelector(".modal-content").appendChild(loadingOverlay);
    }

    modalBody.style.pointerEvents = "none";
    modalFooter.style.pointerEvents = "none";
  } else {
    const loadingOverlay = modal.querySelector(".loading-overlay");
    if (loadingOverlay) {
      loadingOverlay.remove();
    }

    modalBody.style.pointerEvents = "auto";
    modalFooter.style.pointerEvents = "auto";
  }
}

// Custom Confirm System
function showCustomConfirm(message, onConfirm, onCancel = null) {
  document.querySelectorAll(".custom-confirm").forEach((confirm) => confirm.remove());

  const confirmDiv = document.createElement("div");
  confirmDiv.className = "custom-confirm";
  confirmDiv.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10000;
  `;

  confirmDiv.innerHTML = `
    <div style="
      background: white;
      padding: 25px;
      border-radius: 10px;
      max-width: 400px;
      width: 90%;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    ">
      <div style="
        display: flex;
        align-items: center;
        gap: 15px;
        margin-bottom: 20px;
      ">
        <i class="bi bi-question-circle-fill" style="font-size: 24px; color: #0d6efd;"></i>
        <span style="font-size: 16px; font-weight: 500; color: #333;">${message}</span>
      </div>
      <div style="
        display: flex;
        gap: 10px;
        justify-content: flex-end;
      ">
        <button class="btn btn-secondary confirm-cancel" style="padding: 8px 20px;">Hủy</button>
        <button class="btn btn-primary confirm-ok" style="padding: 8px 20px;">Đồng ý</button>
      </div>
    </div>
  `;

  document.body.appendChild(confirmDiv);

  confirmDiv.querySelector(".confirm-ok").onclick = () => {
    confirmDiv.remove();
    if (onConfirm) onConfirm();
  };

  confirmDiv.querySelector(".confirm-cancel").onclick = () => {
    confirmDiv.remove();
    if (onCancel) onCancel();
  };

  confirmDiv.onclick = (e) => {
    if (e.target === confirmDiv) {
      confirmDiv.remove();
      if (onCancel) onCancel();
    }
  };
}

// Validation Functions
function validateRequestForm(price, district, address, time) {
  let isValid = true;
  const errors = [];

  const form = document.getElementById("createOrderForm");
  form.querySelectorAll(".is-invalid").forEach((el) => {
    el.classList.remove("is-invalid");
  });
  form.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());

  const priceStr = price.toString().trim();
  const priceInput = form.querySelector("input[name='quotedPrice']");
  if (!priceStr) {
    addValidationErrorToElement(priceInput, "Vui lòng nhập giá");
    errors.push("Vui lòng nhập giá");
    isValid = false;
  } else {
    if (!/^[\d,.\s]+$/.test(priceStr)) {
      addValidationErrorToElement(priceInput, "Giá chỉ được chứa số");
      errors.push("Giá chỉ được chứa số");
      isValid = false;
    } else {
      const numericPrice = parseFloat(priceStr.replace(/[,\s]/g, ""));
      if (isNaN(numericPrice) || numericPrice <= 0) {
        addValidationErrorToElement(priceInput, "Giá phải là số dương lớn hơn 0");
        errors.push("Giá phải là số dương lớn hơn 0");
        isValid = false;
      } else if (numericPrice > 999999999) {
        addValidationErrorToElement(priceInput, "Giá không được vượt quá 999,999,999 VNĐ");
        errors.push("Giá không được vượt quá 999,999,999 VNĐ");
        isValid = false;
      }
    }
  }

  const districtSelect = form.querySelector("select[name='district']");
  if (!district || district.trim() === "") {
    addValidationErrorToElement(districtSelect, "Vui lòng chọn quận/huyện");
    errors.push("Vui lòng chọn quận/huyện");
    isValid = false;
  }

  const addressInput = form.querySelector("input[name='addressDetail']");
  if (!address || address.trim() === "") {
    addValidationErrorToElement(addressInput, "Vui lòng nhập địa chỉ chi tiết");
    errors.push("Vui lòng nhập địa chỉ chi tiết");
    isValid = false;
  } else if (address.trim().length < 5) {
    addValidationErrorToElement(addressInput, "Địa chỉ chi tiết phải có ít nhất 5 ký tự");
    errors.push("Địa chỉ chi tiết phải có ít nhất 5 ký tự");
    isValid = false;
  } else if (address.trim().length > 255) {
    addValidationErrorToElement(addressInput, "Địa chỉ chi tiết không được vượt quá 255 ký tự");
    errors.push("Địa chỉ chi tiết không được vượt quá 255 ký tự");
    isValid = false;
  }

  const timeInput = form.querySelector("input[name='executionTime']");
  if (!time) {
    addValidationErrorToElement(timeInput, "Vui lòng chọn thời gian thực hiện");
    errors.push("Vui lòng chọn thời gian thực hiện");
    isValid = false;
  } else {
    const selectedDate = new Date(time);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (selectedDate < today) {
      addValidationErrorToElement(timeInput, "Thời gian thực hiện không được là ngày trong quá khứ");
      errors.push("Thời gian thực hiện không được là ngày trong quá khứ");
      isValid = false;
    }

    const oneYearFromNow = new Date();
    oneYearFromNow.setFullYear(oneYearFromNow.getFullYear() + 1);
    if (selectedDate > oneYearFromNow) {
      addValidationErrorToElement(timeInput, "Thời gian thực hiện không được quá 1 năm từ hiện tại");
      errors.push("Thời gian thực hiện không được quá 1 năm từ hiện tại");
      isValid = false;
    }
  }

  return { isValid, errors };
}

function addValidationErrorToElement(element, message) {
  if (!element) return;

  element.classList.add("is-invalid");

  const feedback = document.createElement("div");
  feedback.className = "invalid-feedback";
  feedback.textContent = message;
  element.parentNode.appendChild(feedback);
}

// Modal Management System for createOrderModal
document.addEventListener("DOMContentLoaded", function () {
  if (localStorage.getItem("switchToOrdersTab") === "true") {
    localStorage.removeItem("switchToOrdersTab");
    setTimeout(() => {
      document.getElementById("orders-tab").click();
    }, 500);
  }

  const modal = document.getElementById("createOrderModal");
  const form = document.getElementById("createOrderForm");

  if (!modal || !form) return;

  // Modal Manager
  let modalManager = {
    isProcessing: false,
    currentRequestId: null,
    loadingPromise: null,
    isRestoreAction: false,

    forceCleanupModal: function () {
      console.log("🧹 Force cleanup modal...");
      if (this.loadingPromise) {
        this.loadingPromise.abort?.();
        this.loadingPromise = null;
      }
      const bsModal = bootstrap.Modal.getInstance(modal);
      if (bsModal) {
        bsModal.hide();
      }
      modal.style.display = "none";
      modal.classList.remove("show");
      modal.setAttribute("aria-hidden", "true");
      modal.removeAttribute("aria-modal");
      document.body.classList.remove("modal-open");
      document.body.style.overflow = "";
      document.body.style.paddingRight = "";
      document.querySelectorAll(".modal-backdrop").forEach((el) => {
        el.remove();
      });
      this.isProcessing = false;
      this.currentRequestId = null;
      console.log("✅ Modal cleanup completed");
    },

    canOpenModal: function () {
      if (this.isRestoreAction) {
        console.log("✅ Restore action detected, allowing immediate modal open");
        return true;
      }
      if (this.isProcessing) {
        console.log("❌ Modal đang xử lý, không thể mở");
        return false;
      }
      return true;
    },

    markActionCompleted: function () {
      this.isRestoreAction = false;
      this.forceCleanupModal();
    },

    markRestoreCompleted: function () {
      console.log("🔄 Restore action completed");
      this.isRestoreAction = true;
      this.isProcessing = false;
      this.currentRequestId = null;
      this.loadingPromise = null;
      setTimeout(() => {
        this.isRestoreAction = false;
        console.log("🔄 Restore flag auto-reset");
      }, 3000);
    },

    reset: function () {
      console.log("🔄 Reset modal manager");
      this.isProcessing = false;
      this.currentRequestId = null;
      this.loadingPromise = null;
      this.isRestoreAction = false;
    },
  };

  // Modal show event
  modal.addEventListener("show.bs.modal", function (event) {
    console.log("🚀 Attempting to open modal...");
    if (!modalManager.canOpenModal()) {
      console.log("❌ Modal bị block, preventing open");
      event.preventDefault();
      return;
    }
    if (!modalManager.isRestoreAction) {
      modalManager.forceCleanupModal();
    }
    modalManager.isProcessing = true;
    loadModalData(event);
  });

  function loadModalData(event) {
    const button = event.relatedTarget;
    const requestId = button.getAttribute("data-request-id");
    const serviceName = button.getAttribute("data-service-name");
    const status = button.getAttribute("data-status");

    document.getElementById("requestIdInput").value = requestId;
    document.getElementById("serviceNameInput").value = serviceName;
    form.setAttribute("data-request-id", requestId);

    const btnContact = modal.querySelector(".btn-contact");
    const btnUpdate = modal.querySelector(".btn-update");
    const btnConfirm = modal.querySelector(".btn-confirm");
    const btnCancel = modal.querySelector(".btn-cancel");

    form.reset();
    form.querySelectorAll(".is-invalid").forEach((el) => {
      el.classList.remove("is-invalid");
    });
    form.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());
    document.getElementById("serviceNameInput").value = serviceName;

    modalManager.currentRequestId = requestId;
    console.log(`📊 Loading data for request ${requestId}...`);
    showModalLoading(modal, true);

    const abortController = new AbortController();
    const cacheParam = modalManager.isRestoreAction ? 
      `t=${Date.now()}&r=${Math.random()}&restore=1&force=${Date.now()}` : 
      `t=${Date.now()}&r=${Math.random()}`;
    const fetchUrl = `/admin/service-requests/${requestId}/draft?${cacheParam}`;
    console.log("📡 Fetching fresh data from:", fetchUrl);
    
    if (modalManager.isRestoreAction) {
      console.log("🔄 Loading data after restore action - using aggressive cache busting");
    }

    const fetchPromise = fetch(fetchUrl, {
      method: "GET",
      headers: {
        "Cache-Control": "no-cache, no-store, must-revalidate",
        Pragma: "no-cache",
        Expires: "0",
        "X-Requested-With": "XMLHttpRequest",
      },
      signal: abortController.signal,
    });

    modalManager.loadingPromise = {
      abort: () => abortController.abort(),
    };

    fetchPromise
      .then((res) => res.json())
      .then((data) => {
        console.log("Dữ liệu nhận được:", data);

        if (data.status === "PENDING") {
          btnContact.style.display = "inline-flex";
          btnUpdate.style.display = "none";
          btnConfirm.style.display = "inline-flex";
          btnCancel.style.display = "inline-flex";
        } else if (data.status === "CONTACTED") {
          btnContact.style.display = "none";
          btnUpdate.style.display = "inline-flex";
          btnConfirm.style.display = "inline-flex";
          btnCancel.style.display = "inline-flex";

          console.log("Loading draft data:", data);

          if (data.quotedPrice !== undefined && data.quotedPrice !== null) {
            form.querySelector("input[name='quotedPrice']").value = data.quotedPrice;
          }
          if (data.addressDetail) {
            form.querySelector("input[name='addressDetail']").value = data.addressDetail;
          }
          if (data.executionTime) {
            form.querySelector("input[name='executionTime']").value = data.executionTime;
          }
          if (data.description) {
            form.querySelector("textarea[name='description']").value = data.description;
          }

          if (data.district) {
            let retryCount = 0;
            const maxRetries = 20;

            const loadDistrict = () => {
              const districtSelect = form.querySelector("select[name='district']");
              if (districtSelect && districtSelect.options.length > 1) {
                districtSelect.value = data.district;
                console.log("District loaded:", data.district);
              } else if (retryCount < maxRetries) {
                retryCount++;
                setTimeout(loadDistrict, 100);
              } else {
                console.warn("Could not load district after", maxRetries, "retries");
              }
            };

            setTimeout(loadDistrict, 200);
          }
        } else {
          btnContact.style.display = "none";
          btnUpdate.style.display = "none";
          btnConfirm.style.display = "none";
          btnCancel.style.display = "none";
        }
      })
      .catch((err) => {
        if (err.name !== "AbortError") {
          console.error("Lỗi load dữ liệu:", err);
          btnContact.style.display = "inline-flex";
          btnUpdate.style.display = "none";
          btnConfirm.style.display = "inline-flex";
          btnCancel.style.display = "inline-flex";
        }
      })
      .finally(() => {
        if (modalManager.currentRequestId === requestId) {
          console.log(`✅ Data loaded for request ${requestId}`);
          showModalLoading(modal, false);
          modalManager.isProcessing = false;
          modalManager.loadingPromise = null;
        } else {
          console.log(`⚠️ Stale request ${requestId}, current: ${modalManager.currentRequestId}`);
        }
      });
  }

  // Modal hidden event
  modal.addEventListener("hidden.bs.modal", function () {
    console.log("🔒 Modal hidden event triggered");
    modalManager.forceCleanupModal();
    const form = document.getElementById("createOrderForm");
    form.querySelectorAll(".is-invalid").forEach((el) => {
      el.classList.remove("is-invalid");
    });
    form.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());
    form.reset();
    showModalLoading(modal, false);
    console.log("🧹 Modal cleanup on hidden completed");
  });

  // Update UI after action
  const updateUIAfterAction = (requestId, actionType) => {
    const button = document.querySelector(`[data-request-id="${requestId}"]`);
    if (!button) return;

    const row = button.closest("tr");
    if (!row) return;

    const statusBadge = row.querySelector(".badge");
    if (!statusBadge) return;

    const actionCell = button.closest("td");
    if (!actionCell) return;

    if (actionType === "contact" || actionType === "update") {
      statusBadge.className = "badge bg-primary";
      statusBadge.textContent = "Đã liên hệ";
      button.setAttribute("data-status", "CONTACTED");
    } else if (actionType === "confirm") {
      statusBadge.className = "badge bg-success";
      statusBadge.textContent = "Đã xác nhận";

      actionCell.innerHTML = `
        <button
          class="btn btn-secondary btn-action"
          disabled
          title="Đã chốt đơn - không thể thao tác"
        >
          <i class="bi bi-lock"></i>
        </button>
      `;

      setTimeout(() => {
        showCustomAlert("Đơn hàng đã được tạo thành công!", "success");
      }, 500);
    } else if (actionType === "cancel") {
      statusBadge.className = "badge bg-secondary";
      statusBadge.textContent = "Đã hủy";

      const serviceName = button.getAttribute("data-service-name");

      const restoreButton = document.createElement("button");
      restoreButton.className = "btn btn-warning btn-action btn-restore";
      restoreButton.setAttribute("data-request-id", requestId);
      restoreButton.setAttribute("data-service-name", serviceName);
      restoreButton.setAttribute("title", "Khôi phục yêu cầu");
      restoreButton.innerHTML = '<i class="bi bi-arrow-clockwise"></i>';

      button.parentNode.replaceChild(restoreButton, button);
      
      modalManager.reset();
      console.log("🔄 Modal manager reset after cancel action");
    }
  };

  // Request action handler
  const requestAction = (type) => {
    const id = form.getAttribute("data-request-id");
    if (!id) {
      showCustomAlert("Thiếu ID yêu cầu", "error");
      return;
    }

    const url = `/admin/service-requests/${id}/${type}`;
    const formData = new FormData();

    if (type === "confirm" || type === "contact" || type === "update") {
      const price = form.querySelector("input[name='quotedPrice']").value;
      const district = form.querySelector("select[name='district']").value;
      const address = form.querySelector("input[name='addressDetail']").value;
      const time = form.querySelector("input[name='executionTime']").value;
      const desc = form.querySelector("textarea[name='description']").value;

      const requestValidation = validateRequestForm(price, district, address, time);
      if (!requestValidation.isValid) {
        showCustomAlert(
          "Vui lòng kiểm tra lại thông tin: " + requestValidation.errors.join(", "),
          "error"
        );
        return;
      }

      formData.append("quotedPrice", price);
      formData.append("district", district);
      formData.append("addressDetail", address);
      formData.append("executionTime", time);
      formData.append("description", desc);
    }

    fetch(url, {
      method: "POST",
      body: formData,
    })
      .then((res) => res.json())
      .then((data) => {
        console.log("Response data:", data);

        if (data.success) {
          console.log(`✅ Action ${type} successful for request ${id}`);
          modalManager.markActionCompleted();

          setTimeout(() => {
            updateUIAfterAction(id, type);

            if (type !== "confirm") {
              let message = data.success;
              if (message) {
                if (message.includes("liên hệ") && (message.includes("lưu") || message.includes("tạm"))) {
                  message = "Đã liên hệ thành công!";
                } else if (message.includes("đã liên hệ")) {
                  message = "Đã liên hệ thành công!";
                }
              }
              showCustomAlert(message, "success");
            }
          }, 200);
        } else if (data.error) {
          console.log(`❌ Action ${type} failed for request ${id}: ${data.error}`);
          modalManager.markActionCompleted();
          setTimeout(() => {
            showCustomAlert("Lỗi: " + data.error, "error");
          }, 200);
        }
      })
      .catch((err) => {
        console.error("❌ Network/Exception error:", err);
        modalManager.markActionCompleted();
        setTimeout(() => {
          showCustomAlert("Có lỗi xảy ra.", "error");
        }, 200);
      });
  };

  // Button event listeners
  const btnContact = modal.querySelector(".btn-contact");
  const btnUpdate = modal.querySelector(".btn-update");
  const btnConfirm = modal.querySelector(".btn-confirm");
  const btnCancel = modal.querySelector(".btn-cancel");

  if (btnContact) {
    btnContact.addEventListener("click", () => {
      requestAction("contact");
    });
  }

  if (btnUpdate) {
    btnUpdate.addEventListener("click", () => {
      requestAction("update");
    });
  }

  if (btnConfirm) {
    btnConfirm.addEventListener("click", () => {
      requestAction("confirm");
    });
  }

  if (btnCancel) {
    btnCancel.addEventListener("click", () => {
      showCustomConfirm("Bạn có chắc muốn hủy yêu cầu này không?", () => {
        requestAction("cancel");
      });
    });
  }

  // Restore button handler
  document.addEventListener("click", function (event) {
    if (event.target.closest(".btn-restore")) {
      const button = event.target.closest(".btn-restore");
      const requestId = button.getAttribute("data-request-id");
      const serviceName = button.getAttribute("data-service-name");

      showCustomConfirm(
        "Bạn có chắc muốn khôi phục yêu cầu này không?",
        () => {
          console.log("🔄 Starting restore action for request:", requestId);
          
          fetch(`/admin/service-requests/${requestId}/restore`, {
            method: "POST",
          })
            .then((res) => res.json())
            .then((data) => {
              if (data.success) {
                console.log("✅ Restore successful:", data.success);
                showCustomAlert(data.success, "success");

                const row = button.closest("tr");
                const statusBadge = row.querySelector(".badge");

                let newStatus;
                if (data.success.includes("đã liên hệ")) {
                  statusBadge.className = "badge bg-primary";
                  statusBadge.textContent = "Đã liên hệ";
                  newStatus = "CONTACTED";
                } else {
                  statusBadge.className = "badge bg-warning text-dark";
                  statusBadge.textContent = "Đang chờ";
                  newStatus = "PENDING";
                }

                const newButton = document.createElement("button");
                newButton.className = "btn btn-success btn-action";
                newButton.setAttribute("data-bs-toggle", "modal");
                newButton.setAttribute("data-bs-target", "#createOrderModal");
                newButton.setAttribute("data-request-id", requestId);
                newButton.setAttribute("data-service-name", serviceName);
                newButton.setAttribute("data-status", newStatus);
                newButton.setAttribute("title", "Tạo đơn hàng");
                newButton.innerHTML = '<i class="bi bi-pencil"></i>';

                button.parentNode.replaceChild(newButton, button);
                modalManager.markRestoreCompleted();
                console.log("🎯 Modal manager marked restore completed - modal can now open immediately");

              } else if (data.error) {
                console.log("❌ Restore failed:", data.error);
                showCustomAlert("Lỗi: " + data.error, "error");
              }
            })
            .catch((err) => {
              console.error("❌ Restore error:", err);
              showCustomAlert("Có lỗi xảy ra khi khôi phục yêu cầu.", "error");
            });
        }
      );
    }
  });
});