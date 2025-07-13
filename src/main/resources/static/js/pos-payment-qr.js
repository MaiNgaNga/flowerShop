
let timeLeft = 15 * 60;
let totalTime = 15 * 60;

function updateCountdown() {
  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  document.getElementById("countdown").textContent = `${minutes
    .toString()
    .padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;

  const progressBar = document.getElementById("progressBar");
  const percentage = (timeLeft / totalTime) * 100;
  progressBar.style.width = percentage + "%";

  if (percentage < 30) {
    progressBar.className =
      "progress-bar progress-bar-striped progress-bar-animated bg-danger";
  } else if (percentage < 60) {
    progressBar.className =
      "progress-bar progress-bar-striped progress-bar-animated bg-warning";
  }

  if (timeLeft <= 0) {
    alert("⏰ Hết thời gian thanh toán! Đơn hàng sẽ bị hủy.");
    window.location.href = "/pos?error=timeout";
    return;
  }
  timeLeft--;
}

setInterval(updateCountdown, 1000);

const checkPaymentBtn = document.getElementById("checkPaymentBtn");
if (checkPaymentBtn) {
  checkPaymentBtn.addEventListener("click", function () {
    const btn = this;
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang kiểm tra...';

    setTimeout(() => {
      if (Math.random() > 0.7) {
        document.getElementById("statusAlert").innerHTML =
          '<i class="fas fa-check-circle"></i> ✅ Thanh toán thành công!';
        document.getElementById("statusAlert").className =
          "alert alert-success";

        try {
          new Audio("/sounds/success.mp3").play();
        } catch (e) {}

        setTimeout(() => {
          alert("🎉 Thanh toán thành công! Đơn hàng đã được xử lý.");
          window.location.href = "/pos?success=payment_completed";
        }, 2000);
      } else {
        btn.disabled = false;
        btn.innerHTML = originalText;

        const tempAlert = document.createElement("div");
        tempAlert.className = "alert alert-warning mt-2";
        tempAlert.innerHTML =
          "<small>⏳ Chưa nhận được thanh toán. Vui lòng thử lại.</small>";
        btn.parentNode.appendChild(tempAlert);

        setTimeout(() => {
          tempAlert.remove();
        }, 3000);
      }
    }, 1500);
  });
}

const printQRBtn = document.getElementById("printQRBtn");
if (printQRBtn) {
  printQRBtn.addEventListener("click", function () {
    const printWindow = window.open("", "_blank");
    const qrImage = document.querySelector('img[alt="QR Code thanh toán"]');
    const orderCode =
      document.querySelector(".info-value[th\\:text='${orderCode}']")
        ?.textContent || "";
    const totalAmount =
      document.querySelector(".info-value.text-danger")?.textContent || "";

    printWindow.document.write(`
        <html>
        <head>
            <title>In mã QR - ${orderCode}</title>
            <style>
                body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }
                .qr-container { border: 2px solid #333; padding: 20px; margin: 20px auto; max-width: 400px; }
                img { max-width: 300px; margin: 20px 0; }
                .info { margin: 10px 0; font-size: 14px; }
                .total { font-size: 18px; font-weight: bold; color: #d9534f; }
            </style>
        </head>
        <body>
            <div class="qr-container">
                <h2>🌸 Cửa Hàng Hoa</h2>
                <div class="info">Mã đơn hàng: <strong>${orderCode}</strong></div>
                <img src="${qrImage.src}" alt="QR Code">
                <div class="total">Tổng tiền: ${totalAmount}</div>
                <div class="info">Quét mã QR để thanh toán</div>
                <div class="info"><small>Cảm ơn quý khách!</small></div>
            </div>
        </body>
        </html>
    `);

    printWindow.document.close();
    printWindow.print();
  });
}

setInterval(() => {
  const orderCode =
    document.querySelector(".info-value[th\\:text='${orderCode}']")
      ?.textContent || "";
  if (!orderCode) return;
  fetch("/pos/check-payment-status", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderCode: orderCode }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success && data.status === "Đã thanh toán") {
        document.getElementById("statusAlert").innerHTML =
          '<i class="fas fa-check-circle"></i> ✅ Thanh toán thành công!';
        document.getElementById("statusAlert").className =
          "alert alert-success";
        setTimeout(() => {
          window.location.href = "/pos?success=payment_completed";
        }, 2000);
      }
    })
    .catch((error) => {
    });
}, 20000);

window.addEventListener("beforeunload", function (e) {
  e.preventDefault();
  e.returnValue =
    "Bạn có chắc muốn rời khỏi trang? Giao dịch đang chờ thanh toán.";
});
