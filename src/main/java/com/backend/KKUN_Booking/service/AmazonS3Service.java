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
        if (file == null) {
            throw new RuntimeException("Failed to convert MultipartFile to File");
        }

        try {
            // Đọc file gốc
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                throw new IOException("Failed to read image from file");
            }

            // Giảm chất lượng ảnh xuống 0.7 (70%)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(originalImage.getWidth(), originalImage.getHeight())
                    .outputQuality(0.7)
                    .outputFormat("jpg") // Specify the output format
                    .toOutputStream(outputStream);

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

            // Trả về URL của file đã upload
            return amazonS3.getUrl(awsProvider.getBucket(), fileName).toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while processing image file", e);
        }
    }
    public String uploadRoomFile(MultipartFile multipartFile,String hotelIdToString, String seoUrl) {
        File file = convertMultiPartFileToFile(multipartFile);
        if (file == null) {
            throw new RuntimeException("Failed to convert MultipartFile to File");
        }

        try {
            // Đọc file gốc
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                throw new IOException("Failed to read image from file");
            }

            // Giảm chất lượng ảnh xuống 0.7 (70%)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(originalImage.getWidth(), originalImage.getHeight())
                    .outputQuality(0.7)
                    .outputFormat("jpg") // Specify the output format
                    .toOutputStream(outputStream);

            byte[] compressedImageData = outputStream.toByteArray();
            if (compressedImageData.length == 0) {
                throw new IOException("Compressed image data is empty");
            }

            // Lưu ảnh nén tạm thời vào file mới
            File compressedFile = new File("compressed_" + file.getName());
            Files.write(compressedFile.toPath(), compressedImageData);

            // Tạo tên file và upload lên S3
            String fileName = "images/" + hotelIdToString + "/" + new Date().getTime() + "_" + seoUrl;
            PutObjectRequest putObjectRequest = new PutObjectRequest(awsProvider.getBucket(), fileName, compressedFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest);

            // Xóa các file tạm thời
            file.delete();
            compressedFile.delete();

            // Trả về URL của file đã upload
            return amazonS3.getUrl(awsProvider.getBucket(), fileName).toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while processing image file", e);
        }
    }
    public String uploadAvatarUserFile(MultipartFile multipartFile, String seoUrl) {
        File file = convertMultiPartFileToFile(multipartFile);
        if (file == null) {
            throw new RuntimeException("Failed to convert MultipartFile to File");
        }

        try {
            String fileName = "images/users/" + new Date().getTime() + "_" + seoUrl;
            PutObjectRequest putObjectRequest = new PutObjectRequest(awsProvider.getBucket(), fileName, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(putObjectRequest);

            // Xóa file tạm thời
            file.delete();

            return amazonS3.getUrl(awsProvider.getBucket(), fileName).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while uploading avatar", e);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        try {
            File convertedFile = new File(file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
                fos.write(file.getBytes());
            }
            return convertedFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public S3ObjectInputStream getFile(String fileName) {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(awsProvider.getBucket(), fileName));
        return s3Object.getObjectContent();
    }

    public String deleteFile(String fileUrl) {
        String fileName = getFileName(fileUrl);
        if (fileName == null) {
            throw new RuntimeException("Invalid file URL");
        }

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(awsProvider.getBucket(), fileName));
            return "File deleted successfully";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
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
        // Phân tách URL thành các phần
        String[] parts = url.split("/");
        String bucketName = awsProvider.getBucket();

        // Tìm kiếm tên tệp trong URL
        for (String part : parts) {
            if (part.startsWith(bucketName)) {
                // Lấy phần sau dấu "/" để lấy tên tệp
                return url.substring(url.indexOf(part) + part.length() + 1); // Thêm 1 để loại bỏ dấu "/"
            }
        }

        return null; // Nếu không tìm thấy
    }
}
