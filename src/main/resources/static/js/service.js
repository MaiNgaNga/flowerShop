document.addEventListener("DOMContentLoaded", function () {
  // Khởi động AOS
  AOS.init({ duration: 1000, once: true });

  // Tạo hiệu ứng hoa bay
  const particleContainer = document.getElementById("flower-particles");
  function createFlower() {
    const flower = document.createElement("div");
    flower.classList.add("flower");
    flower.style.left = Math.random() * 100 + "vw";
    flower.style.animationDuration = Math.random() * 5 + 5 + "s";
    flower.style.opacity = Math.random() * 0.5 + 0.3;
    particleContainer.appendChild(flower);
    setTimeout(() => flower.remove(), 10000);
  }
  setInterval(createFlower, 500);

  // Cuộn mượt khi click anchor
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      e.preventDefault();
      document
        .querySelector(this.getAttribute("href"))
        .scrollIntoView({ behavior: "smooth" });
    });
  });

  // Xử lý nút submit loading
  const form = document.getElementById("contact-form");
  if (form) {
    const submitBtn = form.querySelector(".btn-submit");
    form.addEventListener("submit", function () {
      submitBtn.disabled = true;
      submitBtn.innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Đang gửi...
      `;
    });
  }

  // Tự động ẩn alert sau 5 giây
  const alert = document.getElementById("successAlert");
  if (alert) {
    setTimeout(() => {
      alert.classList.remove("show");
      alert.classList.add("fade");
    }, 5000);
  }
});
