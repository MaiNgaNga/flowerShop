// Chat Float JavaScript
document.addEventListener("DOMContentLoaded", function () {
  // Tạo HTML cho chat float
  const chatFloatHTML = `
        <div class="chat-float-container">
            <div class="chat-icon messenger-icon" onclick="openMessenger()" title="Chat qua Messenger">
                <img src="/images/messenger.png" alt="Messenger" style="width: 32px; height: 32px; object-fit: contain;">
            </div>
            <div class="chat-icon zalo-icon" onclick="openZaloModal()" title="Đặt hoa qua Zalo">
                <img src="/images/zalo.png" alt="Zalo" style="width: 32px; height: 32px; object-fit: contain;">
            </div>
        </div>

        <!-- Zalo QR Modal -->
        <div id="zaloQRModal" class="qr-modal">
            <div class="qr-modal-content">
                <span class="qr-modal-close" onclick="closeZaloModal()">&times;</span>
                <div class="qr-title">
                    <img src="/images/zalo.png" alt="Zalo" style="width: 24px; height: 24px; margin-right: 10px; vertical-align: middle;">
                    Đặt hoa qua Zalo
                </div>
                <div class="qr-subtitle">Quét mã QR để liên hệ đặt hoa tươi</div>
                <div class="qr-code-container">
                    <img src="/images/zalo-qr.png" alt="Zalo QR Code" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                    <div style="display: none; padding: 40px; background: #f0f0f0; color: #666; border-radius: 10px; text-align: center;">
                        QR Code không tải được<br>
                        Vui lòng liên hệ: <strong>0348278722</strong>
                    </div>
                </div>
                <div class="qr-instructions">
                    <strong>🌸 Hướng dẫn đặt hoa:</strong><br>
                    1. Mở ứng dụng <strong>Zalo</strong> trên điện thoại<br>
                    2. Chọn biểu tượng <strong>QR</strong> để quét mã<br>
                    3. Quét mã QR này để kết nối<br>
                    4. Gửi tin nhắn để tư vấn và đặt hoa tươi
                </div>
                <div style="margin-top: 15px; padding: 10px; background: #e8f5e8; border-radius: 8px; font-size: 12px; color: #2d5a2d;">
                    <i class="fas fa-clock"></i> <strong>Giờ hoạt động:</strong> 07:00 - 18:30 | Thứ Hai - Chủ Nhật
                </div>
            </div>
        </div>
    `;

  // Thêm HTML vào body
  document.body.insertAdjacentHTML("beforeend", chatFloatHTML);
});

// Hàm mở Messenger
function openMessenger() {
  // Thay thế bằng link Messenger page của bạn
  // Tìm page ID Facebook của shop hoa để thay thế
  const messengerURL = "https://m.me/61579630839495"; // Thay bằng username page Facebook
  window.open(messengerURL, "_blank");
}

// Hàm mở modal Zalo QR
function openZaloModal() {
  document.getElementById("zaloQRModal").style.display = "flex";
  // Thêm hiệu ứng
  setTimeout(() => {
    document.querySelector(".qr-modal-content").style.transform = "scale(1)";
    document.querySelector(".qr-modal-content").style.opacity = "1";
  }, 10);
}

// Hàm đóng modal Zalo QR
function closeZaloModal() {
  const modal = document.getElementById("zaloQRModal");
  const content = document.querySelector(".qr-modal-content");
  content.style.transform = "scale(0.8)";
  content.style.opacity = "0";
  setTimeout(() => {
    modal.style.display = "none";
  }, 200);
}

// Đóng modal khi click outside
document.addEventListener("click", function (event) {
  const modal = document.getElementById("zaloQRModal");
  if (event.target === modal) {
    closeZaloModal();
  }
});

// Đóng modal khi nhấn ESC
document.addEventListener("keydown", function (event) {
  if (event.key === "Escape") {
    closeZaloModal();
  }
});
