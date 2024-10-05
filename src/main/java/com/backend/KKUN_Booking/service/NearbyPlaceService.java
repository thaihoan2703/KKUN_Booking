package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.NearbyPlaceDto;

import java.util.List;

public interface NearbyPlaceService {
    List<NearbyPlaceDto> findNearbyNotablePlaces(String address);
}
