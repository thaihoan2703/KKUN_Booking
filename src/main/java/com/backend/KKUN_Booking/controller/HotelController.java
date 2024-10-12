package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<HotelDto> createHotel(
            @ModelAttribute HotelDto hotelDto,
            @RequestParam(value = "exteriorImageList", required = false) MultipartFile[] exteriorImageList,
            Principal principal) throws IOException {

        // Get the email or username of the authenticated user
        String userEmail = principal.getName();

        // Pass the data to the service layer
        HotelDto createdHotel = hotelService.createHotel(hotelDto, exteriorImageList, userEmail);

        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HotelDto> updateHotel(
            @PathVariable UUID id,
            @ModelAttribute HotelDto hotelDto,
            @RequestParam(value = "exteriorImageList", required = false) MultipartFile[] exteriorImageList,
            Principal principal) {

        // Lấy email hoặc username từ token JWT
        String userEmail = principal.getName(); // Thường là email hoặc username
        HotelDto updatedHotel = hotelService.updateHotel(id, hotelDto, exteriorImageList, userEmail);
        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deleteHotel(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
    }
}
