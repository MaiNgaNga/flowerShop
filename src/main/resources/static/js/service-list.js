// Initialize AOS
AOS.init({ duration: 1000, once: true });

// Flower Petal Animation
const particleContainer = document.getElementById("flower-particles");
function createPetal() {
  const petal = document.createElement("div");
  petal.classList.add("petal");
  petal.style.left = Math.random() * 100 + "vw";
  petal.style.animationDuration = Math.random() * 5 + 5 + "s";
  petal.style.opacity = Math.random() * 0.4 + 0.3;
  petal.style.setProperty("--drift", Math.random() > 0.5 ? 1 : -1);
  particleContainer.appendChild(petal);
  setTimeout(() => petal.remove(), 10000);
}
setInterval(createPetal, 300);
