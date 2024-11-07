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

    // Phương thức để lấy displayName của amenityType
    public String getAmenityTypeDisplayName() {
        return amenityType != null ? amenityType.getDisplayName() : null;
    }
}

