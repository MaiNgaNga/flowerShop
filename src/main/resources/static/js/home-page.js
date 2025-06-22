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

  const slides = document.querySelectorAll(".carousel-images img");
  let index = 0;

  function showSlide(i) {
    slides.forEach((img, idx) => {
      img.classList.remove("active");
      if (idx === i) img.classList.add("active");
    });
  }

  setInterval(() => {
    index = (index + 1) % slides.length;
    showSlide(index);
  }, 4000); // 4 giây mỗi ảnh

  const swiper = new Swiper(".post-slider", {
    slidesPerView: 1,
    spaceBetween: 30,
    loop: true,
    centeredSlides: true,
    autoplay: {
      delay: 3000,
      disableOnInteraction: false,
    },
    breakpoints: {
      768: {
        slidesPerView: 2,
      },
      1024: {
        slidesPerView: 3,
      },
    },
  });
});
