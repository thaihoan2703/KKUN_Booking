package com.backend.KKUN_Booking.model.enumModel;

public enum BlogContentType {
    PARAGRAPH("Đoạn văn"),
    IMAGE("Hình ảnh"),
    QUOTE("Trích dẫn");

    private final String displayName;

    BlogContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
