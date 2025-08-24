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

  confirmBtn.setAttribute("href", deleteUrl); // Cập nhật link cho nút Xóa trong modal

  let deleteModal = new bootstrap.Modal(
    document.getElementById("confirmDeleteModal")
  );
  deleteModal.show();
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
