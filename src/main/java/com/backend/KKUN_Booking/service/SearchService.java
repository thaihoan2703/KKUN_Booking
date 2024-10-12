package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.HotelSearchResultDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchService {
    List<HotelSearchResultDto> searchHotels(String location, LocalDateTime checkIn, LocalDateTime checkOut, int guests);
}
