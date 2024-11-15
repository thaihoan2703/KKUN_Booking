package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.HotelSearchResultDto;
import org.springframework.web.bind.annotation.RequestParam;
import software.amazon.ion.Decimal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SearchService {
    List<HotelSearchResultDto> searchHotels(String location, LocalDateTime checkInDate, LocalDateTime checkOutDate, int guests,
                                            BigDecimal minPrice, BigDecimal maxPrice, List<String> amenities, Double  rating,
                                            Boolean freeCancellation, Boolean breakfastIncluded, Boolean prePayment);

    List<HotelSearchResultDto> searchHotelsByName( LocalDateTime checkInDate, LocalDateTime checkOutDate, int guests,
                                                   String location);
}
