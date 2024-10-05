package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.response.NearbyPlaceResultResponseContainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data // This will generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Default constructor
@AllArgsConstructor // Constructor with parameters
public class NearbyPlaceDto {
    private String name;
    private String category;
    private double distanceInKm;

    private NearbyPlaceResultResponseContainer.NearbyPlaceResultResponse.Tags tags;


    // Getters and setters
}
