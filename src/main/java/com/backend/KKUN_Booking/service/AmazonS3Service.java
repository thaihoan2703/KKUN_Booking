package com.backend.KKUN_Booking.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.backend.KKUN_Booking.service.provider.AmazonS3.AWSProvider;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AmazonS3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private AWSProvider awsProvider;

    public String uploadFile(MultipartFile multipartFile, String seoUrl) {
        File file = convertMultiPartFileToFile(multipartFile);

        try {
            // Đọc file gốc
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                throw new IOException("Failed to read image from file");
            }

            // Giảm chất lượng ảnh xuống 0.7 (70%)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                Thumbnails.of(originalImage)
                        .size(originalImage.getWidth(), originalImage.getHeight())
                        .outputQuality(0.7)
                        .outputFormat("jpg") // Specify the output format
                        .toOutputStream(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error while processing image", e);
            }

            byte[] compressedImageData = outputStream.toByteArray();
            if (compressedImageData.length == 0) {
                throw new IOException("Compressed image data is empty");
            }

            // Lưu ảnh nén tạm thời vào file mới
            File compressedFile = new File("compressed_" + file.getName());
            Files.write(compressedFile.toPath(), compressedImageData);

            // Tạo tên file và upload lên S3
            String fileName = "images/" + new Date().getTime() + "_" + seoUrl;
            PutObjectRequest putObjectRequest = new PutObjectRequest(awsProvider.getBucket(), fileName, compressedFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);

            // Xóa các file tạm thời
            file.delete();
            compressedFile.delete();

            URL url = amazonS3.getUrl(awsProvider.getBucket(), fileName);
            return url.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while processing image file", e);
        }
    }



    public String uploadAvatarUserFile(MultipartFile multipartFile, String seoUrl) {
        File file = convertMultiPartFileToFile(multipartFile);

        String fileName = "images/users/" + new Date().getTime() + "_" + seoUrl;

        PutObjectRequest putObjectRequest = new PutObjectRequest(awsProvider.getBucket(), fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        amazonS3.putObject(putObjectRequest);
        file.delete();

        URL url = amazonS3.getUrl(awsProvider.getBucket(), fileName);
        return url.toString();
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }
    public S3ObjectInputStream getFile(String fileName) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(awsProvider.getBucket(), fileName));
        return s3Object.getObjectContent();
    }
    public String deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(awsProvider.getBucket(), fileName));
        return "File deleted successfully";
    }
    public List<String> listFiles() {
        ListObjectsV2Result result = amazonS3.listObjectsV2(awsProvider.getBucket());
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        List<String> fileNames = new ArrayList<>();
        for (S3ObjectSummary os : objects) {
            fileNames.add(os.getKey());
        }
        return fileNames;
    }
    public String getFileName(String url) {
        String[] parts = url.split("https://java-sb-stridestar.s3.ap-southeast-2.amazonaws.com/");
        return parts[parts.length - 1];
    }
}
