document.addEventListener("DOMContentLoaded", function () {
  setTimeout(function () {
    let successMessage = document.querySelector(".success");
    if (successMessage) {
      successMessage.style.display = "none";
    }
  }, 2000); // 3 giây
});
// =======================================================================//
function confirmMarkProcessed(element) {
  let markProcessedUrl = element.getAttribute("href"); // lấy từ href của thẻ a
  let confirmBtn = document.getElementById("confirmMarkProcessedBtn");

  confirmBtn.setAttribute("href", markProcessedUrl);

  let markProcessedModal = new bootstrap.Modal(
    document.getElementById("confirmMarkProcessedModal")
  );
  markProcessedModal.show();

  return false; // chặn <a> nhảy link ngay
}
// =======================================================================//
function confirmReview(element) {
  let markProcessedUrl = element.getAttribute("href"); // lấy từ href của thẻ a
  let confirmBtn = document.getElementById("confirmReviewBtn");

  confirmBtn.setAttribute("href", markProcessedUrl);

  let markProcessedModal = new bootstrap.Modal(
    document.getElementById("confirmReviewModal")
  );
  markProcessedModal.show();

  return false; // chặn <a> nhảy link ngay
}



// =======================================================================//


function confirmDelete(element) {
  let deleteUrl = element.getAttribute("data-url"); // Lấy link từ `data-url`
  let confirmBtn = document.getElementById("confirmDeleteBtn");

  // Thay đổi từ href sang onclick để xử lý AJAX
  confirmBtn.removeAttribute("href");
  confirmBtn.onclick = function() {
    performDelete(deleteUrl);
  };

  let deleteModal = new bootstrap.Modal(
    document.getElementById("confirmDeleteModal")
  );
  deleteModal.show();
}

// Hàm thực hiện xóa với AJAX để bắt lỗi
function performDelete(deleteUrl) {
  // Hiển thị loading
  let confirmBtn = document.getElementById("confirmDeleteBtn");
  let originalText = confirmBtn.innerHTML;
  confirmBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Đang xóa...';
  confirmBtn.disabled = true;

  // Chuyển đổi URL từ GET sang POST API (hỗ trợ cả Product và Service)
  let apiUrl = deleteUrl.replace('/Product/delete/', '/Product/api/delete/')
                       .replace('/Service/delete/', '/Service/api/delete/');
  
  // Thực hiện AJAX call
  fetch(apiUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    }
  })
  .then(response => {
    return response.json().then(data => {
      if (response.ok && data.success) {
        // Xóa thành công
        let deleteModal = bootstrap.Modal.getInstance(document.getElementById("confirmDeleteModal"));
        deleteModal.hide();
        
        // Hiển thị thông báo thành công
        showSuccessAlert(data.message);
        
        // Reload trang sau 1 giây
        setTimeout(() => {
          window.location.reload();
        }, 1000);
        
      } else {
        // Có lỗi từ server
        throw new Error(data.message || 'Có lỗi xảy ra khi xóa sản phẩm');
      }
    });
  })
  .catch(error => {
    console.error('Error:', error);
    // Đóng modal xóa
    let deleteModal = bootstrap.Modal.getInstance(document.getElementById("confirmDeleteModal"));
    deleteModal.hide();
    
    // Hiển thị thông báo lỗi thân thiện
    let defaultErrorMessage = deleteUrl.includes('/Product/') 
      ? "Không thể xóa sản phẩm này vì đã có đơn hàng hoặc đang có trong giỏ hàng của khách hàng!"
      : "Không thể xóa dịch vụ này vì đã có yêu cầu dịch vụ được tạo!";
    showErrorAlert(error.message || defaultErrorMessage);
    
    // Reset button
    confirmBtn.innerHTML = originalText;
    confirmBtn.disabled = false;
  });
}

// Hàm hiển thị thông báo lỗi
function showErrorAlert(message) {
  // Tạo alert động
  let alertHtml = `
    <div class="alert alert-danger alert-dismissible fade show" role="alert" style="position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
      <i class="fa fa-exclamation-triangle"></i> ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  `;
  
  // Thêm vào body
  document.body.insertAdjacentHTML('beforeend', alertHtml);
  
  // Tự động ẩn sau 5 giây
  setTimeout(() => {
    let alerts = document.querySelectorAll('.alert-danger');
    if (alerts.length > 0) {
      alerts[alerts.length - 1].remove();
    }
  }, 5000);
}

// Hàm hiển thị thông báo thành công
function showSuccessAlert(message) {
  // Tạo alert động
  let alertHtml = `
    <div class="alert alert-success alert-dismissible fade show" role="alert" style="position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px;">
      <i class="fa fa-check-circle"></i> ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  `;
  
  // Thêm vào body
  document.body.insertAdjacentHTML('beforeend', alertHtml);
  
  // Tự động ẩn sau 3 giây
  setTimeout(() => {
    let alerts = document.querySelectorAll('.alert-success');
    if (alerts.length > 0) {
      alerts[alerts.length - 1].remove();
    }
  }, 3000);
}

// ============================================================================== //

function showToast(id) {
  var toast = document.getElementById(id);
  if (toast) {
    toast.style.transform = "translateX(0)";
    toast.style.opacity = "1";
    setTimeout(function () {
      hideToast(id);
    }, 5000);
  }
}
function hideToast(id) {
  var toast = document.getElementById(id);
  if (toast) {
    toast.style.transform = "translateX(120%)";
    toast.style.opacity = "0";
  }
}
window.addEventListener("DOMContentLoaded", function () {
  if (
    document.getElementById("errorToast") &&
    document
      .getElementById("errorToast")
      .querySelector(".toast-body span")
      .textContent.trim() !== ""
  )
    showToast("errorToast");
  if (
    document.getElementById("successToast") &&
    document
      .getElementById("successToast")
      .querySelector(".toast-body span")
      .textContent.trim() !== ""
  )
    showToast("successToast");
  if (
    document.getElementById("warningToast") &&
    document
      .getElementById("warningToast")
      .querySelector(".toast-body span")
      .textContent.trim() !== ""
  )
    showToast("warningToast");
});
