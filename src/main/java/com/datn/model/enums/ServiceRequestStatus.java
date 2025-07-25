package com.datn.model.enums;

public enum ServiceRequestStatus {
    PENDING("Đang chờ"),
    CONTACTED("Đã liên hệ"),
    CONFIRMED("Đã xác nhận"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ServiceRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
