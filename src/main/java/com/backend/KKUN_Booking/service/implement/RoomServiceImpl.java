package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.AmenityDto;
import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Amenity;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.UserAbstract.HotelOwnerUser;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.repository.AmenityRepository;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.RoomRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.AmazonS3Service;
import com.backend.KKUN_Booking.service.RoomService;
import com.backend.KKUN_Booking.util.CommonFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, HotelRepository hotelRepository, AmenityRepository amenityRepository, UserRepository userRepository, AmazonS3Service amazonS3Service) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.amenityRepository = amenityRepository;
        this.userRepository = userRepository;
        this.amazonS3Service = amazonS3Service;
    }

    @Override
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(UUID id) {
        return roomRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    @Override
    public RoomDto createRoom(@RequestBody RoomDto roomDto, MultipartFile[] roomImages, String userEmail) {
        User user = findUserByEmail(userEmail);
        // Kiểm tra và ép kiểu sang HotelOwnerUser
        if (!(user instanceof HotelOwnerUser)) {
            throw new IllegalArgumentException("User is not a hotel owner.");
        }
        HotelOwnerUser hotelOwnerUser = (HotelOwnerUser) user;

        // Tiếp tục xử lý như ban đầu
        validateUserRole(hotelOwnerUser);

        roomDto.setHotelId(hotelOwnerUser.getHotel().getId());

        List<String> roomImageUrls = uploadRoomImages(roomImages, roomDto);
        roomDto.setAvailable(true);
        Room room = convertToEntity(roomDto, roomImageUrls);

        return convertToDto(roomRepository.save(room));
    }

    @Override
    public RoomDto updateRoom(UUID id, RoomDto roomDto, MultipartFile[] newRoomImages, String userEmail) {
        Room room = findRoomById(id);
        updateRoomDetails(room, roomDto);
        updateRoomImages(room, newRoomImages, roomDto);
        updateAmenities(room, roomDto.getAmenities());
        return convertToDto(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }

    private User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateUserRole(User user) {
        if (!user.getRole().getName().equals(RoleUser.HOTELOWNER.name())) {
            throw new IllegalArgumentException("User is not a hotel owner");
        }
    }

    private void updateRoomDetails(Room room, RoomDto roomDto) {
        // Update only if the type is different from the current type
        if (roomDto.getType() != null && !roomDto.getType().equals(room.getType())) {
            room.setType(roomDto.getType());
        }

        // Update capacity if it is different
        if (roomDto.getCapacity() != room.getCapacity() && roomDto.getCapacity() >0) {
            room.setCapacity(roomDto.getCapacity());
        }

        // Update base price if it is different
        if (roomDto.getBasePrice() != null && roomDto.getBasePrice().compareTo(room.getBasePrice()) != 0) {
            room.setBasePrice(roomDto.getBasePrice());
        }

        // Update availability if it is different
        if (roomDto.isAvailable() != room.getAvailable()) {
            room.setAvailable(roomDto.isAvailable());
        }


    }

    private void updateRoomImages(Room room, MultipartFile[] newRoomImages, RoomDto roomDto) {
        if (newRoomImages != null && newRoomImages.length > 0) {
            roomDto.setId(room.getId());
            roomDto.setHotelId(room.getHotel().getId());
            List<String> imageUrls = uploadRoomImages(newRoomImages, roomDto);
            room.setRoomImages(imageUrls);
        }
    }

    private List<String> uploadRoomImages(MultipartFile[] roomImages, RoomDto roomDto) {
        List<String> imageUrls = new ArrayList<>();
        if(roomDto.getId() != null){
            RoomDto existingRoomDto = getRoomById(roomDto.getId());
            deleteExistingImages(existingRoomDto.getRoomImages());
        }
        if (roomImages != null) {
            for (MultipartFile image : roomImages) {
                if (!image.isEmpty()) {
                    String uniqueFileName = createUniqueFileName(roomDto.getType().toString());
                    String s3ImageUrl = amazonS3Service.uploadRoomFile(image, roomDto.getHotelId().toString(), uniqueFileName);
                    imageUrls.add(s3ImageUrl);
                }
            }
        }

        return imageUrls;
    }

    private void deleteExistingImages(List<String> oldImageUrls) {
        if (oldImageUrls != null) {
            // Xóa các ảnh cũ trước khi tải ảnh mới lên
            for (String oldImageUrl : oldImageUrls) {
                amazonS3Service.deleteFile(oldImageUrl); // Xóa ảnh cũ
            }
        }
    }
    private void updateAmenities(Room room, List<AmenityDto> amenityDtos) {
        if (amenityDtos != null) {
            List<UUID> amenityIds = amenityDtos.stream()
                    .map(AmenityDto::getId)
                    .collect(Collectors.toList());

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
            room.setAmenities(amenities);
        }
    }

    public List<RoomDto> findAvailableRooms(UUID hotelId, LocalDateTime checkinDate, LocalDateTime checkoutDate){
        // Gọi repository để lấy danh sách các phòng trống
        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelAndDateRange(hotelId, checkinDate, checkoutDate);

        // Chuyển đổi danh sách phòng từ Room sang RoomDto
        return availableRooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    };

    public List<RoomDto> getRoomsByHotelId(UUID hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách sạn với ID: " + hotelId));

        // Lấy danh sách phòng từ khách sạn và chuyển đổi sang RoomDto
        return hotel.getRooms().stream()
                .map(this::convertToDto) // Hàm convert từ Room sang RoomDto
                .collect(Collectors.toList());
    };

    private Room convertToEntity(RoomDto roomDto, List<String> roomImageUrls) {
        Room room = new Room();
        room.setId(roomDto.getId());
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());
        room.setRoomImages(roomImageUrls);
        room.setBedType(roomDto.getBedType()); // Gán loại giường
        room.setBedCount(roomDto.getBedCount()); // Gán số lượng giường
        room.setArea(roomDto.getArea());
        if (roomDto.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            room.setHotel(hotel);
        }
        // Chuyển đổi AmenityDto thành Amenity và gán vào room
       if(roomDto.getAmenities() != null){
           List<Amenity> amenities = roomDto.getAmenities().stream()
                   .map(this::convertToAmenity)
                   .collect(Collectors.toList());
           room.setAmenities(amenities);
       }
        return room;
    }

    private RoomDto convertToDto(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
        roomDto.setHotelId(room.getHotel().getId());
        roomDto.setRoomImages(room.getRoomImages());

        int numOfReviews = (room.getBookings() != null) ? room.getBookings().size() : 0;        // assuming 'getBookings()' returns a list of bookings
        roomDto.setNumOfReviews(numOfReviews);

        roomDto.setBedType(room.getBedType()); // Gán loại giường vào DTO
        roomDto.setBedCount(room.getBedCount()); // Gán số lượng giường vào DTO
        roomDto.setArea(room.getArea());

        // Chuyển đổi từ Amenity sang AmenityDto và gán vào roomDto
        List<AmenityDto> amenityDtos = room.getAmenities().stream()
                .map(this::convertToAmenityDto)
                .collect(Collectors.toList());
        roomDto.setAmenities(amenityDtos);

        return roomDto;
    }

    private AmenityDto convertToAmenityDto(Amenity amenity) {
        AmenityDto amenityDto = new AmenityDto();
        amenityDto.setId(amenity.getId());
        amenityDto.setName(amenity.getName());
        return amenityDto;
    }
    private Amenity convertToAmenity(AmenityDto amenityDto) {
        Amenity amenity = new Amenity();
        amenity.setId(amenityDto.getId());
        amenity.setName(amenityDto.getName());
        return amenity;
    }
    private String createUniqueFileName(String hotelName) {
        String seoFileName = CommonFunction.SEOUrl(hotelName);
        return seoFileName + "-" + UUID.randomUUID();
    }
    private Room findRoomById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }
}
