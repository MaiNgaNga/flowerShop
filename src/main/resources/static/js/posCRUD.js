
// // Chặn submit nếu cả hai trường ngày đều rỗng, hiện toast cảnh báo
// function validateDateFilter(e) {
//   var from = document.getElementById('fromDate').value;
//   var to = document.getElementById('toDate').value;
//   if (!from && !to) {
//     e.preventDefault();
//     showToast('Vui lòng chọn ít nhất một ngày để lọc!', 'warning');
//     return false;
//   }
//   return true;
// }

// // Hàm show toast Bootstrap (cần có toast ở layout)
// function showToast(message, type) {
//   var toast = document.getElementById('customToast');
//   if (!toast) {
//     toast = document.createElement('div');
//     toast.id = 'customToast';
//     toast.className = 'toast align-items-center text-bg-' + (type || 'warning') + ' border-0 position-fixed top-0 start-50 translate-middle-x mt-3';
//     toast.style.zIndex = 9999;
//     toast.style.minWidth = '300px';
//     toast.innerHTML = '<div class="d-flex"><div class="toast-body">' + message + '</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>';
//     document.body.appendChild(toast);
//   } else {
//     toast.querySelector('.toast-body').innerText = message;
//     toast.className = 'toast align-items-center text-bg-' + (type || 'warning') + ' border-0 position-fixed top-0 start-50 translate-middle-x mt-3';
//   }
//   var bsToast = bootstrap.Toast.getOrCreateInstance(toast, { delay: 2500 });
//   bsToast.show();
// }

// document.addEventListener('DOMContentLoaded', function() {
//   var form = document.querySelector('form');
//   if (form) {
//     form.addEventListener('submit', validateDateFilter);
//   }
// });
