package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.HotelDto;

import java.util.List;
import java.util.UUID;

public interface HotelService {
    List<HotelDto> getAllHotels();
    HotelDto getHotelById(UUID id);
    HotelDto createHotel(HotelDto hotelDto, String userEmail); // Thêm tham số user
    HotelDto updateHotel(UUID id, HotelDto hotelDto, String userEmail); // Thêm tham số user
    void deleteHotel(UUID id);
}

