package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.AdminUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.CustomerUserDto;
import com.backend.KKUN_Booking.dto.abstractDto.UserAbstract.HotelOwnerUserDto;
import com.backend.KKUN_Booking.model.enumModel.UserStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Data
@NoArgsConstructor // Default constructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdminUserDto.class, name = "admin"),
        @JsonSubTypes.Type(value = CustomerUserDto.class, name = "customer"),
        @JsonSubTypes.Type(value = HotelOwnerUserDto.class, name = "hotelowner")
})
public abstract class UserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String alias;
    private LocalDateTime createdDate;
    private UserStatus status;
    private String password;
    private UUID roleId;

    // Thông tin xác thực
    private String authProvider; // "LOCAL" hoặc "GOOGLE"

    // Thông tin cho hệ thống đề xuất
    private List<String> preferredDestinations;
    private List<String> preferredAmenities;
    private String travelStyle;

    private List<String> recentSearches;
    private List<String> savedHotels;
}

