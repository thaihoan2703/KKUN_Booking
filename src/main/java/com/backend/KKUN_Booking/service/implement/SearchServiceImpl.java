package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.*;
import com.backend.KKUN_Booking.model.*;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.RoomRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.NearbyPlaceService;
import com.backend.KKUN_Booking.service.SearchService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Autowired
    private NearbyPlaceService nearbyPlaceService;

    @Autowired
    private UserService userService;

    @Autowired
    public SearchServiceImpl(HotelRepository hotelRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }


    @Override
    public List<HotelSearchResultDto> searchHotels(String location, LocalDateTime checkInDate, LocalDateTime checkOutDate, int guests) {
        String searchLocation = location.toLowerCase();
        String[] locationKeywords = searchLocation.split(",");

        List<Hotel> hotels = hotelRepository.findAll().stream()
                .filter(hotel -> {
                    String hotelLocation = hotel.getLocation().toLowerCase();
                    return Arrays.stream(locationKeywords)
                            .anyMatch(keyword -> hotelLocation.contains(keyword.trim()));
                })
                .collect(Collectors.toList());

        // Handle user authentication and preferred destinations
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String userEmail = authentication.getName();
            Optional<User> optionalUser = userRepository.findByEmail(userEmail);

            optionalUser.ifPresent(currentUser -> {
                if (!currentUser.getPreferredDestinations().contains(searchLocation)) {
                    currentUser.getPreferredDestinations().add(searchLocation);
                    userRepository.save(currentUser);
                }
            });
        }

        return hotels.stream()
                .map(hotel -> {
                    List<Room> suitableRooms = roomRepository.findAvailableRoomsByHotelAndDateRange(hotel.getId(), checkInDate, checkOutDate)
                            .stream()
                            .filter(room -> room.getCapacity() >= guests) // Lọc các phòng có capacity >= guests
                            .sorted(Comparator.comparingInt(room -> Math.abs(room.getCapacity() - guests))) // Sắp xếp theo sự chênh lệch
                            .collect(Collectors.toList());

                    if (!suitableRooms.isEmpty()) {
                        // Tìm các phòng có booking với reviewed = true
                        List<Room> reviewedRooms = suitableRooms.stream()
                                .filter(room -> room.getBookings().stream()
                                        .anyMatch(booking -> booking.isReviewed())
                                )
                                .collect(Collectors.toList());

                        Room bestRoom;
                        if (!reviewedRooms.isEmpty()) {
                            // Nếu có phòng nào có booking được review, tìm phòng tốt nhất trong số này
                            bestRoom = reviewedRooms.stream()
                                    .max(Comparator.comparingDouble(room -> {
                                        long reviewedBookingsCount = room.getBookings().stream()
                                                .filter(booking -> booking.isReviewed())
                                                .count();
                                        double averageRating = room.getAverageRating();

                                        // Normalize reviewed bookings count (assuming max 100 reviewed bookings)
                                        double normalizedBookings = Math.min(reviewedBookingsCount / 100.0, 1.0);
                                        // Rating is already normalized (0-5 scale)
                                        double normalizedRating = averageRating / 5.0;

                                        // Combined score gives equal weight to reviewed bookings and ratings
                                        return (normalizedBookings * 0.5) + (normalizedRating * 0.5);
                                    }))
                                    .orElse(null);
                        } else {
                            // Nếu không có phòng nào có booking được review, trả về một phòng bất kỳ của khách sạn
                            bestRoom = suitableRooms.get(0);
                        }

                        if (bestRoom != null) {


                            // Map Hotel to HotelDto
                            HotelDto hotelDto = new HotelDto();
                            hotelDto.setId(hotel.getId());
                            hotelDto.setName(hotel.getName());
                            hotelDto.setCategory(hotel.getCategory());
                            hotelDto.setRating(hotel.getRating());
                            hotelDto.setLocation(hotel.getLocation());
                            hotelDto.setNumOfReviews(hotel.getNumOfReviews());

                            hotelDto.setPaymentPolicy(hotel.getPaymentPolicy());
                            hotelDto.setExteriorImages(hotel.getExteriorImages());
                            hotelDto.setRoomImages(hotel.getRoomImages());
                            // Convert amenities to AmenityDto list
                            hotelDto.setAmenities(hotel.getAmenities().stream()
                                    .map(this::convertAmenityToDto)
                                    .collect(Collectors.toList()));

                            // Convert best room to RoomDto
                            RoomDto bestRoomDto = mapRoomToRoomDto(bestRoom);

                            // Tìm giá thấp nhất từ suitableRooms (có thể khác với bestRoom)
                            BigDecimal lowestPrice = suitableRooms.stream()
                                    .map(Room::getBasePrice)
                                    .min(Comparator.naturalOrder())
                                    .orElse(BigDecimal.valueOf(Double.MAX_VALUE));

                            return new HotelSearchResultDto(
                                    hotelDto,
                                    lowestPrice,
                                    1,
                                    calculatePopularityScore(hotel),

                                    bestRoomDto
                            );
                        }
                    }
                    return null;
                })
                .filter(result -> result != null)
                .sorted((r1, r2) -> Double.compare(r2.getPopularityScore(), r1.getPopularityScore()))
                .collect(Collectors.toList());
    }

    // Assuming you have a method to convert Room to RoomDto
    private RoomDto mapRoomToRoomDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
        roomDto.setRoomImages(room.getRoomImages());

        // Thay vì chỉ lấy ID, chúng ta chuyển đổi toàn bộ Amenity sang AmenityDto
        List<AmenityDto> amenityDtos = room.getAmenities().stream()
                .map(this::convertToAmenityDto)
                .collect(Collectors.toList());
        roomDto.setAmenities(amenityDtos);

        // Đếm số lượng đánh giá từ các lượt đặt phòng đã được đánh giá
        roomDto.setNumOfReviews((int) room.getBookings().stream()
                .filter(Booking::isReviewed)
                .count());

        return roomDto;
    }

    // Hàm chuyển đổi từ Amenity sang AmenityDto
    private AmenityDto convertToAmenityDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setId(amenity.getId());
        amenityDto.setName(amenity.getName());
        return amenityDto;
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

    private AmenityDto convertAmenityToDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setId(amenity.getId());
        amenityDto.setName(amenity.getName());
        amenityDto.setDescription(amenity.getDescription());
        return amenityDto;
    }
}