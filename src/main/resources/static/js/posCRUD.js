// Chặn submit nếu cả hai trường ngày đều rỗng, hiện toast cảnh báo
function validateDateFilter(e) {
  let from = document.getElementById("fromDate").value;
  let to = document.getElementById("toDate").value;
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

// Toast server-side (hiệu ứng trượt cho warningToast nếu có)
function showServerToast() {
  let toast = document.getElementById("warningToast");
  if (!toast) return;
  toast.style.transform = "translateX(0)";
  toast.style.opacity = 1;
  setTimeout(function () {
    toast.style.transform = "translateX(120%)";
    toast.style.opacity = 0;
  }, 2500);
}

document.addEventListener("DOMContentLoaded", function () {
  let form = document.querySelector("form");
  if (form) {
    form.addEventListener("submit", validateDateFilter);
  }
});
