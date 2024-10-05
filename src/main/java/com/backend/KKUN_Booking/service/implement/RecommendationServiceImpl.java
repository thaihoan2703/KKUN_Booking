package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    @Override
    public List<HotelDto> recommendHotelsForUser(UserDto userDto){
        return null;
    }
}
