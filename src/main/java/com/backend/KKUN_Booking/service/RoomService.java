package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.RoomDto;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<RoomDto> getAllRooms();
    RoomDto getRoomById(UUID id);
    RoomDto createRoom(RoomDto roomDto, String userEmail);
    RoomDto updateRoom(UUID id, RoomDto roomDto, String userEmail);
    void deleteRoom(UUID id);
}

