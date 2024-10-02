package com.backend.KKUN_Booking.model.enumModel;

public enum PaymentType {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Bank Transfer"),
    CASH("Cash"),
    VNPAY("VNPay"),
    MOMO("MoMo"),
    ZALOPAY("ZaloPay"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    CRYPTOCURRENCY("Cryptocurrency"),
    GIFT_CARD("Gift Card"),
    LOYALTY_POINTS("Loyalty Points"),
    POC("Cash on Delivery"),
    INSTALLMENT("Installment");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

