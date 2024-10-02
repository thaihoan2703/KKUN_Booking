package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public List<HotelDto> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    public HotelDto getHotelById(@PathVariable UUID id) {
        return hotelService.getHotelById(id);
    }

    @PostMapping
    public ResponseEntity<HotelDto> createHotel(@RequestBody HotelDto hotelDto, Principal principal) {
        // Lấy email hoặc username
        String userEmail = principal.getName();  // Lấy email hoặc username từ authentication

        // Truyền thông tin user qua Service để tạo hotel
        HotelDto createdHotel = hotelService.createHotel(hotelDto, userEmail);
        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDto> updateHotel(@PathVariable UUID id, @RequestBody HotelDto hotelDto,Principal principal ) {
        // Lấy email hoặc username từ token JWT
        String userEmail = principal.getName(); // Thường là email hoặc username
        HotelDto updatedHotel = hotelService.updateHotel(id, hotelDto,userEmail);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public void deleteHotel(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
    }
}
