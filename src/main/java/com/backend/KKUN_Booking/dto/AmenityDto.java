package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.AmenityType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AmenityDto {
    private UUID id;
    private String name;
    private String description;
    private AmenityType amenityType;
    // Getters and Setters
}

