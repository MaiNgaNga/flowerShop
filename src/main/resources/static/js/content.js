(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    const adminLayout = document.getElementById("adminLayout");
    const themeToggle = document.getElementById("adminThemeToggle");
    const themeIcon = document.getElementById("adminThemeIcon");
    const sidebar = document.getElementById("adminSidebar");
    const sidebarToggle = document.getElementById("adminSidebarToggle");
    const header = document.getElementById("adminHeader");
    const mainContent = document.getElementById("adminMainContent");

    // Theme toggle
    const savedTheme = localStorage.getItem("admin-theme") || "light";
    adminLayout.setAttribute("data-theme", savedTheme);
    themeIcon.className = savedTheme === "dark" ? "fas fa-sun" : "fas fa-moon";

    themeToggle.addEventListener("click", () => {
      const currentTheme = adminLayout.getAttribute("data-theme");
      const newTheme = currentTheme === "dark" ? "light" : "dark";
      adminLayout.setAttribute("data-theme", newTheme);
      localStorage.setItem("admin-theme", newTheme);
      themeIcon.className = newTheme === "dark" ? "fas fa-sun" : "fas fa-moon";
    });

    // Sidebar toggle - giữ trạng thái theo localStorage
    const savedSidebarState = localStorage.getItem("admin-sidebar-collapsed");
    // Nếu chưa có setting thì mặc định là expanded (false)
    const shouldCollapse = savedSidebarState === "true";
    if (shouldCollapse) {
      sidebar.classList.add("collapsed");
      header.classList.add("collapsed");
      mainContent.classList.add("collapsed");
    } else {
      // Đảm bảo remove class collapsed nếu có
      sidebar.classList.remove("collapsed");
      header.classList.remove("collapsed");
      mainContent.classList.remove("collapsed");
      if (savedSidebarState === null) {
        localStorage.setItem("admin-sidebar-collapsed", "false");
      }
    }

    sidebarToggle.addEventListener("click", () => {
      const isCollapsed = sidebar.classList.contains("collapsed");
      if (isCollapsed) {
        sidebar.classList.remove("collapsed");
        header.classList.remove("collapsed");
        mainContent.classList.remove("collapsed");
        localStorage.setItem("admin-sidebar-collapsed", "false");
      } else {
        // Đóng tất cả nav groups
        const navGroups = document.querySelectorAll(".admin-nav-group");
        navGroups.forEach((group) => {
          group.classList.remove("expanded");
        });

        sidebar.classList.add("collapsed");
        header.classList.add("collapsed");
        mainContent.classList.add("collapsed");
        localStorage.setItem("admin-sidebar-collapsed", "true");
      }
    });

    // Navigation groups
    const navGroups = document.querySelectorAll(".admin-nav-group");
    navGroups.forEach((group) => {
      const header = group.querySelector(".admin-nav-group-header");
      header.addEventListener("click", () => {
        // Đóng các groups khác
        navGroups.forEach((otherGroup) => {
          if (otherGroup !== group) {
            otherGroup.classList.remove("expanded");
          }
        });

        // Toggle group hiện tại
        group.classList.toggle("expanded");
      });
    });

    // Set active nav item - chỉ mở 1 nhóm duy nhất
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll(".admin-nav-item");
    let activeGroupFound = false;

    navItems.forEach((item) => {
      if (item.getAttribute("href") === currentPath && !activeGroupFound) {
        item.classList.add("active");
        const parentGroup = item.closest(".admin-nav-group");
        if (parentGroup) {
          // Đóng tất cả các nhóm khác trước
          navGroups.forEach((group) => group.classList.remove("expanded"));
          // Mở nhóm hiện tại
          parentGroup.classList.add("expanded");
          activeGroupFound = true;
        }
      }
    });

    // Nếu không tìm thấy trang nào khớp, mặc định mở Tổng quan
    if (!activeGroupFound) {
      const overviewGroup = document.querySelector(".admin-nav-group");
      if (overviewGroup) {
        navGroups.forEach((group) => group.classList.remove("expanded"));
        overviewGroup.classList.add("expanded");
      }
    }
  });
})();
