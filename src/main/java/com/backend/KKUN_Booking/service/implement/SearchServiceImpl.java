package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.HotelSearchResultDto;
import com.backend.KKUN_Booking.dto.NearbyPlaceDto;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.RoomRepository;
import com.backend.KKUN_Booking.service.NearbyPlaceService;
import com.backend.KKUN_Booking.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Autowired
    private NearbyPlaceService nearbyPlaceService;

    @Autowired
    public SearchServiceImpl(HotelRepository hotelRepository, RoomRepository roomRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<HotelSearchResultDto> searchHotels(String location, LocalDateTime checkInDate, LocalDateTime checkOutDate, int guests) {
        // Convert location to lowercase to handle case-insensitivity
        List<Hotel> hotels = hotelRepository.findByLocationContainingIgnoreCase(location);

        return hotels.stream()
                .map(hotel -> {
                    List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelAndDateRange(hotel.getId(), checkInDate, checkOutDate);
                    double lowestPrice = availableRooms.stream()
                            .mapToDouble(Room::getBasePrice)
                            .min()
                            .orElse(Double.MAX_VALUE);

                    boolean canAccommodateGuests = availableRooms.stream()
                            .anyMatch(room -> room.getCapacity() >= guests);

                    if (!availableRooms.isEmpty() && canAccommodateGuests) {
                        List<NearbyPlaceDto> nearbyPlaces = getNearbyPlaces(hotel);
                        return new HotelSearchResultDto(
                                hotel.getId(),
                                hotel.getName(),
                                lowestPrice,
                                availableRooms.size(),
                                calculatePopularityScore(hotel),
                                nearbyPlaces
                        );
                    } else {
                        return null;
                    }
                })
                .filter(result -> result != null)
                .sorted((r1, r2) -> Double.compare(r2.getPopularityScore(), r1.getPopularityScore()))
                .collect(Collectors.toList());
    }


    private List<NearbyPlaceDto> getNearbyPlaces(Hotel hotel) {
        // Assuming hotel has latitude and longitude fields
        return nearbyPlaceService.findNearbyNotablePlaces(hotel.getLocation()); // 5km radius
    }

    private double calculatePopularityScore(Hotel hotel) {
        List<Room> rooms = hotel.getRooms();

        // If there are no rooms, return a base score of 0
        if (rooms.isEmpty()) {
            return 0.0;
        }

        // Total rating and review count across all rooms
        double totalRating = 0.0;
        int totalReviewCount = 0;

        // Loop through each room and aggregate the reviews' ratings
        for (Room room : rooms) {
            List<RoomReview> reviews = room.getReviews(); // Assuming Room has a `getReviews()` method

            // If there are reviews, calculate the total rating for the room
            for (RoomReview review : reviews) {
                totalRating += review.getOverallRating(); // Assuming Review has `getOverallRating()` method
            }

            // Count the number of reviews for this room
            totalReviewCount += reviews.size();
        }

        // If no reviews, return a base score of 0
        if (totalReviewCount == 0) {
            return 0.0;
        }

        // Calculate the average rating across all rooms
        double averageRating = totalRating / totalReviewCount;

        // Scale the score to a range of 0 to 10, assuming reviews are on a 5-point scale
        double popularityScore = averageRating * (10.0 / 5.0); // Adjust scale from 5 to 10

        // Cap the score at 10
        return Math.min(popularityScore, 10.0);
    }
}