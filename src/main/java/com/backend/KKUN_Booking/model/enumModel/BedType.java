package com.backend.KKUN_Booking.model.enumModel;

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

    // Override toString() to return the displayName
    @Override
    public String toString() {
        return displayName;
    }
}
