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

const printQRBtn = document.getElementById("printQRBtn");
if (printQRBtn) {
  printQRBtn.addEventListener("click", function () {
    const qrImage = document.querySelector('img[alt="QR Code thanh toán"]');
    const orderCode =
      document.querySelector(".info-value")?.textContent.trim() || "";
    const totalAmount =
      document.querySelector(".info-value.text-danger")?.textContent.trim() ||
      "";

    function printQR(qrImageHtml) {
      const printWindow = window.open("", "_blank");
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
                ${qrImageHtml}
                <div class="total">Tổng tiền: ${totalAmount}</div>
                <div class="info">Quét mã QR để thanh toán</div>
                <div class="info"><small>Cảm ơn quý khách!</small></div>
            </div>
        </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.print();
    }

    if (qrImage && qrImage.src && !qrImage.src.startsWith("data:")) {
      fetch(qrImage.src)
        .then((res) => res.blob())
        .then((blob) => {
          const reader = new FileReader();
          reader.onloadend = function () {
            printQR(`<img src="${reader.result}" alt="QR Code">`);
          };
          reader.readAsDataURL(blob);
        });
    } else {
      let qrImageHtml =
        qrImage && qrImage.src
          ? `<img src="${qrImage.src}" alt="QR Code">`
          : "<div style='color:red;'>Không có mã QR</div>";
      printQR(qrImageHtml);
    }
  });
}

const modal = document.getElementById("confirmPaymentModal");
if (modal) {
  modal.addEventListener("show.bs.modal", function () {
    let confirmBtn = document.getElementById("confirmPaymentActionBtn");
    if (confirmBtn) {
      confirmBtn.onclick = function () {
        let orderCode = this.getAttribute("data-ordercode");
        if (!orderCode) {
          alert("Không tìm thấy mã đơn hàng!");
          return;
        }
        fetch("/pos/manual-confirm-payment", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ orderCode: orderCode }),
        })
          .then((res) => res.json())
          .then((data) => {
            if (data.success) {
              var modal = bootstrap.Modal.getInstance(
                document.getElementById("confirmPaymentModal")
              );
              if (modal) {
                modal.hide();
              }
              if (data.redirectUrl) {
                const w = 700,
                  h = 600;
                const left = window.screenX + (window.outerWidth - w) / 2;
                const top = window.screenY + (window.outerHeight - h) / 2;
                window.open(
                  data.redirectUrl,
                  "_blank",
                  `width=${w},height=${h},left=${left},top=${top},resizable=yes,scrollbars=yes`
                );
                window.location.href = "/pos?success=payment_completed";
              } else {
                window.location.href = "/pos?success=payment_completed";
              }
            } else {
              alert("Lỗi: " + data.message);
            }
          })
          .catch((err) => {
            alert("Lỗi kết nối tới máy chủ!");
          });
      };
    }
  });
}

// Xử lý modal hủy đơn hàng
const confirmCancelBtn = document.getElementById("confirmCancelBtn");
if (confirmCancelBtn) {
  confirmCancelBtn.addEventListener("click", function () {
    // Đóng modal
    const cancelModal = bootstrap.Modal.getInstance(
      document.getElementById("cancelOrderModal")
    );
    if (cancelModal) {
      cancelModal.hide();
    }

    // Submit form hủy đơn hàng
    const cancelForm = document.getElementById("cancelOrderForm");
    if (cancelForm) {
      cancelForm.submit();
    }
  });
}
