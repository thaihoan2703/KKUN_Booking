package com.backend.KKUN_Booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class HotelSearchResultDto {
    private UUID id;
    private String name;
    private double lowestPrice;
    private int availableRooms;
    private double popularityScore;
//    private List<NearbyPlaceDto> nearbyPlaces;
}
