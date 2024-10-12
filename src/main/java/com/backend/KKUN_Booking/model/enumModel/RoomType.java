package com.backend.KKUN_Booking.model.enumModel;

public enum RoomType {
    PHONG_DOI("Phòng đôi"),
    PHONG_DON("Phòng đơn"),
    PHONG_VIP("Phòng VIP"),
    PHONG_GIA_DINH("Phòng gia đình"),
    PHONG_SUITE("Phòng Suite"),
    PHONG_TAP_THE("Phòng tập thể"),
    PHONG_THUONG("Phòng thường");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
