package com.backend.KKUN_Booking.dto;

import java.math.BigDecimal;

public class PriceCalculationResult {
    private BigDecimal totalPrice;      // Tổng giá sau khi áp dụng giảm giá, thuế, phí
    private BigDecimal discountAmount; // Giá trị giảm giá thực sự đã áp dụng
    private BigDecimal taxAmount;      // Giá trị thuế
    private BigDecimal serviceFee;     // Giá trị phí dịch vụ

    public PriceCalculationResult(BigDecimal totalPrice, BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal serviceFee) {
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.serviceFee = serviceFee;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }
}

