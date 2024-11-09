// UploadServiceImpl.java
package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.ImageDto;
import com.backend.KKUN_Booking.service.AmazonS3Service;
import com.backend.KKUN_Booking.service.UploadService;
import com.backend.KKUN_Booking.util.CommonFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private AmazonS3Service amazonS3Service;

    public String saveImage(MultipartFile profileImage) {
        String s3ImageUrl = "";
        if (profileImage != null && !profileImage.isEmpty()) {
            String uniqueFileName = CommonFunction.generateUniqueFileName(profileImage.getOriginalFilename());
            s3ImageUrl = amazonS3Service.uploadFile(profileImage, uniqueFileName);
        }
        return s3ImageUrl;
    }
}
