package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.model.enumModel.DiscountType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private int quantity;
    private int usedCount;

    @Column(nullable = false)
    private double value;
    private BigDecimal maxDiscountValue;
    @Column(nullable = false)
    private DiscountType discountType; // "percent" hoặc "fixed"

    private String applyTo; // "all", "customer", "hotel", "homestay"

    @Column(length = 1000)
    private String description;

    private boolean isActive;

    // Getters và Setters
}
