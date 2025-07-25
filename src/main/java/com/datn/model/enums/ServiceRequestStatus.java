package com.datn.model.enums;

public enum ServiceRequestStatus {
    PENDING("Đang chờ", "warning text-dark"),
    CONTACTED("Đã liên hệ", "primary"),
    CONFIRMED("Đã xác nhận", "success"),
    CANCELLED("Đã hủy", "secondary");

    private final String displayName;
    private final String badgeColor;

    ServiceRequestStatus(String displayName, String badgeColor) {
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
