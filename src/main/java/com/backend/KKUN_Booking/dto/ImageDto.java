package com.backend.KKUN_Booking.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageDto {
    private UUID id;
    private String altImage;
    private String url;
}
