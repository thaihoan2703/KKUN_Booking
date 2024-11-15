package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.AmenityDto;
import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.*;
import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.repository.AmenityRepository;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.AmazonS3Service;
import com.backend.KKUN_Booking.service.HotelService;
import com.backend.KKUN_Booking.service.RoomService;
import com.backend.KKUN_Booking.util.CommonFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public HotelServiceImpl(HotelRepository hotelRepository, UserRepository userRepository,
                            AmenityRepository amenityRepository, AmazonS3Service amazonS3Service) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.amenityRepository = amenityRepository;
        this.amazonS3Service = amazonS3Service;
    }

    @Override
    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public HotelDto getHotelById(UUID id) {
        return hotelRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }

    @Override
    public HotelDto createHotel(HotelDto hotelDto, MultipartFile[] exteriorImages, String userEmail) {
        User user = findUserByEmail(userEmail);
        validateUserAsHotelOwner(user);
        ensureUserHasNoExistingHotel(user);
        validatePaymentPolicy(hotelDto.getPaymentPolicy());

        List<String> exteriorImageUrls = uploadExteriorImages(exteriorImages, hotelDto);
        Hotel hotel = convertToEntity(hotelDto);
        hotel.setOwner(user);
        hotel.setExteriorImages(exteriorImageUrls);

        return convertToDto(hotelRepository.save(hotel));
    }

        public HotelDto createHotelAndRooms(
            HotelDto hotelDto,
            MultipartFile[] exteriorImages,
            MultipartFile[] roomImages,
            String userEmail) {

        // Tìm và xác minh người dùng
        User user = findUserByEmail(userEmail);
        validateUserAsHotelOwner(user);
        ensureUserHasNoExistingHotel(user);
        validatePaymentPolicy(hotelDto.getPaymentPolicy());
        validateAmenities(hotelDto.getAmenities());

        // Tải lên ảnh ngoại thất của khách sạn
        List<String> exteriorImageUrls = uploadExteriorImages(exteriorImages, hotelDto);

        // Chuyển đổi từ HotelDto sang đối tượng Hotel và thiết lập thông tin
        Hotel hotel = convertToEntity(hotelDto);
        hotel.setOwner(user);
        hotel.setExteriorImages(exteriorImageUrls);

        // Thiết lập danh sách tiện ích cho khách sạn
        List<Amenity> amenities = convertAmenities(
                hotelDto.getAmenities().stream().map(AmenityDto::getId).collect(Collectors.toList())
        );
        hotel.setAmenities(amenities);

        // Xử lý và gán ảnh cho từng phòng trong danh sách Rooms
        if (hotelDto.getRooms() != null && roomImages != null) {
            List<Room> rooms = new ArrayList<>();
            int imageIndex = 0; // Biến đếm để theo dõi vị trí trong mảng roomImages

            for (RoomDto roomDto : hotelDto.getRooms()) {
                Room room = convertRoomToEntity(roomDto);
                room.setHotel(hotel);
                room.setAvailable(true);

                // Thêm tiện ích cho phòng
                if (roomDto.getAmenities() != null) {
                    List<Amenity> roomAmenities = convertAmenities(
                            roomDto.getAmenities().stream().map(AmenityDto::getId).collect(Collectors.toList())
                    );
                    room.setAmenities(roomAmenities);
                }

                // Gán ảnh cho phòng nếu còn ảnh trong mảng roomImages
                List<String> roomImageUrls = new ArrayList<>();
                while (imageIndex < roomImages.length && roomImages[imageIndex] != null) {
                    MultipartFile image = roomImages[imageIndex];
                    if (!image.isEmpty()) {
                        String uniqueFileName = createUniqueFileName(roomDto.getType().toString());
                        String s3ImageUrl = amazonS3Service.uploadFile(image, uniqueFileName);
                        roomImageUrls.add(s3ImageUrl);
                    }
                    imageIndex++; // Chuyển tới ảnh tiếp theo
                }
                room.setRoomImages(roomImageUrls);
                rooms.add(room);
            }
            hotel.setRooms(rooms);
        }

        // Lưu khách sạn và trả về HotelDto
        return convertToDto(hotelRepository.save(hotel));
    }


    @Override
    public HotelDto updateHotel(UUID id, HotelDto hotelDto, MultipartFile[] newExteriorImages, String userEmail) {
        Hotel hotel = findHotelById(id);
        User user = findUserByEmail(userEmail);
        validateUserOwnership(hotel, user);

        updateHotelFields(hotel, hotelDto);
        updateExteriorImages(hotel, newExteriorImages, hotelDto);
        updatePaymentPolicy(hotel, hotelDto.getPaymentPolicy());
        updateAmenities(hotel, hotelDto.getAmenities()); // New method for updating amenities

        return convertToDto(hotelRepository.save(hotel));
    }


    @Override
    public void deleteHotel(UUID id) {
        hotelRepository.deleteById(id);
    }

    @Override
    public List<HotelDto> findTopHotelsByRating(int limit) {
        return hotelRepository.findTopHotelsByRating(limit).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotelDto> findTrendingDestinations(int limit) {
        return hotelRepository.findTrendingDestinations(limit).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void updateHotelFields(Hotel hotel, HotelDto hotelDto) {
        if (hotelDto.getName() != null) hotel.setName(hotelDto.getName());
        if (hotelDto.getCategory() != null) hotel.setCategory(hotelDto.getCategory());
        if (hotelDto.getRating() != null) hotel.setRating(hotelDto.getRating());
        if (hotelDto.getLocation() != null) hotel.setLocation(hotelDto.getLocation());
    }

    private void updateExteriorImages(Hotel hotel, MultipartFile[] newExteriorImages, HotelDto hotelDto) {
        if (newExteriorImages != null && newExteriorImages.length > 0) {
            List<String> exteriorImageUrls = uploadExteriorImages(newExteriorImages, hotelDto);
            hotel.setExteriorImages(exteriorImageUrls);
        }
    }

    private void updatePaymentPolicy(Hotel hotel, PaymentPolicy paymentPolicy) {
        if (paymentPolicy != null) {
            validatePaymentPolicy(paymentPolicy);
            hotel.setPaymentPolicy(paymentPolicy);
        }
    }

    private HotelDto convertToDto(Hotel hotel) {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setId(hotel.getId());
        hotelDto.setName(hotel.getName());
        hotelDto.setCategory(hotel.getCategory());
        hotelDto.setRating(hotel.getRating());
        hotelDto.setLocation(hotel.getLocation());
        hotelDto.setDescription(hotel.getDescription());
        hotelDto.setFreeCancellation(hotel.getFreeCancellation());
        hotelDto.setBreakfastIncluded(hotel.getBreakfastIncluded());
        hotelDto.setPrePayment(hotel.getPrePayment());
        hotelDto.setPaymentPolicy(hotel.getPaymentPolicy());

        hotelDto.setNumOfReviews(hotel.getNumOfReviews());
        hotelDto.setOwnerName(hotel.getOwner().getFirstName() + " " + hotel.getOwner().getLastName());

        // Thiết lập danh sách đường dẫn ảnh ngoại thất và phòng
        hotelDto.setExteriorImages(hotel.getExteriorImages());
        hotelDto.setRoomImages(hotel.getRoomImages());

        // Chuyển đổi từ Amenity sang AmenityDto và gán vào hotelDto
        List<AmenityDto> amenityDtos = hotel.getAmenities().stream()
                .map(this::convertAmenityToDto)
                .collect(Collectors.toList());
        hotelDto.setAmenities(amenityDtos);

        // Chuyển đổi từ Room sang RoomDto và gán vào hotelDto
        List<RoomDto> roomDtos = hotel.getRooms().stream()
                .map(this::convertRoomToDto)
                .collect(Collectors.toList());
        hotelDto.setRooms(roomDtos);

        return hotelDto;
    }

    private Hotel convertToEntity(HotelDto hotelDto) {
        Hotel hotel = new Hotel();
        hotel.setId(hotelDto.getId());
        hotel.setName(hotelDto.getName());
        hotel.setCategory(hotelDto.getCategory());
        hotel.setDescription(hotelDto.getDescription());
        hotel.setRating(hotelDto.getRating());
        hotel.setLocation(hotelDto.getLocation());
        hotel.setFreeCancellation(hotelDto.getFreeCancellation());
        hotel.setBreakfastIncluded(hotelDto.getBreakfastIncluded());
        hotel.setPrePayment(hotelDto.getPrePayment());
        hotel.setPaymentPolicy(hotelDto.getPaymentPolicy());
        hotel.setExteriorImages(hotelDto.getExteriorImages());
        hotel.setRoomImages(hotelDto.getRoomImages());

        // Chuyển đổi từ RoomDto sang Room và thiết lập liên kết với khách sạn
        List<Room> rooms = hotelDto.getRooms() != null ? hotelDto.getRooms().stream()
                .map(roomDto -> {
                    Room room = convertRoomToEntity(roomDto);
                    room.setHotel(hotel); // Liên kết room với hotel
                    return room;
                })
                .collect(Collectors.toList()) : new ArrayList<>();
        hotel.setRooms(rooms);

        // Chuyển đổi từ AmenityDto sang Amenity
        if (hotelDto.getAmenities() != null) {
            List<UUID> amenityIds = hotelDto.getAmenities().stream()
                    .map(AmenityDto::getId)
                    .collect(Collectors.toList());
            hotel.setAmenities(convertAmenities(amenityIds));
        }

        return hotel;
    }

    private AmenityDto convertAmenityToDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setId(amenity.getId());
        amenityDto.setName(amenity.getName());
        amenityDto.setDescription(amenity.getDescription());
        amenityDto.setAmenityType(amenity.getAmenityType());
        // Set other fields as needed
        return amenityDto;
    }

    private List<Amenity> convertAmenities(List<UUID> amenityIds) {
        return amenityRepository.findAllById(amenityIds);
    }

    private RoomDto convertRoomToDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
        roomDto.setHotelId(room.getHotel().getId());
        roomDto.setRoomImages(room.getRoomImages());
        roomDto.setBedType(room.getBedType()); // Gán loại giường vào DTO
        roomDto.setBedCount(room.getBedCount()); // Gán số lượng giường vào DTO
        roomDto.setArea(room.getArea());

        // Chuyển đổi từ Amenity sang AmenityDto và gán vào roomDto
        List<AmenityDto> amenityDtos = room.getAmenities().stream()
                .map(this::convertAmenityToDto)
                .collect(Collectors.toList());
        roomDto.setAmenities(amenityDtos);

        return roomDto;
    }

    private List<Room> convertRooms(List<RoomDto> roomDtos, Hotel hotel) {
        return roomDtos != null ? roomDtos.stream()
                .map(roomDto -> {
                    Room room = convertRoomToEntity(roomDto);
                    room.setHotel(hotel);
                    return room;
                }).collect(Collectors.toList()) : new ArrayList<>();
    }

    private Room convertRoomToEntity(RoomDto roomDto) {
        Room room = new Room();
        room.setId(roomDto.getId());
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());
        room.setRoomImages(roomDto.getRoomImages());
        room.setBedType(roomDto.getBedType());
        room.setBedCount(roomDto.getBedCount());
        room.setArea(roomDto.getArea());
        return room;
    }

    private void validateAmenities(List<AmenityDto> amenities) {
        if (amenities != null) {
            List<UUID> amenityIds = amenities.stream()
                    .map(AmenityDto::getId)
                    .collect(Collectors.toList());

            // Check if all amenities exist in the database
            List<Amenity> existingAmenities = amenityRepository.findAllById(amenityIds);
            if (existingAmenities.size() != amenityIds.size()) {
                throw new ResourceNotFoundException("One or more amenities not found");
            }
        }
    }

    private void updateAmenities(Hotel hotel, List<AmenityDto> amenityDtos) {
        if (amenityDtos != null) {
            List<UUID> amenityIds = amenityDtos.stream()
                    .map(AmenityDto::getId)
                    .collect(Collectors.toList());

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
            hotel.setAmenities(amenities);
        }
    }
    private List<String> uploadExteriorImages(MultipartFile[] exteriorImages, HotelDto hotelDto) {
        List<String> exteriorImageUrls = new ArrayList<>();
        if (hotelDto.getId() != null) {
            HotelDto hotelDtoTemp = getHotelById(hotelDto.getId());
            List<String> oldImageUrls = hotelDtoTemp.getExteriorImages();
            if (oldImageUrls != null) {
                oldImageUrls.forEach(amazonS3Service::deleteFile);
            }
        }

        if (exteriorImages != null) {
            for (MultipartFile exteriorImage : exteriorImages) {
                if (!exteriorImage.isEmpty()) {
                    String uniqueFileName = createUniqueFileName(hotelDto.getName());
                    String s3ImageUrl = amazonS3Service.uploadFile(exteriorImage, uniqueFileName);
                    exteriorImageUrls.add(s3ImageUrl);
                }
            }
        }

        return exteriorImageUrls;
    }

    private String createUniqueFileName(String hotelName) {
        String seoFileName = CommonFunction.SEOUrl(hotelName);
        return seoFileName + "-" + UUID.randomUUID().toString();
    }

    private void validateUserAsHotelOwner(User user) {
        if (!user.getRole().getName().equals(RoleUser.HOTELOWNER.name())) {
            throw new IllegalArgumentException("User is not a hotel owner");
        }
    }

    private void ensureUserHasNoExistingHotel(User user) {
        if (hotelRepository.existsByOwner(user)) {
            throw new IllegalArgumentException("User already has a hotel and can only create one.");
        }
    }

    private void validatePaymentPolicy(PaymentPolicy paymentPolicy) {
        if (paymentPolicy == null || (!paymentPolicy.equals(PaymentPolicy.ONLINE) &&
                !paymentPolicy.equals(PaymentPolicy.CHECKOUT) && !paymentPolicy.equals(PaymentPolicy.ONLINE_CHECKOUT))) {
            throw new IllegalArgumentException("Invalid payment policy. It must be either 'ONLINE' or 'CHECKOUT' or both.");
        }
    }

    private void validateUserOwnership(Hotel hotel, User user) {
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not have permission to update this hotel.");
        }
    }

    private Hotel findHotelById(UUID id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
