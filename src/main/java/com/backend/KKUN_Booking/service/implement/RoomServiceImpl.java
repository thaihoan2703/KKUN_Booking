package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.RoomRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.AmazonS3Service;
import com.backend.KKUN_Booking.service.RoomService;
import com.backend.KKUN_Booking.util.CommonFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, HotelRepository hotelRepository, UserRepository userRepository, AmazonS3Service amazonS3Service) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
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
    public RoomDto createRoom(RoomDto roomDto, MultipartFile[] roomImages, String userEmail) {
        User user = findUserByEmail(userEmail);
        validateUserRole(user);

        List<String> roomImageUrls = uploadRoomImages(roomImages, roomDto);
        Room room = convertToEntity(roomDto, roomImageUrls);

        return convertToDto(roomRepository.save(room));
    }

    @Override
    public RoomDto updateRoom(UUID id, RoomDto roomDto, MultipartFile[] newRoomImages, String userEmail) {
        Room room = findRoomById(id);
        updateRoomDetails(room, roomDto);
        updateRoomImages(room, newRoomImages, roomDto);

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
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());

        if (roomDto.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            room.setHotel(hotel);
        }
    }

    private void updateRoomImages(Room room, MultipartFile[] newRoomImages, RoomDto roomDto) {
        if (newRoomImages != null && newRoomImages.length > 0) {
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

    private Room convertToEntity(RoomDto roomDto, List<String> roomImageUrls) {
        Room room = new Room();
        room.setId(roomDto.getId());
        room.setType(roomDto.getType());
        room.setCapacity(roomDto.getCapacity());
        room.setBasePrice(roomDto.getBasePrice());
        room.setAvailable(roomDto.isAvailable());
        room.setRoomImages(roomImageUrls);

        if (roomDto.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            room.setHotel(hotel);
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
        return roomDto;
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
