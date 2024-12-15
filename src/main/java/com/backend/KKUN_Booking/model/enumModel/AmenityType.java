package com.backend.KKUN_Booking.model.enumModel;

public enum AmenityType {
    BASIC("Cơ bản"),
    ENTERTAINMENT("Giải trí"),
    COMFORT("Tiện nghi"),
    KITCHEN("Bếp"),
    SECURITY("An ninh"),
    SPA("Spa"),
    POOL("Hồ bơi"),
    GYM("Phòng tập"),
    BUSINESS("Doanh nhân"),
    PARKING("Bãi đỗ xe"),
    PET_FRIENDLY("Thân thiện với vật nuôi"),
    TRANSPORT("Dịch vụ vận chuyển"),
    DINING("Ăn uống"),
    LAUNDRY("Dịch vụ giặt là"),
    WIFI("Wi-Fi");

    private final String displayName;

    AmenityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

