package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.Hotel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class HotelSearchResultDto {
    private HotelDto hotelDto;
    private double lowestPrice;
    private int availableRooms;
    private double popularityScore;

    private RoomDto mostBookedRoom;
}
