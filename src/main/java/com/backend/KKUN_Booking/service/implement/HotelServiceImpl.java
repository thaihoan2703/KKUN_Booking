package com.backend.KKUN_Booking.service.implement;

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

    @Override
    public HotelDto updateHotel(UUID id, HotelDto hotelDto, MultipartFile[] newExteriorImages, String userEmail) {
        Hotel hotel = findHotelById(id);
        User user = findUserByEmail(userEmail);
        validateUserOwnership(hotel, user);

        updateHotelFields(hotel, hotelDto);
        updateExteriorImages(hotel, newExteriorImages, hotelDto);
        updatePaymentPolicy(hotel, hotelDto.getPaymentPolicy());

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
        hotelDto.setExteriorImages(hotel.getExteriorImages());
        hotelDto.setRoomImages(hotel.getRoomImages());
        hotelDto.setPaymentPolicy(hotel.getPaymentPolicy());
        hotelDto.setNumOfReviews(hotel.getNumOfReviews());
        hotelDto.setAmenityIds(hotel.getAmenities().stream().map(Amenity::getId).collect(Collectors.toList()));
        hotelDto.setRooms(hotel.getRooms().stream().map(this::convertRoomToDto).collect(Collectors.toList()));
        return hotelDto;
    }

    private Hotel convertToEntity(HotelDto hotelDto) {
        Hotel hotel = new Hotel();
        hotel.setId(hotelDto.getId());
        hotel.setName(hotelDto.getName());
        hotel.setCategory(hotelDto.getCategory());
        hotel.setRating(hotelDto.getRating());
        hotel.setLocation(hotelDto.getLocation());
        hotel.setExteriorImages(hotelDto.getExteriorImages());
        hotel.setRoomImages(hotelDto.getRoomImages());
        hotel.setPaymentPolicy(hotelDto.getPaymentPolicy());
        hotel.setRooms(convertRooms(hotelDto.getRooms(), hotel));
        hotel.setAmenities(convertAmenities(hotelDto.getAmenityIds()));
        return hotel;
    }

    private RoomDto convertRoomToDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setHotelId(room.getHotel().getId());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
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
        // Convert RoomDto to Room entity
        Room room = new Room();
        room.setId(roomDto.getId());  // Đảm bảo rằng UUID được xử lý đúng
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());

        // Lấy đối tượng Hotel từ hotelId
        if (roomDto.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            room.setHotel(hotel); // Gán đối tượng Hotel vào phòng
        }

        return room;
    }
    private List<Amenity> convertAmenities(List<UUID> amenityIds) {
        return amenityIds != null ? amenityIds.stream()
                .map(amenityId -> {
                    Amenity amenity = new Amenity();
                    amenity.setId(amenityId);
                    return amenity;
                }).collect(Collectors.toList()) : new ArrayList<>();
    }

    private List<String> uploadExteriorImages(MultipartFile[] exteriorImages, HotelDto hotelDto) {
        List<String> exteriorImageUrls = new ArrayList<>();
        if(hotelDto.getId() != null){
            HotelDto hotelDtoTemp = getHotelById(hotelDto.getId());
            // Lấy danh sách các URL ảnh cũ từ hotelDto
            List<String> oldImageUrls = hotelDtoTemp.getExteriorImages(); // Giả sử hotelDto có phương thức này
            if (oldImageUrls != null) { // Kiểm tra xem oldImageUrls có phải là null không
                // Xóa các ảnh cũ trước khi tải ảnh mới lên
                for (String oldImageUrl : oldImageUrls) {
                    amazonS3Service.deleteFile(oldImageUrl); // Xóa ảnh cũ
                }
            }
        }

        if (exteriorImages != null) {
            // Tải ảnh mới lên
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
        if (paymentPolicy == null ||
                (!paymentPolicy.equals(PaymentPolicy.ONLINE) && !paymentPolicy.equals(PaymentPolicy.CHECKOUT))) {
            throw new IllegalArgumentException("Invalid payment policy. It must be either 'ONLINE' or 'CHECKOUT'.");
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
