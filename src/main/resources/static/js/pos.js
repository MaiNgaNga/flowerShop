// Xử lý mở bill ở cửa sổ mới khi thanh toán tiền mặt
document.addEventListener("DOMContentLoaded", function () {
  const posForm = document.getElementById("posForm");
  if (posForm) {
    posForm.addEventListener("submit", function (e) {
      const paymentMethod = posForm.querySelector(
        'select[name="paymentMethod"]'
      ).value;
      if (paymentMethod === "cash") {
        e.preventDefault();
        const formData = new FormData(posForm);
        fetch(posForm.action, {
          method: "POST",
          body: formData,
        })
          .then((response) => {
            if (
              response.redirected &&
              response.url.includes("/pos/bill?orderCode=")
            ) {
              const { url } = response;
              const w = 700,
                h = 600;
              const left = window.screenX + (window.outerWidth - w) / 2;
              const top = window.screenY + (window.outerHeight - h) / 2;
              window.open(
                url,
                "_blank",
                `width=${w},height=${h},left=${left},top=${top},resizable=yes,scrollbars=yes`
              );
              window.location.href = "/pos?success=payment_completed";
            } else {
              window.location.href = response.url;
            }
          })
          .catch(() => {
            alert("Có lỗi khi xác nhận bán hàng!");
          });
      }
    });
  }
});
function setColorFilter(color) {
  document.getElementById("colorInput").value = color;
  document.getElementById("filterForm").submit();
}
function setTypeFilter(type) {
  document.getElementById("typeInput").value = type;
  document.getElementById("filterForm").submit();
}
function setPriceFilter(min, max) {
  document.getElementById("minPriceInput").value = min;
  document.getElementById("maxPriceInput").value = max;
  document.getElementById("filterForm").submit();
}
function addToCart(btn) {
  const productId = btn.getAttribute("data-id");
  fetch("/pos/cart/add", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: "productId=" + productId,
  })
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        alert("Có lỗi khi thêm vào giỏ hàng!");
        console.error(cart);
      }
    })
    .catch((err) => {
      alert("Có lỗi khi thêm vào giỏ hàng: " + err.message);
      console.error(err);
    });
}

function renderCart(cart) {
  const cartRows = document.getElementById("cartRows");
  cartRows.innerHTML = "";
  let total = 0;
  if (Array.isArray(cart)) {
    cart.forEach((item) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${item.name}</td>
        <td>${item.quantity}</td>
        <td>${(item.price * item.quantity).toLocaleString()} VND</td>
        <td><button type="button" onclick="removeFromCart(${
          item.productId
        })" class="btn btn-link p-0" title="Xóa" style="text-decoration: none;"><span style="color: red; font-size: 18px; text-decoration: none;">&#10006;</span></button></td>
      `;
      cartRows.appendChild(row);
      total += item.price * item.quantity;
    });
  } else {
    console.error("cart không phải là mảng:", cart);
  }
  document.getElementById("totalAmount").innerText = total.toLocaleString();
}

function removeFromCart(productId) {
  fetch("/pos/cart/remove", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: "productId=" + productId,
  })
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        alert("Có lỗi khi xóa sản phẩm!");
        console.error(cart);
      }
    })
    .catch((err) => {
      alert("Có lỗi khi xóa sản phẩm: " + err.message);
      console.error(err);
    });
}

function resetFilterForm() {
  const form = document.getElementById("filterForm");
  form.reset();
  form.querySelectorAll("select").forEach((sel) => (sel.selectedIndex = 0));
  document.getElementById("filterKeyword").value = "";
  form.submit();
}

window.onload = function () {
  fetch("/pos/cart")
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text);
        });
      }
      return res.json();
    })
    .then((cart) => {
      if (Array.isArray(cart)) {
        renderCart(cart);
      } else {
        console.error("cart không phải là mảng:", cart);
      }
    })
    .catch((err) => {
      console.error("Lỗi khi lấy giỏ hàng:", err);
    });
};
