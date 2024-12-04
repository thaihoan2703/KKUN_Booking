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
        int threshold = 3; // Ngưỡng khoảng cách Levenshtein

        for (BedType bedType : BedType.values()) {
            // Tính khoảng cách Levenshtein giữa từ khóa và tên hiển thị của từng giá trị enum
            int distance = StringUtils.getLevenshteinDistance(bedType.getDisplayName().toLowerCase(), displayName.toLowerCase());

            // Chỉ chấp nhận kết quả nếu khoảng cách thấp hơn ngưỡng
            if (distance < minDistance && distance <= threshold) {
                minDistance = distance;
                closestMatch = bedType;
            }
        }

        // Nếu không có kết quả gần đúng, throw exception
        if (closestMatch == null) {
            throw new IllegalArgumentException("Unknown display name: " + displayName);
        }

        return closestMatch.toString();
    }

}
