document.addEventListener("DOMContentLoaded", function () {
  AOS.init({ duration: 1000, once: true });

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

  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      e.preventDefault();
      document
        .querySelector(this.getAttribute("href"))
        .scrollIntoView({ behavior: "smooth" });
    });
  });

  const form = document.getElementById("contact-form");
  if (form) {
    const submitBtn = form.querySelector(".btn-submit");
    form.addEventListener("submit", function (e) {
      e.preventDefault();
      if (form.checkValidity()) {
        submitBtn.classList.add("loading");
        submitBtn.disabled = true;
        setTimeout(() => {
          submitBtn.classList.remove("loading");
          submitBtn.disabled = false;
          form.reset();
          alert("Yêu cầu của bạn đã được gửi!");
        }, 2000);
      } else {
        form.reportValidity();
      }
    });
  }
});
