// Giảm giá
function toggleDiscountFields() {
  const checkbox = document.getElementById("applyDiscount");
  const discountSection = document.getElementById("discountFields");

  if (checkbox.checked) {
    discountSection.classList.remove("d-none");
    discountSection.style.display = "block";
    
    // Đặt required cho các trường khi checkbox được tick
    document.getElementById("discountPercent").setAttribute("required", "required");
    document.getElementById("discountStart").setAttribute("required", "required");
    document.getElementById("discountEnd").setAttribute("required", "required");
  } else {
    discountSection.style.display = "none";
    
    // Xóa required và clear values
    document.getElementById("discountPercent").removeAttribute("required");
    document.getElementById("discountStart").removeAttribute("required");
    document.getElementById("discountEnd").removeAttribute("required");
    
    document.getElementById("discountPercent").value = "";
    document.getElementById("discountStart").value = "";
    document.getElementById("discountEnd").value = "";
  }
}

// Không cần frontend validation nữa vì sẽ dùng backend validation hiển thị lỗi dưới field
function validateDiscountForm() {
  return true; // Luôn cho phép submit để backend validation xử lý
}

window.addEventListener("DOMContentLoaded", () => {
  const checkbox = document.getElementById("applyDiscount");
  toggleDiscountFields();

  // Kiểm tra xem có lỗi validation nào cho các trường giảm giá không
  const discountPercentError = document.querySelector("i.error[th\\:errors='*{discountPercent}']");
  const discountStartError = document.querySelector("i.error[th\\:errors='*{discountStart}']");
  const discountEndError = document.querySelector("i.error[th\\:errors='*{discountEnd}']");
  
  const hasDiscountError = (discountPercentError && discountPercentError.textContent.trim()) ||
                          (discountStartError && discountStartError.textContent.trim()) ||
                          (discountEndError && discountEndError.textContent.trim());

  // Nếu có lỗi validation hoặc có giá trị trong các trường giảm giá, hiển thị section
  if (hasDiscountError || checkbox.checked) {
    checkbox.checked = true;
    toggleDiscountFields();
  }
});

// Tự động chuyển sang tab tương ứng
const params = new URLSearchParams(window.location.search);
const activeTab = params.get("tab");
if (activeTab) {
  const triggerEl = document.querySelector(`#${activeTab}-tab`);
  if (triggerEl) {
    new bootstrap.Tab(triggerEl).show();
  }
}
