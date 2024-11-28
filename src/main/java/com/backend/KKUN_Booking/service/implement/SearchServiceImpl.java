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
import java.util.*;
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
    public List<HotelSearchResultDto> searchHotels(String location, LocalDateTime checkInDate, LocalDateTime checkOutDate, int guests,
                                                   BigDecimal minPrice, BigDecimal maxPrice, List<String> amenities, Double rating,
                                                   Boolean freeCancellation, Boolean breakfastIncluded, Boolean prePayment) {
        List<Hotel> filteredHotels = filterHotelsByCriteria(location, rating, amenities, freeCancellation, breakfastIncluded, prePayment, minPrice, maxPrice);

        addLocationToUserPreferences(location);

        return mapHotelsToDtos(filteredHotels, checkInDate, checkOutDate, guests, minPrice, maxPrice);
    }
    @Override
    public List<HotelSearchResultDto> searchHotelsByName(
            LocalDateTime checkInDate, LocalDateTime checkOutDate, int guests, String hotelName) {

        // Convert hotelName to lowercase for case-insensitive search
        String searchName = hotelName.toLowerCase();

        // Filter hotels by name
        List<Hotel> hotels = hotelRepository.findAll().stream()
                .filter(hotel -> hotel.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());

        return hotels.stream()
                .map(hotel -> createHotelSearchResultDto(hotel, checkInDate, checkOutDate, guests, null, null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(HotelSearchResultDto::getSuitabilityScore)
                        .thenComparing(Comparator.comparingDouble(HotelSearchResultDto::getPopularityScore).reversed()))
                .collect(Collectors.toList());
    }

    private List<Hotel> filterHotelsByCriteria(String location, Double rating, List<String> amenities,
                                               Boolean freeCancellation, Boolean breakfastIncluded, Boolean prePayment,
                                               BigDecimal minPrice, BigDecimal maxPrice) {
        String[] locationKeywords = location.toLowerCase().split(",");

        return hotelRepository.findAll().stream()
                .filter(hotel -> matchesLocation(hotel, locationKeywords))
                .filter(hotel -> rating == null || (hotel.getRating() != null && hotel.getRating() >= rating))
                .filter(hotel -> hasRequiredAmenities(hotel, amenities))
                .filter(hotel -> freeCancellation == null || !freeCancellation || hotel.getFreeCancellation())
                .filter(hotel -> breakfastIncluded == null || !breakfastIncluded || hotel.getBreakfastIncluded())
                .filter(hotel -> prePayment == null || !prePayment || hotel.getPrePayment())
                .filter(hotel -> hasRoomWithinPriceRange(hotel, minPrice, maxPrice))
                .collect(Collectors.toList());
    }

    private boolean matchesLocation(Hotel hotel, String[] locationKeywords) {
        String hotelLocation = hotel.getLocation().toLowerCase();
        return Arrays.stream(locationKeywords).anyMatch(hotelLocation::contains);
    }

    private boolean hasRequiredAmenities(Hotel hotel, List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) return true;

        // Chuẩn hóa danh sách tiện ích khách sạn
        List<String> hotelAmenities = hotel.getAmenities().stream()
                .map(amenity -> amenity.getName().toLowerCase()) // Convert to lowercase
                .collect(Collectors.toList());

        // Chuẩn hóa danh sách tiện ích yêu cầu
        List<String> normalizedAmenities = amenities.stream()
                .map(String::toLowerCase) // Convert to lowercase
                .collect(Collectors.toList());

        // Kiểm tra nếu tất cả tiện ích yêu cầu xuất hiện dưới dạng chứa trong tên tiện ích khách sạn
        return normalizedAmenities.stream()
                .allMatch(requiredAmenity ->
                        hotelAmenities.stream().anyMatch(hotelAmenity -> hotelAmenity.contains(requiredAmenity))
                );
    }

    private boolean hasRoomWithinPriceRange(Hotel hotel, BigDecimal minPrice, BigDecimal maxPrice) {
        return hotel.getRooms().stream()
                .anyMatch(room -> (minPrice == null || room.getBasePrice().compareTo(minPrice) >= 0) &&
                        (maxPrice == null || room.getBasePrice().compareTo(maxPrice) <= 0));
    }

    private void addLocationToUserPreferences(String location) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                if (!user.getPreferredDestinations().contains(location)) {
                    user.getPreferredDestinations().add(location);
                    userRepository.save(user);
                }
            });
        }
    }

    private List<HotelSearchResultDto> mapHotelsToDtos(List<Hotel> hotels, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                                                       int guests, BigDecimal minPrice, BigDecimal maxPrice) {
        return hotels.stream()
                .map(hotel -> createHotelSearchResultDto(hotel, checkInDate, checkOutDate, guests, minPrice, maxPrice))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(HotelSearchResultDto::getSuitabilityScore)
                        .thenComparing(Comparator.comparingDouble(HotelSearchResultDto::getPopularityScore).reversed()))
                .collect(Collectors.toList());
    }

    private HotelSearchResultDto createHotelSearchResultDto(Hotel hotel, LocalDateTime checkInDate, LocalDateTime checkOutDate,
                                                            int guests, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Room> suitableRooms = roomRepository.findAll().stream()
                .filter(room -> room.getCapacity() >= guests)
                .filter(room -> isRoomWithinPriceRange(room, minPrice, maxPrice))
                .collect(Collectors.toList());

        if (suitableRooms.isEmpty()) return null;

        Room bestRoom = selectBestRoom(suitableRooms, guests);
        int suitabilityScore = Math.abs(bestRoom.getCapacity() - guests);

        return new HotelSearchResultDto(
                mapHotelToDto(hotel),
                getLowestRoomPrice(suitableRooms),
                1,
                calculatePopularityScore(hotel),
                mapRoomToRoomDto(bestRoom),
                suitabilityScore
        );
    }

    private boolean isRoomWithinPriceRange(Room room, BigDecimal minPrice, BigDecimal maxPrice) {
        BigDecimal price = room.getBasePrice();
        return (minPrice == null || price.compareTo(minPrice) >= 0) &&
                (maxPrice == null || price.compareTo(maxPrice) <= 0);
    }

    private Room selectBestRoom(List<Room> rooms, int guests) {
        int minSuitabilityScore = rooms.stream()
                .mapToInt(room -> Math.abs(room.getCapacity() - guests))
                .min()
                .orElse(Integer.MAX_VALUE);

        return rooms.stream()
                .filter(room -> Math.abs(room.getCapacity() - guests) == minSuitabilityScore)
                .max(Comparator.comparingDouble(this::calculateRoomScore))
                .orElse(rooms.get(0));
    }

    private double calculateRoomScore(Room room) {
        long reviewedBookings = room.getBookings().stream().filter(Booking::isReviewed).count();
        double averageRating = room.getAverageRating();
        return 0.5 * Math.min(reviewedBookings / 100.0, 1.0) + 0.5 * (averageRating / 5.0);
    }

    private BigDecimal getLowestRoomPrice(List<Room> rooms) {
        return rooms.stream()
                .map(Room::getBasePrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.valueOf(Double.MAX_VALUE));
    }

    private HotelDto mapHotelToDto(Hotel hotel) {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(hotel.getId());
        hotelDto.setName(hotel.getName());
        hotelDto.setCategory(hotel.getCategory());
        hotelDto.setRating(hotel.getRating());
        hotelDto.setLocation(hotel.getLocation());
        hotelDto.setNumOfReviews(hotel.getNumOfReviews());
        hotelDto.setPaymentPolicy(hotel.getPaymentPolicy());
        hotelDto.setFreeCancellation(hotel.getFreeCancellation());
        hotelDto.setBreakfastIncluded(hotel.getBreakfastIncluded());
        hotelDto.setPrePayment(hotel.getPrePayment());
        hotelDto.setExteriorImages(hotel.getExteriorImages());
        hotelDto.setRoomImages(hotel.getRoomImages());
        hotelDto.setAmenities(hotel.getAmenities().stream().map(this::convertAmenityToDto).collect(Collectors.toList()));
        return hotelDto;
    }

    private RoomDto mapRoomToRoomDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
        roomDto.setRoomImages(room.getRoomImages());
        roomDto.setAmenities(room.getAmenities().stream().map(this::convertAmenityToDto).collect(Collectors.toList()));
        roomDto.setNumOfReviews((int) room.getBookings().stream().filter(Booking::isReviewed).count());
        return roomDto;
    }

    private AmenityDto convertAmenityToDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setId(amenity.getId());
        amenityDto.setName(amenity.getName());
        return amenityDto;
    }

    private double calculatePopularityScore(Hotel hotel) {
        double totalRating = hotel.getRooms().stream().flatMap(room -> room.getReviews().stream())
                .mapToDouble(RoomReview::getOverallRating).sum();
        int totalReviews = (int) hotel.getRooms().stream().flatMap(room -> room.getReviews().stream()).count();

        return totalReviews > 0 ? Math.min((totalRating / totalReviews) * 2, 10.0) : 0.0;
    }
}
