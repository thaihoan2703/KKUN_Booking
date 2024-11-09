package com.backend.KKUN_Booking.model.enumModel;

public enum BlogPostCategory {
    ADVENTURE("Phiêu lưu"),
    CITY_TOUR("Tham quan thành phố"),
    ROAD_TRIP("Hành trình đường dài"),
    TOURISM("Du lịch"),
    WILDLIFE("Động vật hoang dã"),
    NATURE_EXCURSION("Khám phá thiên nhiên"),
    PHOTOGRAPHY("Nhiếp ảnh"),
    CRUISE("Du thuyền"),
    CULTURAL("Văn hóa"),
    FOOD("Ẩm thực"),
    HISTORY("Lịch sử"),
    FESTIVAL("Lễ hội"),
    SHOPPING("Mua sắm"),
    SEA("Biển");

    private final String displayName;

    BlogPostCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}

