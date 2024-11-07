package com.backend.KKUN_Booking.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime checkinDate;

    @Column(nullable = false)
    private LocalDateTime checkoutDate;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private boolean reviewed;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private String bookingName;
    private String bookingPhone;
    private String bookingEmail;
    private String bookingNotes;

    // Giá phòng cơ bản mỗi đêm
    @Column(name = "base_rate_per_night", nullable = false, columnDefinition = "NUMERIC(38,2) DEFAULT 0")
    private BigDecimal baseRatePerNight;

    // Tỷ lệ giảm giá (vd: 0.1 cho 10%)
    private BigDecimal discount;

    // Tỷ lệ thuế (vd: 0.1 cho 10%)
    private BigDecimal taxRate;

    // Tỷ lệ phí dịch vụ (vd: 0.05 cho 5%)
    private BigDecimal serviceFeeRate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Review review;

    // Quan hệ một-một với Payment
    @JsonManagedReference // This will be serialized
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
}
