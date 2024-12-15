package com.backend.KKUN_Booking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String altImage;
    private String url;
}
