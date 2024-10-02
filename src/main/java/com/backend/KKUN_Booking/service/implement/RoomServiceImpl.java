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
import com.backend.KKUN_Booking.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomServiceImpl(RoomRepository roomRepository, HotelRepository hotelRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
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
    public RoomDto createRoom(RoomDto roomDto , String userEmail) {
        // Tìm người dùng theo email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra người dùng có vai trò HotelOwner hay không
        if (!user.getRole().getName().equals(RoleUser.HOTELOWNER.name())) {
            throw new IllegalArgumentException("User is not a hotel owner");
        }

        Room room = convertToEntity(roomDto);
        return convertToDto(roomRepository.save(room));
    }

    @Override
    public RoomDto updateRoom(UUID id, RoomDto roomDto , String userEmail) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
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

        return convertToDto(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }

    private RoomDto convertToDto(Room room) {
        // Convert Room entity to RoomDto
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setType(room.getType());
        roomDto.setCapacity(room.getCapacity());
        roomDto.setBasePrice(room.getBasePrice());
        roomDto.setAvailable(room.getAvailable());
        roomDto.setHotelId(room.getHotel().getId()); // Thêm hotelId

        return roomDto;
    }

    private Room convertToEntity(RoomDto roomDto) {
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
}
