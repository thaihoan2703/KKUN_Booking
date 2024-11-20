package com.backend.KKUN_Booking.controller;


import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.HotelSearchResultDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.security.UserDetailsImpl;
import com.backend.KKUN_Booking.service.HotelService;
import com.backend.KKUN_Booking.service.SearchService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import software.amazon.ion.Decimal;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Autowired
    private HotelService hotelService;
    @Autowired
    private UserService userService;
    @Autowired
    private SearchService searchService;

    @GetMapping("/hotels")
    public ResponseEntity<List<HotelSearchResultDto>> searchHotels(
            @RequestParam String location,
            @RequestParam LocalDateTime checkInDate,
            @RequestParam LocalDateTime checkOutDate,
            @RequestParam int guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) Double  rating,
            @RequestParam(required = false) Boolean freeCancellation,
            @RequestParam(required = false) Boolean breakfastIncluded,
            @RequestParam(required = false) Boolean prePayment
    ) {
        List<HotelSearchResultDto> results = searchService.searchHotels(
                location, checkInDate, checkOutDate, guests,
                minPrice, maxPrice, amenities, rating,
                freeCancellation, breakfastIncluded, prePayment);
        return ResponseEntity.ok(results);
    }
    @GetMapping("/hotels/search-by-name")
    public ResponseEntity<List<HotelSearchResultDto>> searchHotelsByName(
            @RequestParam LocalDateTime checkInDate,
            @RequestParam LocalDateTime checkOutDate,
            @RequestParam int guests,
            @RequestParam String hotelName
    ) {
        List<HotelSearchResultDto> results = searchService.searchHotelsByName(
                checkInDate, checkOutDate, guests,hotelName);
            return ResponseEntity.ok(results);
    }


    @PostMapping("/add-recent-searches")
    public ResponseEntity<String> addRecentSearch(
            @RequestParam String searchTerm,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        try {
            // Kiểm tra người dùng có đăng nhập hay không
            if (principal != null) {
                UUID userId = principal.getId();
                userService.addRecentSearch(userId, searchTerm);
                return ResponseEntity.ok("Recent search added successfully.");
            }

            // Trường hợp không đăng nhập, trả về thông báo nhưng không thực hiện lưu
            return ResponseEntity.ok("No recent search added (user not logged in).");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
