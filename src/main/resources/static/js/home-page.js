document.addEventListener("DOMContentLoaded", function () {
  // Hàm cập nhật số lượng giỏ hàng trên header (dùng chung với header.html)
  function updateCartCount() {
    fetch("/cart/count")
      .then((response) => response.json())
      .then((count) => {
        const cartCountSpan = document.getElementById("cart-count");
        if (cartCountSpan) cartCountSpan.textContent = count;
      });
  }
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

  // Gắn sự kiện cho form thêm giỏ hàng ở lần load đầu (render Thymeleaf), chỉ gắn 1 lần cho cả best-seller và phụ kiện
  const cartFormsInit = document.querySelectorAll(
    '#best-seller-grid form[action="/cart/add"], .accessory-section .product-grid form[action="/cart/add"]'
  );
  cartFormsInit.forEach((form) => {
    if (!form.dataset.listener) {
      form.addEventListener("submit", async function (e) {
        e.preventDefault();
        const formData = new FormData(form);
        try {
          const response = await fetch("/cart/add", {
            method: "POST",
            body: formData,
          });
          if (response.ok) {
            updateCartCount(); // Cập nhật số lượng trên icon header
            const modal = new bootstrap.Modal(
              document.getElementById("cartSuccessModal")
            );
            modal.show();
            document.getElementById("continueShoppingBtn").onclick =
              function () {
                modal.hide();
              };
            document.getElementById("viewCartBtn").onclick = function () {
              window.location.href = "/cart";
            };
          } else {
            alert("Có lỗi khi thêm vào giỏ hàng!");
          }
        } catch (err) {
          alert("Có lỗi khi thêm vào giỏ hàng!");
        }
      });
      form.dataset.listener = "true";
    }
  });

  function updateProductGrid(products) {
    if (!productGrid) return;

    productGrid.innerHTML = "";

    products.forEach((product, index) => {
      const productCard = createProductCard(product, index);
      productGrid.appendChild(productCard);
    });

    // Gắn lại sự kiện cho form thêm giỏ hàng (sau khi render lại), chỉ gắn 1 lần
    const cartForms = productGrid.querySelectorAll('form[action="/cart/add"]');
    cartForms.forEach((form) => {
      if (!form.dataset.listener) {
        form.addEventListener("submit", async function (e) {
          e.preventDefault();
          const formData = new FormData(form);
          try {
            const response = await fetch("/cart/add", {
              method: "POST",
              body: formData,
            });
            if (response.ok) {
              const modal = new bootstrap.Modal(
                document.getElementById("cartSuccessModal")
              );
              modal.show();
              document.getElementById("continueShoppingBtn").onclick =
                function () {
                  modal.hide();
                };
              document.getElementById("viewCartBtn").onclick = function () {
                window.location.href = "/cart";
              };
            } else {
              alert("Có lỗi khi thêm vào giỏ hàng!");
            }
          } catch (err) {
            alert("Có lỗi khi thêm vào giỏ hàng!");
          }
        });
        form.dataset.listener = "true";
      }
    });
  }

  // Gắn sự kiện cho form thêm giỏ hàng ở lần load đầu (render Thymeleaf), chỉ gắn 1 lần
  document.addEventListener("DOMContentLoaded", function () {
    const cartFormsInit = document.querySelectorAll(
      '#best-seller-grid form[action="/cart/add"]'
    );
    cartFormsInit.forEach((form) => {
      if (!form.dataset.listener) {
        form.addEventListener("submit", async function (e) {
          e.preventDefault();
          const formData = new FormData(form);
          try {
            const response = await fetch("/cart/add", {
              method: "POST",
              body: formData,
            });
            if (response.ok) {
              const modal = new bootstrap.Modal(
                document.getElementById("cartSuccessModal")
              );
              modal.show();
              document.getElementById("continueShoppingBtn").onclick =
                function () {
                  modal.hide();
                };
              document.getElementById("viewCartBtn").onclick = function () {
                window.location.href = "/cart";
              };
            } else {
              alert("Có lỗi khi thêm vào giỏ hàng!");
            }
          } catch (err) {
            alert("Có lỗi khi thêm vào giỏ hàng!");
          }
        });
        form.dataset.listener = "true";
      }
    });
  });

  function createProductCard(product, index) {
    const productCard = document.createElement("div");
    productCard.className = "product-card";
    productCard.setAttribute("data-aos", "fade-in");
    productCard.setAttribute("data-aos-delay", 300 + index * 100);

    const imageUrl =
      product.image_url && product.image_url !== ""
        ? `/images/${product.image_url}`
        : "/images/logo.png";
    const imageUrl2 =
      product.image_url2 && product.image_url2 !== ""
        ? `/images/${product.image_url2}`
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
          <img src="${imageUrl2}" class="hover-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
        </a>
      `;
    } else {
      imageBlock = `
        <img src="${imageUrl}" class="default-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
        <img src="${imageUrl2}" class="hover-img" alt="${product.name}" onerror="this.src='/images/logo.png'"/>
        <span style="color:red;">ID NULL</span>
      `;
    }

    productCard.innerHTML = `
      <div class="product-image">
        ${imageBlock}
        <form action="/cart/add" method="post" style="display: inline">
          <input type="hidden" name="productId" value="${product.id}" />
          <input type="hidden" name="quantity" value="1" />
          <button type="submit" class="cart-icon-wrapper" style="border: none; background: none; padding: 0">
            <div class="cart-icon"><i class="fas fa-shopping-cart"></i></div>
            <div class="add-to-cart-text">Add To Cart</div>
          </button>
        </form>
      </div>
      <div class="product-info">
        <div class="product-name">${product.name}</div>
        ${priceHTML}
        
      </div>
    `;

    return productCard;
  }

  function formatPrice(price) {
    return new Intl.NumberFormat("vi-VN").format(price);
  }

  // Hiển thị/ẩn popup chatbot
  document.getElementById("chatbot-toggle").onclick = function () {
    document.getElementById("chatbot-popup").style.display = "flex";
    this.style.display = "none";
  };
  document.getElementById("chatbot-close").onclick = function () {
    document.getElementById("chatbot-popup").style.display = "none";
    document.getElementById("chatbot-toggle").style.display = "flex";
  };

  // Hàm gửi tin nhắn và gọi API
  async function sendChatbotMessage() {
    const input = document.getElementById("chatbot-input");
    const text = input.value.trim();
    if (!text) return;
    const body = document.getElementById("chatbot-body");

    // Hiển thị tin nhắn người dùng
    const userMsg = document.createElement("div");
    userMsg.className = "chatbot-message user";
    userMsg.innerText = text;
    body.appendChild(userMsg);
    input.value = "";
    body.scrollTop = body.scrollHeight;

    // Hiển thị trạng thái đang trả lời
    const botMsg = document.createElement("div");
    botMsg.className = "chatbot-message bot";
    botMsg.innerText = "Đang trả lời...";
    body.appendChild(botMsg);
    body.scrollTop = body.scrollHeight;

    // Gọi API
    try {
      const response = await fetch("http://localhost:3000/api/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ question: text }),
      });
      if (!response.ok) throw new Error("Lỗi server");
      const data = await response.json();
      botMsg.innerText = data.answer || "Xin lỗi, tôi chưa có câu trả lời.";
    } catch (e) {
      botMsg.innerText = "Có lỗi khi kết nối đến máy chủ.";
    }
  }

  // Gửi tin nhắn khi nhấn Enter
  document
    .getElementById("chatbot-input")
    .addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        e.preventDefault();
        sendChatbotMessage();
      }
    });
  document.getElementById("chatbot-send").onclick = sendChatbotMessage;
});
