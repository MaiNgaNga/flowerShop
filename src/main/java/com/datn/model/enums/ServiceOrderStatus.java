package com.datn.model.enums;

public enum ServiceOrderStatus {
    UNPAID("Chưa thanh toán", "warning"),
    PAID("Đã thanh toán", "success"),
    DONE("Đã hoàn tất", "primary"),
    CANCELLED("Đã hủy", "secondary");

    private final String displayName;
    private final String badgeColor;

    ServiceOrderStatus(String displayName, String badgeColor) {
        this.displayName = displayName;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeColor() {
        return badgeColor;
    }
}
