package com.backend.KKUN_Booking.model.enumModel;

import org.apache.commons.lang3.StringUtils;

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

    public static String fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }

        int minDistance = Integer.MAX_VALUE;
        RoomType closestMatch = null;

        for (RoomType roomType : RoomType.values()) {
            // Tính khoảng cách Levenshtein giữa từ khóa và tên hiển thị của từng giá trị enum
            int distance = StringUtils.getLevenshteinDistance(roomType.getDisplayName().toLowerCase(), displayName.toLowerCase());

            // Cập nhật nếu tìm thấy khoảng cách nhỏ hơn
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = roomType;
            }
        }

        // Nếu không tìm thấy giá trị gần đúng hợp lệ
        if (closestMatch == null) {
            throw new IllegalArgumentException("Unknown display name: " + displayName);
        }

        return closestMatch.toString();
    }

}
