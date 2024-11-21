package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.DiscountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PromotionDto {
    private UUID id;
    private String name;
    private String code;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int quantity;
    private int usedCount;
    private double value;
    private BigDecimal maxDiscountValue;
    private DiscountType discountType; // "percent" hoặc "fixed"
    private String applyTo; // "all", "customer", "hotel", "homestay"
    private String description;

    @JsonProperty("isActive")
    private boolean isActive;
}
