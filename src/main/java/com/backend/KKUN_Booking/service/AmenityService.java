package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.AmenityDto;

import java.util.List;
import java.util.UUID;

public interface AmenityService {
    List<AmenityDto> getAllAmenities();
    List<String> getAmenitiesByIds(List<UUID> amenityIds);
    AmenityDto getAmenityById(UUID id);
    AmenityDto createAmenity(AmenityDto amenityDto);
    AmenityDto updateAmenity(UUID id, AmenityDto amenityDto);
    void deleteAmenity(UUID id);
}
