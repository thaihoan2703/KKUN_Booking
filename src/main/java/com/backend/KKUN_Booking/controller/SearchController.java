package com.backend.KKUN_Booking.controller;


import com.backend.KKUN_Booking.dto.HotelDto;
import com.backend.KKUN_Booking.dto.HotelSearchResultDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.service.HotelService;
import com.backend.KKUN_Booking.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
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
    private SearchService searchService;

    @GetMapping("/hotels")
    public ResponseEntity<List<HotelSearchResultDto>> searchHotels(@RequestParam String location,
                                                                   @RequestParam LocalDateTime checkInDate, @RequestParam LocalDateTime checkOutDate,
                                                                   @RequestParam int guests, Principal principal) {

        List<HotelSearchResultDto> results = searchService.searchHotels(location, checkInDate, checkOutDate, guests);
        return ResponseEntity.ok(results);
    }
}
