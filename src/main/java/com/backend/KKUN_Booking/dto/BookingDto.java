package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.Payment;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private UUID id;
    private LocalDateTime checkinDate;
    private LocalDateTime checkoutDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private String bookingName;
    private String bookingPhone;
    private String bookingEmail;
    private String bookingNotes;

    private BookingStatus status;
    private boolean reviewed;

    private BigDecimal baseRatePerNight;   // Giá cơ bản mỗi đêm cho phòng
    private BigDecimal discount;           // Tỷ lệ giảm giá (vd: 0.1 cho 10%)
    private BigDecimal taxRate;            // Tỷ lệ thuế (vd: 0.1 cho 10%)
    private BigDecimal serviceFeeRate;     // Tỷ lệ phí dịch vụ (vd: 0.05 cho 5%)
    private BigDecimal totalPrice;         // Tổng giá sau khi tính toán
    private PaymentType paymentType;       // Phương thức thanh toán

    private UUID userId;                   // Khóa ngoại đến User
    private UUID roomId;                   // Khóa ngoại đến Room
    private UUID promotionId;
    private Payment payment;               // Chi tiết thanh toán (khóa ngoại đến Payment)
}
