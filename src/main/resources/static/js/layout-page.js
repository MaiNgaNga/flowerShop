document.addEventListener("DOMContentLoaded", function () {
  setTimeout(function () {
    let successMessage = document.querySelector(".success");
    if (successMessage) {
      successMessage.style.display = "none";
    }
  }, 2000); // 3 giây
});
function confirmDelete(element) {
  let deleteUrl = element.getAttribute("data-url"); // Lấy link từ `data-url`
  let confirmBtn = document.getElementById("confirmDeleteBtn");

  confirmBtn.setAttribute("href", deleteUrl); // Cập nhật link cho nút Xóa trong modal

  let deleteModal = new bootstrap.Modal(
    document.getElementById("confirmDeleteModal")
  );
  deleteModal.show();
}
