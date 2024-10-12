package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        List<RoomDto> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms); // Trả về danh sách phòng với trạng thái 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable UUID id) {
        RoomDto room = roomService.getRoomById(id);
        return ResponseEntity.ok(room); // Trả về phòng với trạng thái 200 OK
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<RoomDto> createRoom(@ModelAttribute RoomDto roomDto,
                                              @RequestParam(value = "roomImageList", required = false) MultipartFile[] roomImageList, Principal principal) {
        // Lấy email hoặc username
        String userEmail = principal.getName();  // Lấy email hoặc username từ authentication
        RoomDto createdRoom = roomService.createRoom(roomDto,roomImageList, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom); // Trả về phòng mới với trạng thái 201 Created
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomDto> updateRoom(@PathVariable UUID id, @ModelAttribute RoomDto roomDto,
                                              @RequestParam(value = "roomImageList", required = false) MultipartFile[] roomImageList, Principal principal) {
        // Lấy email hoặc username
        String userEmail = principal.getName();  // Lấy email hoặc username từ authentication
        RoomDto updatedRoom = roomService.updateRoom(id, roomDto, roomImageList, userEmail);
        return ResponseEntity.ok(updatedRoom); // Trả về phòng đã cập nhật với trạng thái 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
    }
}
