package com.backend.KKUN_Booking.controller;


import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.HotelSearchResultDto;
import com.backend.KKUN_Booking.dto.UserDto;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<List<HotelSearchResultDto>> searchHotels(@RequestParam String location,
                                                                   @RequestParam LocalDateTime checkInDate, @RequestParam LocalDateTime checkOutDate,
                                                                   @RequestParam int roomQty,
                                                                   @RequestParam int guests) {

        List<HotelSearchResultDto> results = searchService.searchHotels(location, checkInDate, checkOutDate, guests);
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
