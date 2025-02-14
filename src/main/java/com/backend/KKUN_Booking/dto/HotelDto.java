package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.HotelCategory;
import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Data
public class HotelDto {
    private UUID id;
    private String name;
    private HotelCategory category;
    private String description;
    private Double rating;
    private String location;
    private Boolean freeCancellation;
    private Boolean breakfastIncluded;
    private Boolean prePayment;
    private PaymentPolicy paymentPolicy;
    private List<String> exteriorImages;
    private List<String> roomImages;
    private List<RoomDto> rooms;  // List of room details
    private List<AmenityDto> amenities;
    private int numOfReviews;
    private String ownerName;
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : null;
    }
    // Getters and Setters
}

