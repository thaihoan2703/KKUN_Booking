package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.RoomType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoomDto {
    private UUID id;
    private RoomType type;
    private int capacity;
    private double basePrice;
    private boolean available;
    private UUID hotelId;   // Foreign key to hotel
    private List<String> roomImages;
    private List<UUID> amenityIds;
    // Getters and Setters
}
