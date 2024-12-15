package com.backend.KKUN_Booking.model.enumModel;

public enum DiscountType {
    PERCENT("Phần trăm"),
    FIXED("Giá");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
