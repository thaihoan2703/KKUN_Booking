package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.RoomDto;
import com.backend.KKUN_Booking.model.enumModel.AmenityType;
import com.backend.KKUN_Booking.model.enumModel.BedType;
import com.backend.KKUN_Booking.model.enumModel.RoomType;
import com.backend.KKUN_Booking.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    //    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
//        roomService.deleteRoom(id);
//        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
//    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        // Thêm log để kiểm tra roomId
        System.out.println("Room ID to delete: " + id);
        RoomDto room = roomService.getRoomById(id);

        try {
            if(room == null) {
                throw new EntityNotFoundException("Room not found");
            }
            roomService.deleteRoom(id);
            System.out.println("Room deleted successfully!");
        } catch (EntityNotFoundException e) {
            System.err.println("Error: Room not found. ID = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found nếu phòng không tồn tại
        } catch (Exception e) {
            System.err.println("Error while deleting room: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 nếu lỗi khác
        }

        return ResponseEntity.noContent().build(); // 204 No Content nếu xóa thành công
    }


    @GetMapping("hotel/{hotelId}")
    public ResponseEntity<?> getRoomsByHotelId(@PathVariable UUID hotelId){
        List<RoomDto> rooms = roomService.getRoomsByHotelId(hotelId);
        return ResponseEntity.ok(rooms); // Trả về danh sách phòng với trạng thái 200 OK
    }

    @GetMapping("/room-types")
    public ResponseEntity<List<Map<String, String>>> getRoomTypes() {
        List<Map<String, String>> roomTypes = Arrays.stream(RoomType.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(roomTypes);
    }

    @GetMapping("/bed-types")
    public ResponseEntity<List<Map<String, String>>> getBedTypes() {
        List<Map<String, String>> bedTypes = Arrays.stream(BedType.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bedTypes);
    }
}
