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
  // Đặt lại tất cả select về option đầu tiên (Tất cả)
  form.querySelectorAll("select").forEach((sel) => (sel.selectedIndex = 0));
  // Xóa trắng ô tìm kiếm
  document.getElementById("filterKeyword").value = "";
  // Submit lại form
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
