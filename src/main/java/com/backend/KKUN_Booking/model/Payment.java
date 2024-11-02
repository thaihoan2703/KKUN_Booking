package com.backend.KKUN_Booking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // Status of the payment (e.g., PENDING, COMPLETED)

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PaymentType paymentType; // Type of payment (e.g., CREDIT_CARD, ELECTRONIC_PAYMENT)

    private LocalDateTime paymentDate;

    // One-to-one relationship with Booking
    @OneToOne
    @JsonBackReference // This will not be serialized
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(unique = true, nullable = true)
    private String transactionReference;
}
