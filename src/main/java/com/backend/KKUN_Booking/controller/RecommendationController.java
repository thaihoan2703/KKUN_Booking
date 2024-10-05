package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.service.RecommendationService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private UserService userService;
    @GetMapping("/user/{userId}")
    public List<HotelDto> getRecommendations(@PathVariable UUID userId) {
        UserDto userDto = userService.getUserById(userId);
        return recommendationService.recommendHotelsForUser(userDto);
    }
}
