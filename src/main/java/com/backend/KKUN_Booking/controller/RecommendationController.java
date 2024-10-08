package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.service.RecommendationService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/personalized/{userId}")
    public ResponseEntity<List<HotelDto>> getPersonalizedRecommendations(@PathVariable UUID userId) {
        List<HotelDto> recommendations = recommendationService.getPersonalizedRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<HotelDto>> getPopularHotels() {
        List<HotelDto> popularHotels = recommendationService.getPopularHotels();
        return ResponseEntity.ok(popularHotels);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<HotelDto>> getTrendingDestinations() {
        List<HotelDto> trendingDestinations = recommendationService.getTrendingDestinations();
        return ResponseEntity.ok(trendingDestinations);
    }
}