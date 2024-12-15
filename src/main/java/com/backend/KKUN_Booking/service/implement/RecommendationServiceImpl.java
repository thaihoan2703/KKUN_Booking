package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.AmenityDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final UserRepository userRepository;
    private final HotelService hotelService;
    private final UserService userService;
    private final AmenityService amenityService;

    @Autowired
    public RecommendationServiceImpl(UserRepository userRepository, HotelService hotelService,
                                     UserService userService, AmenityService amenityService) {
        this.userRepository = userRepository;
        this.hotelService = hotelService;
        this.userService = userService;
        this.amenityService = amenityService;
    }

    public List<HotelDto> getPersonalizedRecommendations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime checkInDate = LocalDateTime.now(); // Today's date
        List<HotelDto> availableHotels = hotelService.getHotelsWithAvailableRooms(checkInDate);
        Map<HotelDto, Double> hotelScores = new HashMap<>();

        for (HotelDto hotel : availableHotels) {
            double score = calculateHotelScore(user, hotel);
            hotelScores.put(hotel, score);
        }

        // Sort the hotels by their score in descending order
        return hotelScores.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // Sort descending by value
                .limit(10) // Limit to the top 10
                .map(Map.Entry::getKey) // Map the result back to HotelDto
                .collect(Collectors.toList());
    }

    private double calculateHotelScore(User user, HotelDto hotelDto) {
        double score = 0.0;

        // Check preferred destinations
        if (user.getPreferredDestinations().stream()
                .anyMatch(destination -> hotelDto.getLocation().toLowerCase().contains(destination.toLowerCase()) ||
                        destination.toLowerCase().contains(hotelDto.getLocation().toLowerCase()))) {
            score += 2.0;
        }

        // Check preferred amenities using the new AmenityDto structure
        List<String> hotelAmenityNames = hotelDto.getAmenities().stream()
                .map(AmenityDto::getName)
                .collect(Collectors.toList());

        score += user.getPreferredAmenities().stream()
                .filter(preferredAmenity -> hotelAmenityNames.contains(preferredAmenity))
                .count() * 0.5;

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

        // Bonus points for available rooms today
        score += 1.5;

        return score;
    }

    public List<HotelDto> getAvailableHotelsForToday() {
        LocalDateTime checkInDate = LocalDateTime.now(); // Today's date
        return hotelService.getHotelsWithAvailableRooms(checkInDate)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<HotelDto> getTopRatingHotels() {
        // Filter top-rated hotels with available rooms for today
        LocalDateTime checkInDate = LocalDateTime.now();
        return hotelService.findTopHotelsByRating(10).stream()
                .filter(hotel -> hotelService.checkRoomAvailability(hotel.getId(), checkInDate))
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<HotelDto> getTrendingDestinations() {
        // Get trending destinations with available rooms for today
        LocalDateTime checkInDate = LocalDateTime.now();
        return hotelService.findTrendingDestinations(10).stream()
                .filter(hotel -> {
                    boolean isAvailable = hotelService.checkRoomAvailability(hotel.getId(), checkInDate);
                    System.out.println("Room availability for " + hotel.getName() + ": " + isAvailable);  // Log kết quả availability
                    return isAvailable;
                })
                .limit(10)
                .collect(Collectors.toList());
    }
}