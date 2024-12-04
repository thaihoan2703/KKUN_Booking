package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.*;
import com.backend.KKUN_Booking.model.*;
import com.backend.KKUN_Booking.model.enumModel.BedType;
import com.backend.KKUN_Booking.model.enumModel.RoomType;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.RoomRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.NearbyPlaceService;
import com.backend.KKUN_Booking.service.RoomService;
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

import static com.backend.KKUN_Booking.util.CommonFunction.removeAccents;

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
    private RoomService roomService;
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
        String searchName = removeAccents(hotelName).toLowerCase();

        // Lọc danh sách khách sạn
        List<Hotel> hotels = hotelRepository.findAll().stream()
                .filter(hotel -> removeAccents(hotel.getName()).toLowerCase().contains(searchName))
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
        String[] locationKeywords = removeAccents(location).toLowerCase().split(",");

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
        String hotelLocation = removeAccents(hotel.getLocation()).toLowerCase();
        return Arrays.stream(locationKeywords).anyMatch(hotelLocation::contains);
    }

    private boolean hasRequiredAmenities(Hotel hotel, List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) return true;

        // Normalize hotel amenities (convert to lowercase, remove special characters)
        List<String> hotelAmenities = hotel.getAmenities().stream()
                .map(amenity -> normalizeString(amenity.getName()))  // Normalize hotel amenities
                .collect(Collectors.toList());

        // Normalize the input amenities
        List<String> normalizedAmenities = amenities.stream()
                .map(this::normalizeString)  // Normalize user input
                .collect(Collectors.toList());

        // Get synonym map to check for similar terms
        Map<String, List<String>> synonymMap = getSynonymMap();

        // Check if all required amenities are present in the hotel's amenities
        return normalizedAmenities.stream()
                .allMatch(requiredAmenity ->
                        hotelAmenities.stream().anyMatch(hotelAmenity ->
                                matchesAmenity(hotelAmenity, requiredAmenity, synonymMap)
                        )
                );
    }

    /**
     * Normalize the amenity string by converting it to lowercase and removing special characters.
     */
    private String normalizeString(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]", ""); // Remove non-alphanumeric characters
    }

    /**
     * Match amenities by checking if the hotel amenity matches the required one,
     * including checking synonyms.
     */
    private boolean matchesAmenity(String hotelAmenity, String requiredAmenity, Map<String, List<String>> synonymMap) {
        // First, check if the exact normalized name matches
        if (hotelAmenity.equals(requiredAmenity)) {
            return true;
        }

        // Then, check if the required amenity matches any of the synonyms for the hotel amenity
        if (synonymMap.containsKey(requiredAmenity)) {
            List<String> synonyms = synonymMap.get(requiredAmenity);
            return synonyms.stream().anyMatch(synonym -> hotelAmenity.contains(synonym));
        }

        // Finally, check if the required amenity is part of the hotel's amenity (substring match)
        return hotelAmenity.contains(requiredAmenity);
    }

    /**
     * Example synonym map: You can expand this as needed.
     */
    private Map<String, List<String>> getSynonymMap() {
        Map<String, List<String>> synonyms = new HashMap<>();

        // Example: Wi-Fi synonyms (English + Vietnamese)
        synonyms.put("wifi", Arrays.asList("wi-fi", "wireless internet", "internet", "wi fi", "wifi", "mạng không dây", "internet không dây"));

        // Example: Air Conditioning synonyms (English + Vietnamese)
        synonyms.put("air conditioner", Arrays.asList("ac", "air conditioning", "air-conditioner", "máy lạnh", "điều hòa", "máy điều hòa"));

        // Example: Parking synonyms (English + Vietnamese)
        synonyms.put("parking", Arrays.asList("free parking", "parking included", "on-site parking", "bãi đỗ xe", "đậu xe miễn phí", "chỗ đậu xe"));

        // Example: Breakfast synonyms (English + Vietnamese)
        synonyms.put("breakfast", Arrays.asList("continental breakfast", "included breakfast", "free breakfast", "bữa sáng", "sáng miễn phí"));

        // Example: Swimming Pool synonyms (English + Vietnamese)
        synonyms.put("swimming pool", Arrays.asList("pool", "swimming pool", "bể bơi", "hồ bơi"));

        // Example: Gym synonyms (English + Vietnamese)
        synonyms.put("gym", Arrays.asList("fitness center", "gym", "workout room", "phòng gym", "phòng thể dục"));

        // Example: Restaurant synonyms (English + Vietnamese)
        synonyms.put("restaurant", Arrays.asList("restaurant", "dining", "đồ ăn", "nhà hàng", "quán ăn"));

        // Example: Spa synonyms (English + Vietnamese)
        synonyms.put("spa", Arrays.asList("spa", "wellness center", "massage", "mát xa", "trung tâm spa", "khu spa"));

        // Example: Pet-Friendly synonyms (English + Vietnamese)
        synonyms.put("pet friendly", Arrays.asList("pet friendly", "pets allowed", "chấp nhận thú cưng", "cho phép vật nuôi"));

        // Example: Elevator synonyms (English + Vietnamese)
        synonyms.put("elevator", Arrays.asList("elevator", "lift", "thang máy"));

        // Example: Non-smoking Room synonyms (English + Vietnamese)
        synonyms.put("non-smoking room", Arrays.asList("non-smoking room", "phòng không hút thuốc", "phòng cấm hút thuốc"));

        // Example: Airport Shuttle synonyms (English + Vietnamese)
        synonyms.put("airport shuttle", Arrays.asList("airport shuttle", "shuttle service", "dịch vụ đưa đón sân bay"));

        // Example: Room Service synonyms (English + Vietnamese)
        synonyms.put("room service", Arrays.asList("room service", "dịch vụ phòng"));

        // Add more synonym mappings as needed

        return synonyms;
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
        List<RoomDto> roomDtos = roomService.findAvailableRooms(hotel.getId(), checkInDate, checkOutDate).stream()
                .filter(room -> room.getCapacity() >= guests) // Phòng có sức chứa đủ cho số lượng khách
                .filter(room -> isRoomWithinPriceRange(room, minPrice, maxPrice)) // Phòng nằm trong phạm vi giá
                .collect(Collectors.toList());

        if (roomDtos.isEmpty()) return null;

        // Chuyển đổi RoomDto sang Room ngay trong đây
        List<Room> suitableRooms = roomDtos.stream()
                .map(this::mapRoomDtoToRoom)
                .toList();

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

    private boolean isRoomWithinPriceRange(RoomDto roomDto, BigDecimal minPrice, BigDecimal maxPrice) {
        BigDecimal price = roomDto.getBasePrice();
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
    private Room mapRoomDtoToRoom(RoomDto roomDto) {
        // Tạo một đối tượng Room mới từ các thuộc tính của RoomDto
        return roomRepository.findById(roomDto.getId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomDto.getId()));
    }
    private RoomDto mapRoomToRoomDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setBedType(room.getBedType());
        roomDto.setArea(room.getArea());
        roomDto.setHotelId(room.getHotel().getId());

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

    public List<RoomDto> searchRoomsByAttributes(String location,
                                                 LocalDateTime checkInDate,
                                                 LocalDateTime checkOutDate,
                                                 int guests,
                                                 String roomTypeDisplayName,
                                                 String bedTypeDisplayName) {

        // Chuyển đổi roomType và bedType thành enum nếu có (nếu người dùng nhập là chuỗi)
        String roomType = roomTypeDisplayName != null ? RoomType.fromDisplayName(roomTypeDisplayName) : null;
        String bedType = bedTypeDisplayName != null ? BedType.fromDisplayName(bedTypeDisplayName) : null;

        // Tìm kiếm các phòng từ repository với các tham số đã cung cấp
        List<Room> rooms = roomRepository.searchRoomsByAttributes(location, roomType, bedType,guests,checkInDate,checkOutDate);

        // Chuyển đổi từ Room entity sang RoomDto
        return rooms.stream()
                .map(this::mapRoomToRoomDto)
                .collect(Collectors.toList());
    }
}
