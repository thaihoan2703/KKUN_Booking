package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.AmenityService;
import com.backend.KKUN_Booking.service.HotelService;
import com.backend.KKUN_Booking.service.RecommendationService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final UserRepository userRepository;
    private final HotelService hotelService ;
    private final UserService userService;
    private final AmenityService amenityService;

    @Autowired
    public RecommendationServiceImpl(UserRepository userRepository, HotelService hotelService, UserService userService, AmenityService amenityService) {
        this.userRepository = userRepository;
        this.hotelService = hotelService;
        this.userService = userService;
        this.amenityService = amenityService;
    }

    public List<HotelDto> getPersonalizedRecommendations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<HotelDto> allHotels = hotelService.getAllHotels();
        Map<HotelDto, Double> hotelScores = new HashMap<>();

        for (HotelDto hotel : allHotels) {
            double score = calculateHotelScore(user, hotel);
            hotelScores.put(hotel, score);
        }

        return hotelScores.entrySet().stream()
                .sorted(Map.Entry.<HotelDto, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateHotelScore(User user, HotelDto hotelDto) {
        double score = 0.0;

        // Check preferred destinations
        if (user.getPreferredDestinations().contains(hotelDto.getLocation())) {
            score += 2.0;
        }

        // Check preferred amenities (you may need to retrieve actual amenities based on amenityIds)
        List<String> hotelAmenities = amenityService.getAmenitiesByIds(hotelDto.getAmenityIds()); // Assuming you have a service to fetch amenities
        score += user.getPreferredAmenities().stream()
                .filter(amenity -> hotelAmenities.contains(amenity))
                .count() * 0.5;

        // Check travel style
        // if (user.getTravelStyle().equalsIgnoreCase(hotel.getStyle())) {
        //     score += 1.5;
        // }

        // Check recent searches
        List<String> recentSearches = userService.getRecentSearches(user.getId());
        if (recentSearches.stream().anyMatch(search ->
                hotelDto.getName().toLowerCase().contains(search.toLowerCase()) ||
                        hotelDto.getLocation().toLowerCase().contains(search.toLowerCase()))) {
            score += 1.0;
        }

        // Check saved hotels
        List<String> savedHotels = userService.getSavedHotels(user.getId());
        if (savedHotels.contains(hotelDto.getId().toString())) {
            score += 2.0;
        }

        return score;
    }

    public List<HotelDto> getPopularHotels() {
        // This method could be implemented to return a list of generally popular hotels
        // based on overall ratings, number of bookings, etc.
        return hotelService.findTopHotelsByRating(10);
    }

    public List<HotelDto> getTrendingDestinations() {
        // This method could be implemented to return hotels in currently trending destinations
        // based on recent bookings, seasonal popularity, etc.
        return hotelService.findTrendingDestinations(10);
    }
}
