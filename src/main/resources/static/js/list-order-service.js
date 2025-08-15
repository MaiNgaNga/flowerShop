// Filter Functions Script
// Hàm xóa lọc cho tab yêu cầu
function clearRequestFilter() {
  const form = document.getElementById("requestFilterForm");
  form.querySelector('select[name="status"]').value = "";
  form.querySelector('input[name="keyword"]').value = "";
  form.querySelector('input[name="requestMonth"]').value = "";
  form.submit();
}

// Hàm xóa lọc cho tab đơn hàng
function clearOrderFilter() {
  const form = document.getElementById("orderFilterForm");
  form.querySelector('select[name="orderStatus"]').value = "";
  form.querySelector('input[name="orderKeyword"]').value = "";
  form.querySelector('input[name="month"]').value = "";
  form.submit();
}

// Xử lý chuyển tab và giữ nguyên filter
document.addEventListener("DOMContentLoaded", function () {
  // Xử lý sự kiện khi chuyển tab
  const tabs = document.querySelectorAll(
    '#adminTabs button[data-bs-toggle="tab"]'
  );
  tabs.forEach((tab) => {
    tab.addEventListener("shown.bs.tab", function (event) {
      const targetTab = event.target.getAttribute("data-bs-target");

      // Lưu tab hiện tại vào localStorage
      if (targetTab === "#orders") {
        localStorage.setItem("currentTab", "orders");
      } else {
        localStorage.setItem("currentTab", "requests");
      }
    });
  });

  // Chỉ khôi phục tab từ localStorage nếu có switchToOrdersTab flag
  if (localStorage.getItem("switchToOrdersTab") === "true") {
    localStorage.removeItem("switchToOrdersTab");
    setTimeout(() => {
      document.getElementById("orders-tab").click();
    }, 100);
  } else {
    // Khôi phục tab cuối cùng được chọn
    const currentTab = localStorage.getItem("currentTab");
    if (currentTab === "orders") {
      setTimeout(() => {
        document.getElementById("orders-tab").click();
      }, 100);
    }
  }
});

// Hàm hiển thị số lượng kết quả lọc
function updateFilterResults() {
  // Lấy số lượng từ backend thay vì đếm DOM elements
  const requestTotalElements = /*[[${requestTotalElements}]]*/ 0;
  const orderTotalElements = /*[[${orderTotalElements}]]*/ 0;

  // Cập nhật số lượng cho tab yêu cầu
  const requestTab = document.getElementById("requests-tab");
  if (requestTotalElements > 0) {
    requestTab.innerHTML = `Danh sách yêu cầu (${requestTotalElements})`;
  } else {
    requestTab.innerHTML = "Danh sách yêu cầu";
  }

  // Cập nhật số lượng cho tab đơn hàng
  const orderTab = document.getElementById("orders-tab");
  if (orderTotalElements > 0) {
    orderTab.innerHTML = `Đơn hàng dịch vụ (${orderTotalElements})`;
  } else {
    orderTab.innerHTML = "Đơn hàng dịch vụ";
  }
}

// Hàm hiển thị filter summary
function updateFilterSummary() {
  const urlParams = new URLSearchParams(window.location.search);

  // Update Request Filter Summary
  const requestStatus = urlParams.get("status");
  const requestKeyword = urlParams.get("keyword");
  const requestMonth = urlParams.get("requestMonth");
  const requestFilterSummary = document.getElementById("requestFilterSummary");
  const requestFilterTags = document.getElementById("requestFilterTags");
  const requestFilterSection = document.querySelector(
    "#requests .filter-section"
  );

  let requestTags = [];
  if (requestStatus) {
    const statusText =
      document.querySelector(
        `#requests select[name="status"] option[value="${requestStatus}"]`
      )?.textContent || requestStatus;
    requestTags.push(
      `<span class="filter-tag">Trạng thái: ${statusText}</span>`
    );
  }
  if (requestKeyword) {
    requestTags.push(
      `<span class="filter-tag">Tìm kiếm: ${requestKeyword}</span>`
    );
  }
  if (requestMonth) {
    const monthText = new Date(requestMonth + "-01").toLocaleDateString(
      "vi-VN",
      { year: "numeric", month: "long" }
    );
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
    const statusText =
      document.querySelector(
        `#orders select[name="orderStatus"] option[value="${orderStatus}"]`
      )?.textContent || orderStatus;
    orderTags.push(`<span class="filter-tag">Trạng thái: ${statusText}</span>`);
  }
  if (orderKeyword) {
    orderTags.push(`<span class="filter-tag">Tìm kiếm: ${orderKeyword}</span>`);
  }
  if (month) {
    const monthText = new Date(month + "-01").toLocaleDateString("vi-VN", {
      year: "numeric",
      month: "long",
    });
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

// Gọi hàm cập nhật khi trang load
document.addEventListener("DOMContentLoaded", function () {
  setTimeout(() => {
    updateFilterResults();
    updateFilterSummary();
  }, 200);
});

// ============================================================================================= //

document.addEventListener("DOMContentLoaded", function () {
  // Kiểm tra nếu cần chuyển sang tab đơn hàng sau reload
  if (localStorage.getItem("switchToOrdersTab") === "true") {
    localStorage.removeItem("switchToOrdersTab");
    setTimeout(() => {
      document.getElementById("orders-tab").click();
    }, 500);
  }

  const modal = document.getElementById("createOrderModal");
  const form = document.getElementById("createOrderForm");

  // Hệ thống quản lý modal đơn giản
  let modalManager = {
    isProcessing: false,
    currentRequestId: null,
    loadingPromise: null,
    isRestoreAction: false,

    // Force cleanup modal hoàn toàn
    forceCleanupModal: function () {
      console.log("🧹 Force cleanup modal...");

      // 1. Hủy tất cả requests đang chạy
      if (this.loadingPromise) {
        this.loadingPromise.abort?.();
        this.loadingPromise = null;
      }

      // 2. Đóng modal bằng Bootstrap API
      const bsModal = bootstrap.Modal.getInstance(modal);
      if (bsModal) {
        bsModal.hide();
      }

      // 3. Force cleanup DOM
      modal.style.display = "none";
      modal.classList.remove("show");
      modal.setAttribute("aria-hidden", "true");
      modal.removeAttribute("aria-modal");

      // 4. Cleanup body và backdrop
      document.body.classList.remove("modal-open");
      document.body.style.overflow = "";
      document.body.style.paddingRight = "";

      // 5. Remove tất cả backdrop
      document.querySelectorAll(".modal-backdrop").forEach((el) => {
        el.remove();
      });

      // 6. Ensure no duplicate backdrops
      setTimeout(() => {
        const backdrops = document.querySelectorAll(".modal-backdrop");
        if (backdrops.length > 1) {
          backdrops.forEach((backdrop, index) => {
            if (index > 0) backdrop.remove();
          });
        }
      }, 100);

      // 7. Reset state
      this.isProcessing = false;
      this.currentRequestId = null;

      console.log("✅ Modal cleanup completed");
    },

    // Kiểm tra có thể mở modal không
    canOpenModal: function () {
      // Nếu vừa thực hiện restore action, luôn cho phép mở modal
      if (this.isRestoreAction) {
        console.log(
          "✅ Restore action detected, allowing immediate modal open"
        );
        return true;
      }

      // Chỉ block nếu đang xử lý request
      if (this.isProcessing) {
        console.log("❌ Modal đang xử lý, không thể mở");
        return false;
      }

      return true;
    },

    // Đánh dấu action hoàn thành
    markActionCompleted: function () {
      this.isRestoreAction = false;
      this.forceCleanupModal();
    },

    // Đánh dấu restore action hoàn thành
    markRestoreCompleted: function () {
      console.log("🔄 Restore action completed");
      this.isRestoreAction = true;
      this.isProcessing = false;
      this.currentRequestId = null;
      this.loadingPromise = null;

      // Auto reset sau 3 giây
      setTimeout(() => {
        this.isRestoreAction = false;
        console.log("🔄 Restore flag auto-reset");
      }, 3000);
    },

    // Reset hoàn toàn
    reset: function () {
      console.log("🔄 Reset modal manager");
      this.isProcessing = false;
      this.currentRequestId = null;
      this.loadingPromise = null;
      this.isRestoreAction = false;
    },
  };

  modal.addEventListener("show.bs.modal", function (event) {
    console.log("🚀 Attempting to open modal...");

    // Kiểm tra có thể mở modal không
    if (!modalManager.canOpenModal()) {
      console.log("❌ Modal bị block, preventing open");
      event.preventDefault();
      return;
    }

    // Force cleanup trước khi mở (trừ khi là restore action)
    if (!modalManager.isRestoreAction) {
      modalManager.forceCleanupModal();
    }

    // Set processing state
    modalManager.isProcessing = true;

    // Ensure modal displays correctly
    setTimeout(() => {
      ensureModalDisplay();
    }, 50);

    // Load data ngay lập tức
    loadModalData(event);
  });

  // Add event listener for when modal is fully shown
  modal.addEventListener("shown.bs.modal", function (event) {
    console.log("✅ Modal fully shown - applying z-index fixes");

    // Ensure modal displays correctly after it's fully shown
    ensureModalDisplay();

    // Additional fix with multiple attempts to ensure it works
    setTimeout(() => {
      ensureModalDisplay();
    }, 100);

    setTimeout(() => {
      ensureModalDisplay();
    }, 200);

    setTimeout(() => {
      ensureModalDisplay();
    }, 500);
  });

  // Ensure modal displays correctly
  function ensureModalDisplay() {
    // Force remove any existing backdrops first
    document.querySelectorAll(".modal-backdrop").forEach((el) => el.remove());

    // Wait a bit then ensure proper setup
    setTimeout(() => {
      // Ensure body classes are correct
      document.body.classList.add("modal-open");
      document.body.style.overflow = "hidden";
      document.body.style.paddingRight = "0px";

      // Set extremely high z-index for current modal
      const activeModal = document.querySelector(".modal.show");
      if (activeModal) {
        activeModal.style.zIndex = "999999";
        activeModal.style.position = "fixed";
        activeModal.style.top = "0";
        activeModal.style.left = "0";
        activeModal.style.width = "100%";
        activeModal.style.height = "100%";

        // Also set z-index for modal dialog
        const modalDialog = activeModal.querySelector(".modal-dialog");
        if (modalDialog) {
          modalDialog.style.zIndex = "1000001";
          modalDialog.style.position = "relative";
        }

        // Set z-index for modal content
        const modalContent = activeModal.querySelector(".modal-content");
        if (modalContent) {
          modalContent.style.zIndex = "1000002";
          modalContent.style.position = "relative";
        }
      }

      // Remove backdrop completely
      const backdrop = document.querySelector(".modal-backdrop");
      if (backdrop) {
        backdrop.style.display = "none";
        backdrop.style.opacity = "0";
        backdrop.style.visibility = "hidden";
        backdrop.remove();
      }
    }, 5);
  }

  // Hàm load data cho modal
  function loadModalData(event) {
    const button = event.relatedTarget;
    const requestId = button.getAttribute("data-request-id");
    const serviceName = button.getAttribute("data-service-name");
    const status = button.getAttribute("data-status");

    // Gán giá trị vào form
    document.getElementById("requestIdInput").value = requestId;
    document.getElementById("serviceNameInput").value = serviceName;
    form.setAttribute("data-request-id", requestId);

    const btnContact = modal.querySelector(".btn-contact");
    const btnUpdate = modal.querySelector(".btn-update");
    const btnConfirm = modal.querySelector(".btn-confirm");
    const btnCancel = modal.querySelector(".btn-cancel");

    // Reset form trước và clear validation errors
    form.reset();
    form.querySelectorAll(".is-invalid").forEach((el) => {
      el.classList.remove("is-invalid");
    });
    form.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());
    document.getElementById("serviceNameInput").value = serviceName;

    // Cập nhật state
    modalManager.currentRequestId = requestId;

    console.log(`📊 Loading data for request ${requestId}...`);

    // Hiển thị loading state
    showModalLoading(modal, true);

    // Tạo AbortController để hủy request cũ
    const abortController = new AbortController();

    // Load dữ liệu từ server với cache busting cực mạnh (đặc biệt sau restore)
    const cacheParam = modalManager.isRestoreAction
      ? `t=${Date.now()}&r=${Math.random()}&restore=1&force=${Date.now()}`
      : `t=${Date.now()}&r=${Math.random()}`;
    const fetchUrl = `/admin/service-requests/${requestId}/draft?${cacheParam}`;
    console.log("📡 Fetching fresh data from:", fetchUrl);

    if (modalManager.isRestoreAction) {
      console.log(
        "🔄 Loading data after restore action - using aggressive cache busting"
      );
    }

    const fetchPromise = fetch(fetchUrl, {
      method: "GET",
      headers: {
        "Cache-Control": "no-cache, no-store, must-revalidate",
        Pragma: "no-cache",
        Expires: "0",
        "X-Requested-With": "XMLHttpRequest", // Thêm header để server biết đây là AJAX
      },
      signal: abortController.signal,
    });

    // Lưu promise để có thể abort
    modalManager.loadingPromise = {
      abort: () => abortController.abort(),
    };

    fetchPromise
      .then((res) => res.json())
      .then((data) => {
        console.log("Dữ liệu nhận được:", data);

        // Hiển thị nút dựa trên trạng thái
        if (data.status === "PENDING") {
          // Chưa liên hệ: hiển thị Liên hệ + Xác nhận + Hủy
          btnContact.style.display = "inline-flex";
          btnUpdate.style.display = "none";
          btnConfirm.style.display = "inline-flex";
          btnCancel.style.display = "inline-flex";
        } else if (data.status === "CONTACTED") {
          // Đã liên hệ: hiển thị Cập nhật + Xác nhận + Hủy
          btnContact.style.display = "none";
          btnUpdate.style.display = "inline-flex";
          btnConfirm.style.display = "inline-flex";
          btnCancel.style.display = "inline-flex";

          // Load dữ liệu bản nháp - LUÔN LUÔN load nếu có
          console.log("Loading draft data:", data);

          // Load dữ liệu ngay lập tức
          if (data.quotedPrice !== undefined && data.quotedPrice !== null) {
            form.querySelector("input[name='quotedPrice']").value =
              data.quotedPrice;
          }
          if (data.addressDetail) {
            form.querySelector("input[name='addressDetail']").value =
              data.addressDetail;
          }
          if (data.executionTime) {
            form.querySelector("input[name='executionTime']").value =
              data.executionTime;
          }
          if (data.description) {
            form.querySelector("textarea[name='description']").value =
              data.description;
          }

          // Load district sau khi district select đã sẵn sàng
          if (data.district) {
            let retryCount = 0;
            const maxRetries = 20; // Tối đa 2 giây

            const loadDistrict = () => {
              const districtSelect = form.querySelector(
                "select[name='district']"
              );
              if (districtSelect && districtSelect.options.length > 1) {
                districtSelect.value = data.district;
                console.log("District loaded:", data.district);
              } else if (retryCount < maxRetries) {
                retryCount++;
                setTimeout(loadDistrict, 100);
              } else {
                console.warn(
                  "Could not load district after",
                  maxRetries,
                  "retries"
                );
              }
            };

            // Delay initial load để tránh conflict
            setTimeout(loadDistrict, 200);
          }
        } else {
          // Đã xác nhận hoặc đã hủy: chỉ hiển thị nút đóng
          btnContact.style.display = "none";
          btnUpdate.style.display = "none";
          btnConfirm.style.display = "none";
          btnCancel.style.display = "none";
        }
      })
      .catch((err) => {
        // Chỉ xử lý lỗi nếu không phải do abort
        if (err.name !== "AbortError") {
          console.error("Lỗi load dữ liệu:", err);
          // Fallback: hiển thị nút mặc định cho PENDING
          btnContact.style.display = "inline-flex";
          btnUpdate.style.display = "none";
          btnConfirm.style.display = "inline-flex";
          btnCancel.style.display = "inline-flex";
        }
      })
      .finally(() => {
        // Chỉ tắt loading nếu đây là request hiện tại
        if (modalManager.currentRequestId === requestId) {
          console.log(`✅ Data loaded for request ${requestId}`);
          showModalLoading(modal, false);
          modalManager.isProcessing = false;
          modalManager.loadingPromise = null;
        } else {
          console.log(
            `⚠️ Stale request ${requestId}, current: ${modalManager.currentRequestId}`
          );
        }
      });
  }

  // Reset state khi modal đóng
  modal.addEventListener("hidden.bs.modal", function () {
    console.log("🔒 Modal hidden event triggered");

    // Force cleanup hoàn toàn
    modalManager.forceCleanupModal();

    // Clear validation errors
    const form = document.getElementById("createOrderForm");
    form.querySelectorAll(".is-invalid").forEach((el) => {
      el.classList.remove("is-invalid");
    });
    form.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());

    // Reset form
    form.reset();

    // Tắt loading state
    showModalLoading(modal, false);

    console.log("🧹 Modal cleanup on hidden completed");
  });

  // Hàm cập nhật UI sau khi thực hiện action
  const updateUIAfterAction = (requestId, actionType) => {
    // Tìm button tương ứng với requestId
    const button = document.querySelector(`[data-request-id="${requestId}"]`);
    if (!button) return;

    // Tìm row chứa button này
    const row = button.closest("tr");
    if (!row) return;

    // Tìm badge trạng thái trong row
    const statusBadge = row.querySelector(".badge");
    if (!statusBadge) return;

    // Tìm cell chứa nút thao tác
    const actionCell = button.closest("td");
    if (!actionCell) return;

    // Cập nhật trạng thái dựa trên action
    if (actionType === "contact" || actionType === "update") {
      // Cập nhật thành CONTACTED
      statusBadge.className = "badge bg-primary";
      statusBadge.textContent = "Đã liên hệ";
      button.setAttribute("data-status", "CONTACTED");
    } else if (actionType === "confirm") {
      // Cập nhật thành CONFIRMED
      statusBadge.className = "badge bg-success";
      statusBadge.textContent = "Đã xác nhận";

      // Thay thế nút tạo đơn bằng nút khóa (vô hiệu hóa)
      actionCell.innerHTML = `
              <button
                class="btn btn-secondary btn-action"
                disabled
                title="Đã chốt đơn - không thể thao tác"
              >
                <i class="bi bi-lock"></i>
              </button>
            `;

      // Hiển thị thông báo thành công
      setTimeout(() => {
        showCustomAlert("Đơn hàng đã được tạo thành công!", "success");
      }, 500);
    } else if (actionType === "cancel") {
      // Cập nhật thành CANCELLED
      statusBadge.className = "badge bg-secondary";
      statusBadge.textContent = "Đã hủy";

      // Lấy thông tin dịch vụ từ button hiện tại
      const serviceName = button.getAttribute("data-service-name");

      // Tạo nút khôi phục mới
      const restoreButton = document.createElement("button");
      restoreButton.className = "btn btn-warning btn-action btn-restore";
      restoreButton.setAttribute("data-request-id", requestId);
      restoreButton.setAttribute("data-service-name", serviceName);
      restoreButton.setAttribute("title", "Khôi phục yêu cầu");
      restoreButton.innerHTML = '<i class="bi bi-arrow-clockwise"></i>';

      // Thay thế nút tạo đơn bằng nút khôi phục
      button.parentNode.replaceChild(restoreButton, button);

      // *** QUAN TRỌNG: Reset modal manager sau khi hủy ***
      modalManager.reset();
      console.log("🔄 Modal manager reset after cancel action");
    }
  };

  // Hàm xử lý hành động
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

      // Validate form cho tab yêu cầu
      const requestValidation = validateRequestForm(
        price,
        district,
        address,
        time
      );
      if (!requestValidation.isValid) {
        showCustomAlert(
          "Vui lòng kiểm tra lại thông tin: " +
            requestValidation.errors.join(", "),
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

          // Đánh dấu action hoàn thành và cleanup modal
          modalManager.markActionCompleted();

          // Đợi modal đóng hoàn toàn rồi mới cập nhật UI
          setTimeout(() => {
            // Cập nhật trạng thái trên giao diện
            updateUIAfterAction(id, type);

            // *** XỬ LÝ THÔNG BÁO THÀNH CÔNG ***
            if (type !== "confirm") {
              // Sửa triệt để text thông báo từ server
              let message = data.success;
              if (message) {
                // Thay thế tất cả các biến thể thông báo liên hệ
                if (
                  message.includes("liên hệ") &&
                  (message.includes("lưu") || message.includes("tạm"))
                ) {
                  message = "Đã liên hệ thành công!";
                } else if (message.includes("đã liên hệ")) {
                  message = "Đã liên hệ thành công!";
                }
              }
              showCustomAlert(message, "success");
            }
          }, 200);
        } else if (data.error) {
          console.log(
            `❌ Action ${type} failed for request ${id}: ${data.error}`
          );

          // Đánh dấu action hoàn thành và cleanup modal
          modalManager.markActionCompleted();

          // Hiển thị lỗi sau khi đóng modal
          setTimeout(() => {
            showCustomAlert("Lỗi: " + data.error, "error");
          }, 200);
        }
      })
      .catch((err) => {
        console.error("❌ Network/Exception error:", err);

        // Đánh dấu action hoàn thành và cleanup modal
        modalManager.markActionCompleted();

        // Hiển thị lỗi sau khi đóng modal
        setTimeout(() => {
          showCustomAlert("Có lỗi xảy ra.", "error");
        }, 200);
      });
  };

  // Gắn sự kiện click vào các nút
  modal.querySelector(".btn-contact").addEventListener("click", () => {
    requestAction("contact");
  });

  modal.querySelector(".btn-update").addEventListener("click", () => {
    requestAction("update");
  });

  modal.querySelector(".btn-confirm").addEventListener("click", () => {
    requestAction("confirm");
  });

  modal.querySelector(".btn-cancel").addEventListener("click", () => {
    showCustomConfirm("Bạn có chắc muốn hủy yêu cầu này không?", () => {
      requestAction("cancel");
    });
  });

  // Xử lý nút khôi phục yêu cầu đã hủy
  document.addEventListener("click", function (event) {
    if (event.target.closest(".btn-restore")) {
      const button = event.target.closest(".btn-restore");
      const requestId = button.getAttribute("data-request-id");
      const serviceName = button.getAttribute("data-service-name");

      showCustomConfirm("Bạn có chắc muốn khôi phục yêu cầu này không?", () => {
        console.log("🔄 Starting restore action for request:", requestId);

        fetch(`/admin/service-requests/${requestId}/restore`, {
          method: "POST",
        })
          .then((res) => res.json())
          .then((data) => {
            if (data.success) {
              console.log("✅ Restore successful:", data.success);
              showCustomAlert(data.success, "success");

              // Cập nhật UI: thay đổi trạng thái dựa trên response
              const row = button.closest("tr");
              const statusBadge = row.querySelector(".badge");

              // Xác định trạng thái dựa trên message response
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

              // Tạo nút mới thay thế nút khôi phục
              const newButton = document.createElement("button");
              newButton.className = "btn btn-success btn-action";
              newButton.setAttribute("data-bs-toggle", "modal");
              newButton.setAttribute("data-bs-target", "#createOrderModal");
              newButton.setAttribute("data-request-id", requestId);
              newButton.setAttribute("data-service-name", serviceName);
              newButton.setAttribute("data-status", newStatus);
              newButton.setAttribute("title", "Tạo đơn hàng");
              newButton.innerHTML = '<i class="bi bi-pencil"></i>';

              // Thay thế nút khôi phục bằng nút tạo đơn
              button.parentNode.replaceChild(newButton, button);

              // *** QUAN TRỌNG: Đánh dấu restore action hoàn thành ***
              modalManager.markRestoreCompleted();
              console.log(
                "🎯 Modal manager marked restore completed - modal can now open immediately"
              );
            } else if (data.error) {
              console.log("❌ Restore failed:", data.error);
              showCustomAlert("Lỗi: " + data.error, "error");
            }
          })
          .catch((err) => {
            console.error("❌ Restore error:", err);
            showCustomAlert("Có lỗi xảy ra khi khôi phục yêu cầu.", "error");
          });
      });
    }
  });
});

// ======================================================================================== //

let currentOrderId = null;

// Custom Alert System
function showCustomAlert(message, type = "success") {
  // Remove existing alerts
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

  // Auto remove after 6 seconds (tăng thời gian để thấy rõ hơn)
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
    // Tạo loading overlay
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

    // Disable form elements
    modalBody.style.pointerEvents = "none";
    modalFooter.style.pointerEvents = "none";
  } else {
    // Remove loading overlay
    const loadingOverlay = modal.querySelector(".loading-overlay");
    if (loadingOverlay) {
      loadingOverlay.remove();
    }

    // Enable form elements
    modalBody.style.pointerEvents = "auto";
    modalFooter.style.pointerEvents = "auto";
  }
}

// Custom Confirm System
function showCustomConfirm(message, onConfirm, onCancel = null) {
  // Remove existing confirms
  document
    .querySelectorAll(".custom-confirm")
    .forEach((confirm) => confirm.remove());

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

  // Handle buttons
  confirmDiv.querySelector(".confirm-ok").onclick = () => {
    confirmDiv.remove();
    if (onConfirm) onConfirm();
  };

  confirmDiv.querySelector(".confirm-cancel").onclick = () => {
    confirmDiv.remove();
    if (onCancel) onCancel();
  };

  // Handle backdrop click
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

  // Clear previous validation errors for request form
  const form = document.getElementById("createOrderForm");
  form.querySelectorAll(".is-invalid").forEach((el) => {
    el.classList.remove("is-invalid");
  });
  form.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());

  // Validate quoted price
  const priceStr = price.toString().trim();
  const priceInput = form.querySelector("input[name='quotedPrice']");
  if (!priceStr) {
    addValidationErrorToElement(priceInput, "Vui lòng nhập giá");
    errors.push("Vui lòng nhập giá");
    isValid = false;
  } else {
    // Check for invalid characters (chỉ cho phép số và dấu phẩy, chấm)
    if (!/^[\d,.\s]+$/.test(priceStr)) {
      addValidationErrorToElement(priceInput, "Giá chỉ được chứa số");
      errors.push("Giá chỉ được chứa số");
      isValid = false;
    } else {
      const numericPrice = parseFloat(priceStr.replace(/[,\s]/g, ""));
      if (isNaN(numericPrice) || numericPrice <= 0) {
        addValidationErrorToElement(
          priceInput,
          "Giá phải là số dương lớn hơn 0"
        );
        errors.push("Giá phải là số dương lớn hơn 0");
        isValid = false;
      } else if (numericPrice > 999999999) {
        addValidationErrorToElement(
          priceInput,
          "Giá không được vượt quá 999,999,999 VNĐ"
        );
        errors.push("Giá không được vượt quá 999,999,999 VNĐ");
        isValid = false;
      }
    }
  }

  // Validate district
  const districtSelect = form.querySelector("select[name='district']");
  if (!district || district.trim() === "") {
    addValidationErrorToElement(districtSelect, "Vui lòng chọn quận/huyện");
    errors.push("Vui lòng chọn quận/huyện");
    isValid = false;
  }

  // Validate address
  const addressInput = form.querySelector("input[name='addressDetail']");
  if (!address || address.trim() === "") {
    addValidationErrorToElement(addressInput, "Vui lòng nhập địa chỉ chi tiết");
    errors.push("Vui lòng nhập địa chỉ chi tiết");
    isValid = false;
  } else if (address.trim().length < 5) {
    addValidationErrorToElement(
      addressInput,
      "Địa chỉ chi tiết phải có ít nhất 5 ký tự"
    );
    errors.push("Địa chỉ chi tiết phải có ít nhất 5 ký tự");
    isValid = false;
  } else if (address.trim().length > 255) {
    addValidationErrorToElement(
      addressInput,
      "Địa chỉ chi tiết không được vượt quá 255 ký tự"
    );
    errors.push("Địa chỉ chi tiết không được vượt quá 255 ký tự");
    isValid = false;
  }

  // Validate execution time
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
      addValidationErrorToElement(
        timeInput,
        "Thời gian thực hiện không được là ngày trong quá khứ"
      );
      errors.push("Thời gian thực hiện không được là ngày trong quá khứ");
      isValid = false;
    }

    // Check if date is too far in future (1 year)
    const oneYearFromNow = new Date();
    oneYearFromNow.setFullYear(oneYearFromNow.getFullYear() + 1);
    if (selectedDate > oneYearFromNow) {
      addValidationErrorToElement(
        timeInput,
        "Thời gian thực hiện không được quá 1 năm từ hiện tại"
      );
      errors.push("Thời gian thực hiện không được quá 1 năm từ hiện tại");
      isValid = false;
    }
  }

  return { isValid, errors };
}

function validateOrderForm() {
  let isValid = true;
  const errors = [];

  // Clear previous validation
  document.querySelectorAll(".is-invalid").forEach((el) => {
    el.classList.remove("is-invalid");
  });
  document.querySelectorAll(".invalid-feedback").forEach((el) => el.remove());

  // Validate quoted price
  const priceInput = document.getElementById("modal-quoted-price").value.trim();
  if (!priceInput) {
    addValidationError("modal-quoted-price", "Vui lòng nhập giá");
    errors.push("Chưa nhập giá");
    isValid = false;
  } else {
    // Check for invalid characters (chỉ cho phép số và dấu phẩy, chấm)
    if (!/^[\d,.\s]+$/.test(priceInput)) {
      addValidationError("modal-quoted-price", "Giá chỉ được chứa số");
      errors.push("Giá chứa ký tự không hợp lệ");
      isValid = false;
    } else {
      const numericPrice = parseFloat(priceInput.replace(/[,\s]/g, ""));
      if (isNaN(numericPrice) || numericPrice <= 0) {
        addValidationError(
          "modal-quoted-price",
          "Giá phải là số dương lớn hơn 0"
        );
        errors.push("Giá không hợp lệ");
        isValid = false;
      } else if (numericPrice > 999999999) {
        addValidationError(
          "modal-quoted-price",
          "Giá không được vượt quá 999,999,999 VNĐ"
        );
        errors.push("Giá quá lớn");
        isValid = false;
      }
    }
  }

  // Validate execution date
  const performDate = document.getElementById("modal-perform-date").value;
  if (!performDate) {
    addValidationError("modal-perform-date", "Vui lòng chọn ngày thực hiện");
    errors.push("Chưa chọn ngày thực hiện");
    isValid = false;
  } else {
    const selectedDate = new Date(performDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (selectedDate < today) {
      addValidationError(
        "modal-perform-date",
        "Ngày thực hiện không được là ngày trong quá khứ"
      );
      errors.push("Ngày thực hiện không hợp lệ");
      isValid = false;
    }

    // Check if date is too far in future (1 year)
    const oneYearFromNow = new Date();
    oneYearFromNow.setFullYear(oneYearFromNow.getFullYear() + 1);
    if (selectedDate > oneYearFromNow) {
      addValidationError(
        "modal-perform-date",
        "Ngày thực hiện không được quá 1 năm từ hiện tại"
      );
      errors.push("Ngày thực hiện quá xa");
      isValid = false;
    }
  }

  // Validate province/district
  const province =
    document.getElementById("modal-province-select").style.display !== "none"
      ? document.getElementById("modal-province-select").value
      : document.getElementById("modal-province").value;
  if (!province || province.trim() === "") {
    const targetElement =
      document.getElementById("modal-province-select").style.display !== "none"
        ? "modal-province-select"
        : "modal-province";
    addValidationError(targetElement, "Vui lòng chọn địa điểm");
    errors.push("Chưa chọn địa điểm");
    isValid = false;
  }

  // Validate address detail
  const address = document.getElementById("modal-address").value.trim();
  if (!address) {
    addValidationError("modal-address", "Vui lòng nhập địa chỉ chi tiết");
    errors.push("Chưa nhập địa chỉ chi tiết");
    isValid = false;
  } else if (address.length < 5) {
    addValidationError(
      "modal-address",
      "Địa chỉ chi tiết phải có ít nhất 5 ký tự"
    );
    errors.push("Địa chỉ chi tiết phải có ít nhất 5 ký tự");
    isValid = false;
  } else if (address.length > 255) {
    addValidationError(
      "modal-address",
      "Địa chỉ chi tiết không được vượt quá 255 ký tự"
    );
    errors.push("Địa chỉ quá dài");
    isValid = false;
  }

  // Validate description (optional but if provided, check length)
  const note = document.getElementById("modal-note").value.trim();
  if (note && note.length > 1000) {
    addValidationError("modal-note", "Mô tả không được vượt quá 1000 ký tự");
    errors.push("Mô tả quá dài");
    isValid = false;
  }

  return { isValid, errors };
}

function addValidationError(elementId, message) {
  const element = document.getElementById(elementId);
  addValidationErrorToElement(element, message);
}

function addValidationErrorToElement(element, message) {
  if (!element) return;

  element.classList.add("is-invalid");

  const feedback = document.createElement("div");
  feedback.className = "invalid-feedback";
  feedback.textContent = message;
  element.parentNode.appendChild(feedback);
}

function loadOrderDetail(orderId) {
  currentOrderId = orderId;

  // Reset form và clear validation errors
  $("#modal-id").val("");
  $("#modal-name").val("");
  $("#modal-service").val("");
  $("#modal-request-date").val("");
  $("#modal-perform-date").val("");
  $("#modal-province").val("");
  $("#modal-address").val("");
  $("#modal-note").val("");
  $("#modal-quoted-price").val("");

  // Clear validation errors
  $("#orderDetailsModal").find(".is-invalid").removeClass("is-invalid");
  $("#orderDetailsModal").find(".invalid-feedback").remove();

  // Load dữ liệu
  $.ajax({
    url: `/admin/service-requests/order/${orderId}/detail`,
    method: "GET",
    success: function (data) {
      $("#modal-id").val(data.orderId);
      $("#modal-name").val(data.fullName);
      $("#modal-service").val(data.serviceName);
      $("#modal-request-date").val(data.requestDate.substring(0, 10));
      $("#modal-perform-date").val(data.performDate);
      $("#modal-province").val(data.province);
      $("#modal-address").val(data.address);
      $("#modal-note").val(data.note || "(Không có)");
      $("#modal-quoted-price").val(
        data.quotedPrice.toLocaleString("vi-VN") + " VNĐ"
      );

      // Hiển thị nút dựa trên trạng thái
      if (data.status === "Chưa thanh toán") {
        $("#btn-edit").show();
        $("#btn-payment").show();
        $("#btn-confirm").hide();
        $("#btn-cancel").show();
      } else if (data.status === "Đã thanh toán") {
        $("#btn-edit").show(); // Vẫn cho phép chỉnh sửa khi đã thanh toán
        $("#btn-payment").hide();
        $("#btn-confirm").show();
        $("#btn-cancel").show();
      } else {
        // Đã hoàn tất hoặc đã hủy - chỉ cho phép xem
        $("#btn-edit").hide();
        $("#btn-payment").hide();
        $("#btn-confirm").hide();
        $("#btn-cancel").hide();
      }
    },
    error: function (xhr, status, error) {
      console.error("Lỗi:", error);
      showCustomAlert("Không thể tải dữ liệu đơn hàng.", "error");
    },
    complete: function () {
      // Tắt loading state
      showModalLoading(document.getElementById("orderDetailsModal"), false);
    },
  });
}

function updateOrderStatus(orderId, newStatus) {
  // Cập nhật trạng thái trong bảng
  const row = $(`button[data-id="${orderId}"]`).closest("tr");
  const statusBadge = row.find(".badge");

  // Cập nhật class và text với màu đúng theo thiết kế
  statusBadge.removeClass().addClass("badge");
  if (newStatus === "CANCELLED") {
    statusBadge.addClass("bg-secondary").text("Đã hủy");
  } else if (newStatus === "DONE") {
    statusBadge.addClass("bg-primary").text("Đã hoàn tất"); // Dùng bg-primary (xanh dương) thay vì bg-success (xanh lá)
  } else if (newStatus === "PAID") {
    statusBadge.addClass("bg-success").text("Đã thanh toán"); // Xanh lá cho đã thanh toán
  }
}

$(document).ready(function () {
  // Xử lý khi modal được mở
  $("#orderDetailsModal").on("show.bs.modal", function (event) {
    const button = $(event.relatedTarget);
    const orderId = button.data("id");

    // Hiển thị loading state
    showModalLoading(document.getElementById("orderDetailsModal"), true);

    loadOrderDetail(orderId);
  });

  // Clear validation errors khi đóng modal đơn hàng
  $("#orderDetailsModal").on("hidden.bs.modal", function () {
    // Clear validation errors
    $(this).find(".is-invalid").removeClass("is-invalid");
    $(this).find(".invalid-feedback").remove();
  });

  // Xử lý nút hoàn thành
  $(document).on("click", "#btn-confirm", function () {
    if (!currentOrderId) return;

    showCustomConfirm("Bạn có chắc muốn hoàn thành đơn hàng này không?", () => {
      $.post(`/admin/service-requests/order/${currentOrderId}/complete`)
        .done(function () {
          // Force cleanup tất cả modal và backdrop
          $(".modal").modal("hide");
          $(".modal-backdrop").remove();
          $("body")
            .removeClass("modal-open")
            .css({ overflow: "auto", "padding-right": "" });

          // Delay để cleanup hoàn toàn
          setTimeout(() => {
            showCustomAlert(
              "Đơn hàng đã được hoàn thành thành công!",
              "success"
            );
            updateOrderStatus(currentOrderId, "DONE");
          }, 500);
        })
        .fail(function (xhr, status, error) {
          console.error("Lỗi:", error);
          // Cleanup modal khi có lỗi
          $(".modal").modal("hide");
          $(".modal-backdrop").remove();
          $("body")
            .removeClass("modal-open")
            .css({ overflow: "auto", "padding-right": "" });

          setTimeout(() => {
            showCustomAlert(
              "Lỗi khi hoàn thành đơn hàng: " + (xhr.responseText || error),
              "error"
            );
          }, 300);
        });
    });
  });

  // Xử lý nút hủy
  $(document).on("click", "#btn-cancel", function () {
    if (!currentOrderId) return;

    showCustomConfirm("Bạn có chắc muốn hủy đơn hàng này không?", () => {
      $.post(`/admin/service-requests/order/${currentOrderId}/cancel`)
        .done(function () {
          // Force cleanup tất cả modal và backdrop
          $(".modal").modal("hide");
          $(".modal-backdrop").remove();
          $("body")
            .removeClass("modal-open")
            .css({ overflow: "auto", "padding-right": "" });

          // Delay để cleanup hoàn toàn
          setTimeout(() => {
            showCustomAlert("Đơn hàng đã được hủy thành công!", "success");
            updateOrderStatus(currentOrderId, "CANCELLED");
          }, 500);
        })
        .fail(function (xhr, status, error) {
          console.error("Lỗi:", error);
          // Cleanup modal khi có lỗi
          $(".modal").modal("hide");
          $(".modal-backdrop").remove();
          $("body")
            .removeClass("modal-open")
            .css({ overflow: "auto", "padding-right": "" });

          setTimeout(() => {
            showCustomAlert(
              "Lỗi khi hủy đơn hàng: " + (xhr.responseText || error),
              "error"
            );
          }, 300);
        });
    });
  });

  // Xử lý nút chỉnh sửa đơn hàng
  $(document).on("click", "#btn-edit", function () {
    console.log("Edit button clicked");
    const isReadonly = $("#modal-quoted-price").prop("readonly");
    console.log("Is currently readonly:", isReadonly);

    if (isReadonly) {
      console.log("Switching to edit mode");
      // Chuyển sang chế độ chỉnh sửa
      $("#modal-quoted-price").prop("readonly", false);
      $("#modal-perform-date").prop("readonly", false).attr("type", "date");

      // Chuyển địa điểm từ input sang select
      const currentProvince = $("#modal-province").val();
      $("#modal-province").hide();
      $("#modal-province-select").show();
      $("#modal-province-select").val(currentProvince); // Set giá trị hiện tại

      $("#modal-address").prop("readonly", false);
      $("#modal-note").prop("readonly", false);

      // Xử lý giá tiền - loại bỏ format để có thể edit
      let currentPrice = $("#modal-quoted-price").val();
      if (currentPrice && currentPrice.includes("VNĐ")) {
        let numericPrice = currentPrice.replace(/[^\d]/g, "");
        $("#modal-quoted-price").val(numericPrice);
      }

      console.log("Fields should now be editable");
      console.log(
        "Quoted price readonly:",
        $("#modal-quoted-price").prop("readonly")
      );
      console.log("Province readonly:", $("#modal-province").prop("readonly"));

      // Thay đổi icon và title
      $(this)
        .html('<i class="bi bi-check2"></i>')
        .attr("title", "Lưu thay đổi");
      $(this).removeClass("btn-primary").addClass("btn-success");

      // Ẩn các nút khác
      $("#btn-payment, #btn-confirm, #btn-cancel").hide();

      // Thêm nút hủy chỉnh sửa
      if ($("#btn-cancel-edit").length === 0) {
        $(this).after(
          '<button id="btn-cancel-edit" class="btn btn-secondary btn-action ms-1" title="Hủy chỉnh sửa"><i class="bi bi-x"></i></button>'
        );
      }
    } else {
      // Lưu thay đổi
      const orderId = currentOrderId;
      const quotedPrice = $("#modal-quoted-price").val().replace(/[^\d]/g, "");
      const performDate = $("#modal-perform-date").val();
      const province = $("#modal-province-select").val(); // Lấy từ select
      const address = $("#modal-address").val();
      const note = $("#modal-note").val();

      // Validate form trước khi lưu
      const validation = validateOrderForm();
      if (!validation.isValid) {
        showCustomAlert(
          "Vui lòng kiểm tra lại thông tin: " + validation.errors.join(", "),
          "error"
        );
        return;
      }

      showCustomConfirm("Bạn có chắc muốn lưu các thay đổi không?", () => {
        $.post(`/admin/service-requests/order/${orderId}/update`, {
          quotedPrice: quotedPrice,
          district: province,
          addressDetail: address,
          description: note,
          executionTime: performDate,
        })
          .done(function () {
            showCustomAlert("Đã cập nhật đơn hàng thành công!", "success");

            // Đóng modal trước
            $("#orderDetailsModal").modal("hide");

            // Delay trước khi reload để người dùng thấy thông báo
            setTimeout(() => {
              location.reload();
            }, 1500); // Delay 1.5 giây để thấy thông báo
          })
          .fail(function (xhr, status, error) {
            console.error("Lỗi:", error);
            showCustomAlert(
              "Lỗi khi cập nhật đơn hàng: " + (xhr.responseText || error),
              "error"
            );
          });
      });
    }
  });

  // Xử lý nút hủy chỉnh sửa
  $(document).on("click", "#btn-cancel-edit", function () {
    // Khôi phục chế độ readonly
    $(
      "#modal-quoted-price, #modal-perform-date, #modal-address, #modal-note"
    ).prop("readonly", true);
    $("#modal-perform-date").attr("type", "text"); // Đổi lại thành text

    // Khôi phục địa điểm từ select về input
    $("#modal-province-select").hide();
    $("#modal-province").show();

    // Khôi phục nút chỉnh sửa
    $("#btn-edit")
      .html('<i class="bi bi-pencil"></i>')
      .attr("title", "Chỉnh sửa đơn hàng");
    $("#btn-edit").removeClass("btn-success").addClass("btn-primary");

    // Hiển thị lại các nút khác
    const status = $("#modal-id").data("status") || "Chưa thanh toán";
    if (status === "Chưa thanh toán") {
      $("#btn-payment, #btn-cancel").show();
    } else if (status === "Đã thanh toán") {
      $("#btn-confirm, #btn-cancel").show();
    }

    // Xóa nút hủy chỉnh sửa
    $(this).remove();

    // Reload lại dữ liệu gốc
    loadOrderDetail(currentOrderId);
  });

  // Xử lý nút thanh toán trong modal
  $(document).on("click", "#btn-payment", function () {
    if (!currentOrderId) return;

    showCustomConfirm(
      "Bạn có chắc muốn xác nhận đơn hàng này đã thanh toán không?",
      () => {
        $.post(`/admin/service-requests/order/${currentOrderId}/mark-paid`)
          .done(function () {
            // Force cleanup tất cả modal và backdrop
            $(".modal").modal("hide");
            $(".modal-backdrop").remove();
            $("body")
              .removeClass("modal-open")
              .css({ overflow: "auto", "padding-right": "" });

            // Delay để cleanup hoàn toàn
            setTimeout(() => {
              showCustomAlert(
                "Đơn hàng đã được xác nhận thanh toán thành công!",
                "success"
              );
              updateOrderStatus(currentOrderId, "PAID");

              // Delay trước khi reload để người dùng thấy thông báo
              setTimeout(() => {
                location.reload();
              }, 1500); // Delay 1.5 giây để thấy thông báo
            }, 500);
          })
          .fail(function (xhr, status, error) {
            console.error("Lỗi:", error);
            // Cleanup modal khi có lỗi
            $(".modal").modal("hide");
            $(".modal-backdrop").remove();
            $("body")
              .removeClass("modal-open")
              .css({ overflow: "auto", "padding-right": "" });

            setTimeout(() => {
              showCustomAlert(
                "Lỗi khi xác nhận thanh toán: " + (xhr.responseText || error),
                "error"
              );
            }, 300);
          });
      }
    );
  });
});

document.addEventListener("DOMContentLoaded", function () {
  // Hệ thống quản lý modal backdrop toàn cục
  let modalCount = 0;

  document.querySelectorAll(".modal").forEach((modal) => {
    modal.addEventListener("show.bs.modal", function () {
      modalCount++;
      console.log("Modal opened, count:", modalCount);
    });

    modal.addEventListener("hidden.bs.modal", function () {
      modalCount--;
      console.log("Modal closed, count:", modalCount);

      // Chỉ cleanup khi không còn modal nào mở
      setTimeout(() => {
        if (modalCount <= 0) {
          modalCount = 0;
          // Force cleanup backdrop
          document
            .querySelectorAll(".modal-backdrop")
            .forEach((el) => el.remove());
          document.body.classList.remove("modal-open");
          document.body.style.overflow = "auto";
          document.body.style.paddingRight = "";
        }
      }, 100);
    });
  });

  // Cleanup toàn cục khi cần
  window.forceCleanupModals = function () {
    modalCount = 0;
    document.querySelectorAll(".modal-backdrop").forEach((el) => el.remove());
    document.body.classList.remove("modal-open");
    document.body.style.overflow = "auto";
    document.body.style.paddingRight = "";
  };

  // IMMEDIATE MODAL FIX - Apply as soon as possible
  (function () {
    const forceModalFix = () => {
      // Force apply styles to any existing modals
      document.querySelectorAll(".modal").forEach((modal) => {
        modal.style.zIndex = "999999";
        modal.style.position = "fixed";
      });

      document.querySelectorAll(".modal-backdrop").forEach((backdrop) => {
        backdrop.style.display = "none";
        backdrop.style.opacity = "0";
        backdrop.style.visibility = "hidden";
        backdrop.remove(); // Remove completely
      });

      document.querySelectorAll(".modal-dialog").forEach((dialog) => {
        dialog.style.zIndex = "1000001";
        dialog.style.position = "relative";
      });

      document.querySelectorAll(".modal-content").forEach((content) => {
        content.style.zIndex = "1000002";
        content.style.position = "relative";
      });

      // Force admin layout z-index down
      document.querySelectorAll(".admin-header").forEach((header) => {
        header.style.zIndex = "998";
      });

      document.querySelectorAll(".admin-sidebar").forEach((sidebar) => {
        sidebar.style.zIndex = "997";
      });
    };

    // Apply immediately
    forceModalFix();

    // Apply when DOM is ready
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", forceModalFix);
    }

    // Apply periodically to catch any new modals
    setInterval(forceModalFix, 1000);
  })();

  // Global modal fix - Apply to all modals on the page
  document.addEventListener("DOMContentLoaded", function () {
    // Override Bootstrap modal show method to ensure proper z-index
    const originalShow = bootstrap.Modal.prototype.show;
    bootstrap.Modal.prototype.show = function () {
      // Remove any existing backdrops first
      document.querySelectorAll(".modal-backdrop").forEach((el) => el.remove());

      originalShow.call(this);

      // Apply fixes immediately and with delays
      const applyFixes = () => {
        const modal = this._element;
        if (modal) {
          modal.style.zIndex = "999999";
          modal.style.position = "fixed";
          modal.style.top = "0";
          modal.style.left = "0";
          modal.style.width = "100%";
          modal.style.height = "100%";

          const backdrop = document.querySelector(".modal-backdrop");
          if (backdrop) {
            backdrop.style.display = "none";
            backdrop.style.opacity = "0";
            backdrop.style.visibility = "hidden";
            backdrop.remove();
          }

          const modalDialog = modal.querySelector(".modal-dialog");
          if (modalDialog) {
            modalDialog.style.zIndex = "1000001";
            modalDialog.style.position = "relative";
          }

          const modalContent = modal.querySelector(".modal-content");
          if (modalContent) {
            modalContent.style.zIndex = "1000002";
            modalContent.style.position = "relative";
          }

          // Force body styles
          document.body.style.overflow = "hidden";
          document.body.style.paddingRight = "0px";
        }
      };

      // Apply fixes multiple times to ensure they stick
      applyFixes();
      setTimeout(applyFixes, 10);
      setTimeout(applyFixes, 50);
      setTimeout(applyFixes, 100);
    };

    // Monitor for backdrop issues and fix them immediately
    const observer = new MutationObserver(function (mutations) {
      mutations.forEach(function (mutation) {
        if (mutation.type === "childList") {
          mutation.addedNodes.forEach(function (node) {
            if (
              node.nodeType === 1 &&
              node.classList &&
              node.classList.contains("modal-backdrop")
            ) {
              node.style.display = "none";
              node.style.opacity = "0";
              node.style.visibility = "hidden";
              node.remove(); // Remove immediately
            }
          });
        }
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });

    // Additional global fix for any modal that gets shown
    document.addEventListener("shown.bs.modal", function (e) {
      setTimeout(() => {
        const modal = e.target;
        if (modal) {
          modal.style.zIndex = "999999";
          // Remove any backdrop that might appear
          document.querySelectorAll(".modal-backdrop").forEach((backdrop) => {
            backdrop.style.display = "none";
            backdrop.style.opacity = "0";
            backdrop.style.visibility = "hidden";
            backdrop.remove();
          });
        }
      }, 10);
    });
  });
});
