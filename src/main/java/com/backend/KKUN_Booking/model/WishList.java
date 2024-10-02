package com.backend.KKUN_Booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wish_list")
public class WishList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Liên kết với người dùng

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; // Liên kết với phòng

    // Getter và Setter
}
