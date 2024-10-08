package com.backend.KKUN_Booking.dto.abstractDto.UserAbstract;

import com.backend.KKUN_Booking.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("hotelowner") // Specify the type for Jackson
public class HotelOwnerUserDto extends UserDto {
    private UUID hotelId;
    private String hotelName;
}