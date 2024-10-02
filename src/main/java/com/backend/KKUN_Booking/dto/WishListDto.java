package com.backend.KKUN_Booking.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WishListDto {
    private UUID id;
    private UUID userId;
    private UUID roomId;

    // Getter và Setter
}
