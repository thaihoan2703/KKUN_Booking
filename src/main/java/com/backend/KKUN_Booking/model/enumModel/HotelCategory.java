package com.backend.KKUN_Booking.model.enumModel;

public enum HotelCategory {
    KHACH_SAN_5_SAO("Khách sạn 5 sao"),
    KHACH_SAN_4_SAO("Khách sạn 4 sao"),
    KHACH_SAN_3_SAO("Khách sạn 3 sao"),
    KHACH_SAN("Khách sạn"),
    RESORT("Resort"),
    HOME_STAY("Home stay"),
    NHA_NGHI("Nhà nghỉ");

    private final String displayName;

    HotelCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
