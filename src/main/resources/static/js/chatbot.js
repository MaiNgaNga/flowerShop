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
