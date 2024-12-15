package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.RoomDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<RoomDto> getAllRooms();
    RoomDto getRoomById(UUID id);
    RoomDto createRoom(RoomDto roomDto, MultipartFile[] roomImages, String userEmail);
    RoomDto updateRoom(UUID id, RoomDto roomDto, MultipartFile[] roomImages, String userEmail);
    List<RoomDto> findAvailableRooms(UUID hotelId, LocalDateTime checkinDate, LocalDateTime checkoutDate);
    List<RoomDto> getRoomsByHotelId(UUID hotelId);
    void deleteRoom(UUID id);
}

