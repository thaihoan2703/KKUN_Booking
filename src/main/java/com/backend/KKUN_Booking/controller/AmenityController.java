package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.AmenityDto;
import com.backend.KKUN_Booking.model.enumModel.AmenityType;
import com.backend.KKUN_Booking.service.AmenityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @GetMapping("/for-room")
    public ResponseEntity<List<AmenityDto>> getAllAmenitiesForRoom() {
        List<AmenityDto> amenities = amenityService.getAllAmenities()
                .stream()
                .filter(amenity -> AmenityType.BASIC.equals(amenity.getAmenityType()) || AmenityType.COMFORT.equals(amenity.getAmenityType()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(amenities); // Trả về danh sách tiện nghi đã lọc với trạng thái 200 OK
    }
    @GetMapping("/amenity-types")
    public ResponseEntity<List<Map<String, String>>> getAmenities() {
        List<Map<String, String>> amenities = Arrays.stream(AmenityType.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(amenities);
    }



    @GetMapping("/{id}")
    public ResponseEntity<AmenityDto> getAmenityById(@PathVariable UUID id) {
        AmenityDto amenity = amenityService.getAmenityById(id);
        return ResponseEntity.ok(amenity); // Trả về tiện nghi với trạng thái 200 OK
    }

    @PostMapping
    public ResponseEntity<?> createAmenity(@RequestBody AmenityDto amenityDto) {
        try {
            AmenityDto createdAmenity = amenityService.createAmenity(amenityDto);
            return ResponseEntity.ok(createdAmenity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
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
