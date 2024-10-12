package com.backend.KKUN_Booking.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CommonFunction {

    @Autowired
    private static StringRedisTemplate redisTemplate;

    public static String SEOUrl(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String seoFileName = pattern.matcher(normalized).replaceAll("");
        seoFileName = seoFileName.toLowerCase();
        seoFileName = seoFileName.replaceAll("[^a-z0-9\\-]", "-");
        seoFileName = seoFileName.replaceAll("-+", "-"); // Loại bỏ dấu gạch ngang thừa
        return seoFileName;
    }
    // Hàm loại bỏ dấu và ký tự đặc biệt
    private static String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replaceAll("[^a-zA-Z0-9]", "");
    }
    public static String saveFile(String seoUrl, String directory, MultipartFile file) throws IOException {
        File saveFile = new File("src/main/resources/static/images");
        Path uploadPath =  Paths.get(saveFile + directory);

        try {
            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = file.getInputStream()) {
                String extension = "";
                String fileName = file.getOriginalFilename();

                if(fileName != null) {
                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        extension = fileName.substring(i+1);
                    }
                    seoUrl = seoUrl + "." + extension;
                    Path filePath = uploadPath.resolve(seoUrl);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                return seoUrl;
            } catch (IOException e) {
                throw new IOException("Could not save uploaded file: " + seoUrl, e);
            }
        } catch (IOException e) {
            throw new IOException("Could not create directory: " + uploadPath, e);
        }
    }
    public static String saveAliasAccount(String fullName){
        fullName = fullName.toLowerCase();
        String normalized = Normalizer.normalize(fullName, Normalizer.Form.NFD);    // Loại bỏ các ký tự dấu
        normalized = normalized.replaceAll("\\p{M}", "");
        String baseAlias = normalized.replaceAll("\\s+", "-"); // Replace spaces with hyphens
        String randomDigits = String.format("%03d", new Random().nextInt(1000)); // Generate 3 random digits
        String alias = baseAlias + "-" + randomDigits;
        return alias;
    }
    public static String getTransactionReferenceCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static String generateUniqueTransactionReference() {
        try {
            String transactionReference;

            // Try to set the transaction reference in Redis with a TTL of 24 hours
            do {
                // Combine an 8-digit random number with the current timestamp (last 5 digits of milliseconds)
                String suffix = VNPayUtil.getRandomNumber(8);
                String timeComponent = String.valueOf(System.currentTimeMillis() % 100000); // Last 5 digits of the timestamp
                transactionReference = suffix + timeComponent;

            } while (!redisTemplate.opsForValue().setIfAbsent(
                    "txn_ref:" + transactionReference, "1", 12, TimeUnit.HOURS));

            return transactionReference;
        } catch (Exception e) {
            // Fallback if Redis is not available, use just an 8-digit random number
            return VNPayUtil.getRandomNumber(8);
        }
    }
    // Hàm tạo alias từ firstName và lastName
    public static String generateAlias(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("Tên và họ không được để trống");
        }

        // Kết hợp firstName và lastName, loại bỏ dấu và ký tự đặc biệt
        String baseAlias = normalizeString(firstName + "." + lastName);

        // Thêm số ngẫu nhiên để đảm bảo alias là duy nhất
        Random random = new Random();
        int randomNum = random.nextInt(1000); // Số ngẫu nhiên từ 0 đến 999

        // Tạo alias hoàn chỉnh
        return baseAlias.toLowerCase(Locale.ROOT) + "-" + randomNum;
    }


}
