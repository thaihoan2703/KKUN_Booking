package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private UUID id;
    private Double amount;                // Amount paid or to be paid
    private PaymentStatus status; // Status of the payment
    private PaymentType paymentType; // Type of payment
    private LocalDateTime paymentDate;        // Date of payment
    private UUID bookingId;               // Foreign key to the associated Booking
    private String IpAddress;
    private String transactionReference;
}

