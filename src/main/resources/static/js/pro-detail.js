document.addEventListener("DOMContentLoaded", function () {
  const stars = document.querySelectorAll("#starRating span");
  const ratingInput = document.getElementById("ratingInput");
  let selected = 0;

  const reviewForm = document.querySelector(
    'form[th\\:action="@{/detail/comment}"], form[action="/detail/comment"]'
  );
  if (reviewForm) {
    reviewForm.addEventListener("submit", function (e) {
      if (
        !ratingInput.value ||
        isNaN(ratingInput.value) ||
        ratingInput.value < 1 ||
        ratingInput.value > 5
      ) {
        e.preventDefault();
        alert("Vui lòng chọn số sao đánh giá!");
        return false;
      }
      // Nếu muốn gửi bằng AJAX thì xử lý ở đây
    });
  }

  stars.forEach((star, idx) => {
    star.addEventListener("mouseenter", function () {
      stars.forEach((s, i) => {
        s.style.color = i <= idx ? "#ffc107" : "#ddd";
      });
    });
    star.addEventListener("mouseleave", function () {
      stars.forEach((s, i) => {
        s.style.color = i < selected ? "#ffc107" : "#ddd";
      });
    });
    star.addEventListener("click", function () {
      selected = idx + 1;
      ratingInput.value = selected;
      stars.forEach((s, i) => {
        s.style.color = i < selected ? "#ffc107" : "#ddd";
      });
      console.log("Rating đã chọn:", ratingInput.value);
    });
  });
});
