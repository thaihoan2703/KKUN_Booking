package com.backend.KKUN_Booking.model.enumModel;

import org.apache.commons.lang3.StringUtils;

public enum BedType {
    SINGLE("Giường đơn"),
    DOUBLE("Giường đôi"),
    QUEEN("Giường queen size"),
    KING("Giường king size"),
    TWIN("Giường đôi nhỏ"),
    BUNK("Giường tầng"),
    SOFA_BED("Sofa giường"),
    FUTON("Futon (đệm sàn)"),
    MURPHY("Giường gấp Murphy"),
    ROLL_AWAY("Giường di động");

    private final String displayName;

    // Constructor
    BedType(String displayName) {
        this.displayName = displayName;
    }

    // Getter for displayName
    public String getDisplayName() {
        return displayName;
    }


    // Phương thức chuyển đổi từ displayName sang BedType
    public static String fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }

        int minDistance = Integer.MAX_VALUE;
        BedType closestMatch = null;

        for (BedType bedType : BedType.values()) {
            // Tính khoảng cách Levenshtein giữa từ khóa và tên hiển thị của từng giá trị enum
            int distance = StringUtils.getLevenshteinDistance(bedType.getDisplayName().toLowerCase(), displayName.toLowerCase());

            // Cập nhật nếu tìm thấy khoảng cách nhỏ hơn
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = bedType;
            }
        }

        // Nếu không tìm thấy giá trị gần đúng hợp lệ
        if (closestMatch == null) {
            throw new IllegalArgumentException("Unknown display name: " + displayName);
        }

        return closestMatch.toString();
    }

}
