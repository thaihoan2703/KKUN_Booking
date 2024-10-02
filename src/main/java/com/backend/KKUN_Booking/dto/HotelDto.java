package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HotelDto {
    private UUID id;
    private String name;
    private String category;
    private Double rating;
    private String location;
    private PaymentPolicy paymentPolicy;
    private List<String> exteriorImages;
    private List<String> roomImages;
    private List<RoomDto> rooms;  // List of room details
    private List<UUID> amenityIds;

    // Getters and Setters
}

