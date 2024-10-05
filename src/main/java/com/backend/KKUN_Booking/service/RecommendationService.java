package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;

import java.util.List;

public interface RecommendationService {
    List<HotelDto> recommendHotelsForUser(UserDto userDto);
}
