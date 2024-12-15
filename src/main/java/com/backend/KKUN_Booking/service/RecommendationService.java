package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.User;

import java.util.List;
import java.util.UUID;

public interface RecommendationService {
    List<HotelDto> getPersonalizedRecommendations(UUID userId);
    List<HotelDto> getTopRatingHotels();
    List<HotelDto> getTrendingDestinations();
    List<HotelDto> getAvailableHotelsForToday();
}
