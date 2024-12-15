package com.backend.KKUN_Booking.model.enumModel;

public enum PaymentPolicy {
    ONLINE("Thanh toán trực tuyến"),
    CHECKOUT("Thanh toán khi trả phòng"),
    ONLINE_CHECKOUT("Cả hai phương thức");

    private final String displayName;

    PaymentPolicy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}