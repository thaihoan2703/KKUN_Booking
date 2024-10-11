package com.backend.KKUN_Booking.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return ""; // Trả về chuỗi rỗng hoặc null nếu danh sách rỗng
        }
        return String.join(",", attribute); // Chuyển đổi danh sách thành chuỗi với dấu phẩy
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Arrays.asList(); // Trả về danh sách rỗng
        }
        return Arrays.asList(dbData.split(",")); // Chia chuỗi thành danh sách dựa trên dấu phẩy
    }
}

