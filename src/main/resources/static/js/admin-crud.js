// Giảm giá
function toggleDiscountFields() {
  const checkbox = document.getElementById("applyDiscount");
  const discountSection = document.getElementById("discountFields");

  if (checkbox.checked) {
    discountSection.classList.remove("d-none");
    discountSection.style.display = "block";
  } else {
    discountSection.style.display = "none";
    document.getElementById("discountPercent").value = "";
    document.getElementById("discountStart").value = "";
    document.getElementById("discountEnd").value = "";
  }
}

window.addEventListener("DOMContentLoaded", () => {
  const checkbox = document.getElementById("applyDiscount");
  toggleDiscountFields();

  const hasError =
    document.querySelector(".error[th\\:errors='*{discountPercent}']") ||
    document.querySelector(".error[th\\:errors='*{discountStart}']") ||
    document.querySelector(".error[th\\:errors='*{discountEnd}']");

  if (hasError) {
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
