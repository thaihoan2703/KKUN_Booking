package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.AmenityDto;
import com.backend.KKUN_Booking.service.AmenityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @GetMapping
    public ResponseEntity<List<AmenityDto>> getAllAmenities() {
        List<AmenityDto> amenities = amenityService.getAllAmenities();
        return ResponseEntity.ok(amenities); // Trả về danh sách tiện nghi với trạng thái 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityDto> getAmenityById(@PathVariable UUID id) {
        AmenityDto amenity = amenityService.getAmenityById(id);
        return ResponseEntity.ok(amenity); // Trả về tiện nghi với trạng thái 200 OK
    }

    @PostMapping
    public ResponseEntity<AmenityDto> createAmenity(@RequestBody AmenityDto amenityDto) {
        AmenityDto createdAmenity = amenityService.createAmenity(amenityDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAmenity); // Trả về tiện nghi mới với trạng thái 201 Created
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmenityDto> updateAmenity(@PathVariable UUID id, @RequestBody AmenityDto amenityDto) {
        AmenityDto updatedAmenity = amenityService.updateAmenity(id, amenityDto);
        return ResponseEntity.ok(updatedAmenity); // Trả về tiện nghi đã cập nhật với trạng thái 200 OK
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable UUID id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
    }
}
