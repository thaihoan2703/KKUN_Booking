package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.model.Hotel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HotelService {
    List<HotelDto> getAllHotels();
    HotelDto getHotelById(UUID id);
    HotelDto createHotel(HotelDto hotelDto, MultipartFile[] exteriorImages, String userEmail); // Thêm tham số user
    HotelDto createHotelAndRooms(HotelDto hotelDto, MultipartFile[] exteriorImages,MultipartFile[] roomImages, String userEmail);
    HotelDto updateHotel(UUID id, HotelDto hotelDto,MultipartFile[] exteriorImages, String userEmail); // Thêm tham số user
    void deleteHotel(UUID id);
    List<HotelDto> findTopHotelsByRating(int limit);

    List<HotelDto> findTrendingDestinations(int limit);
    boolean checkRoomAvailability(UUID hotelId, LocalDateTime checkInDate);
    List<HotelDto> getHotelsWithAvailableRooms(LocalDateTime checkInDate);
}

