package com.backend.KKUN_Booking.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @JoinColumn( nullable = false)
    private LocalDateTime checkinDate;

    @JoinColumn( nullable = false)
    private LocalDateTime checkoutDate;

    private LocalDateTime createdDate;    // Optional

    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private boolean reviewed;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @JoinColumn( nullable = false)
    private Double totalPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id",    nullable = false)
    private Room room;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Review review;
    // One-to-one relationship with Payment
    @JsonManagedReference // This will be serialized
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
}
