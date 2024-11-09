package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.BedType;
import com.backend.KKUN_Booking.model.enumModel.RoomType;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoomDto {
    private UUID id;
    private RoomType type;
    @JsonGetter("typeDisplayName")
    public String getTypeDisplayName() {
        return type != null ? type.getDisplayName() : null;
    }
    private BedType bedType; // Loại giường, ví dụ: "King", "Queen"
    private Integer bedCount; // Số lượng giường
    private Double area;      // Diện tích phòng

    private int capacity;
    private BigDecimal basePrice;
    private boolean available;
    private UUID hotelId;   // Foreign key to hotel
    private List<String> roomImages;
    private List<AmenityDto> amenities;
    private int numOfReviews;
    // Getters and Setters
    // Getter tùy chỉnh cho displayName của bedType
    @JsonGetter("bedTypeDisplayName")
    public String getBedTypeDisplayName() {
        return bedType != null ? bedType.getDisplayName() : null;
    }


}
