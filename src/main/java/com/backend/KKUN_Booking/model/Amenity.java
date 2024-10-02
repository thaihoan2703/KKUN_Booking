package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.model.enumModel.AmenityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private AmenityType amenityType;
    // Getters and Setters
}

