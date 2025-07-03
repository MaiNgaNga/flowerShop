// Initialize AOS
AOS.init({ duration: 1000, once: true });

// Flower Petal Animation
const particleContainer = document.getElementById("flower-particles");
function createPetal() {
  const petal = document.createElement("div");
  petal.classList.add("petal");
  petal.style.left = Math.random() * 100 + "vw";
  petal.style.animationDuration = Math.random() * 8 + 7 + "s";
  petal.style.opacity = Math.random() * 0.4 + 0.3;
  petal.style.setProperty("--drift", Math.random() > 0.5 ? 1 : -1);
  particleContainer.appendChild(petal);
  setTimeout(() => petal.remove(), 15000);
}
setInterval(createPetal, 300);

// Image Viewer and Zoom Functionality
const mainImage = document.getElementById("main-image");
const thumbnails = document.querySelectorAll(".thumbnail");
const lightbox = document.getElementById("lightbox");
const lightboxImage = document.getElementById("lightbox-image");
const lightboxClose = document.getElementById("lightbox-close");

// Thumbnail click to change main image
thumbnails.forEach((thumbnail) => {
  thumbnail.addEventListener("click", () => {
    thumbnails.forEach((thumb) => thumb.classList.remove("active"));
    thumbnail.classList.add("active");
    mainImage.src = thumbnail.dataset.image;
  });
});

// Zoom effect centered at cursor
const imageViewer = document.querySelector(".image-viewer");
const zoomFactor = 2;

imageViewer.addEventListener("mousemove", (e) => {
  const rect = mainImage.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;

  const centerX = (x / rect.width) * 100;
  const centerY = (y / rect.height) * 100;

  mainImage.style.transformOrigin = `${centerX}% ${centerY}%`;
  mainImage.style.transform = `scale(${zoomFactor})`;
});

imageViewer.addEventListener("mouseenter", () => {
  mainImage.style.transform = `scale(${zoomFactor})`;
});

imageViewer.addEventListener("mouseleave", () => {
  mainImage.style.transform = `scale(1)`;
});

// Lightbox Zoom Functionality
mainImage.addEventListener("click", () => {
  lightboxImage.src = mainImage.src;
  lightbox.classList.add("active");
});

lightboxClose.addEventListener("click", () => {
  lightbox.classList.remove("active");
});

lightbox.addEventListener("click", (e) => {
  if (e.target === lightbox) {
    lightbox.classList.remove("active");
  }
});

// Smooth Scroll for Anchor Links
document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
  anchor.addEventListener("click", function (e) {
    e.preventDefault();
    document.querySelector(this.getAttribute("href")).scrollIntoView({
      behavior: "smooth",
    });
  });
});
