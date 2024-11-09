// UploadController.java
package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.ImageDto;
import com.backend.KKUN_Booking.service.implement.UploadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private UploadServiceImpl uploadService;

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<ImageDto> createImageUrl(@RequestParam("fileImage") MultipartFile file,
                                                   @RequestParam("altImage") String altImage) throws IOException {

        // Upload file and get URL
        String url = uploadService.saveImage(file);

        // Prepare the ImageDto response
        ImageDto imageDto = new ImageDto();
        imageDto.setAltImage(altImage);
        imageDto.setUrl(url);

        return new ResponseEntity<>(imageDto, HttpStatus.CREATED);
    }
}
