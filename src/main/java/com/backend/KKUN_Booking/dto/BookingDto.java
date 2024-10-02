package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.Payment;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BookingDto {
    private UUID id;
    private LocalDateTime checkinDate;
    private LocalDateTime checkoutDate;
    private LocalDateTime createdDate;    // Add if needed
    private LocalDateTime updatedDate;
    private BookingStatus status;
    private boolean reviewed;

    private Double totalPrice;
    private PaymentType paymentType;
    private UUID userId;                  // Foreign key to User
    private UUID roomId;                  // Foreign key to Room
    private Payment payment;               // Foreign key to Payment

}
