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

  // Handle best seller filter buttons
  const filterBtns = document.querySelectorAll(".filter-btn");
  const productGrid = document.querySelector("#best-seller-grid");

  filterBtns.forEach((btn) => {
    btn.addEventListener("click", async function () {
      // Remove active class from all buttons
      filterBtns.forEach((button) => button.classList.remove("active"));
      // Add active class to clicked button
      this.classList.add("active");

      const type = this.dataset.type || "default";

      try {
        // Call API to get products by type
        const response = await fetch(`/api/best-seller?type=${type}`);
        const products = await response.json();

        // Update product grid
        updateProductGrid(products);
      } catch (error) {
        console.error("Error fetching products:", error);
      }
    });
  });

  function updateProductGrid(products) {
    if (!productGrid) return;

    productGrid.innerHTML = "";

    products.forEach((product, index) => {
      const productCard = createProductCard(product, index);
      productGrid.appendChild(productCard);
    });
  }

  function createProductCard(product, index) {
    const productCard = document.createElement("div");
    productCard.className = "product-card";
    productCard.setAttribute("data-aos", "fade-in");
    productCard.setAttribute("data-aos-delay", 300 + index * 100);

    const imageUrl =
      product.image_url && product.image_url !== ""
        ? `/images/${product.image_url}`
        : "/images/logo.png";

    const priceHTML =
      product.discountPercent && product.discountPercent > 0
        ? `<div class="product-price">
           <span>${formatPrice(product.priceAfterDiscount)}₫</span>
           <span style="text-decoration: line-through; color: #999">${formatPrice(
             product.price
           )}₫</span>
         </div>`
        : `<div class="product-price">${formatPrice(product.price)}₫</div>`;

    // Nếu có id thì bọc ảnh trong thẻ <a>, nếu không thì chỉ hiển thị ảnh
    let imageBlock = "";
    if (product.id !== undefined && product.id !== null) {
      imageBlock = `
        <a href="/detail?id=${product.id}" class="text-decoration-none">
          <img src="${imageUrl}" class="default-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
          <img src="${imageUrl}" class="hover-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
        </a>
      `;
    } else {
      imageBlock = `
        <img src="${imageUrl}" class="default-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
        <img src="${imageUrl}" class="hover-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
        <span style="color:red;">ID NULL</span>
      `;
    }

    productCard.innerHTML = `
      <div class="product-image">
        ${imageBlock}
        <div class="cart-icon-wrapper">
          <div class="cart-icon"><i class="fas fa-shopping-cart"></i></div>
          <div class="add-to-cart-text">Add To Cart</div>
        </div>
      </div>
      <div class="product-info">
        <div class="product-name">${product.name}</div>
        ${priceHTML}
        <div class="product-size">Số lượng: ${product.quantity}</div>
        <div class="product-size">${
          product.color ? product.color.name : "N/A"
        }</div>
      </div>
    `;

    return productCard;
  }

  function formatPrice(price) {
    return new Intl.NumberFormat("vi-VN").format(price);
  }
});
