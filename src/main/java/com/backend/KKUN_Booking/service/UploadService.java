package com.backend.KKUN_Booking.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String saveImage(MultipartFile profileImage);
}
