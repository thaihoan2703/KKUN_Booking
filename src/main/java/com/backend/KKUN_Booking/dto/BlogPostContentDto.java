package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.BlogContentType;
import lombok.Data;

import java.util.UUID;

@Data
public class BlogPostContentDto {
    private UUID id;
    private BlogContentType type;
    private String content; // Dùng cho đoạn văn và trích dẫn
    private String imageUrl; // Dùng cho hình ảnh
    private String authorQuote; // Tác giả của trích dẫn (nếu có)
    private int position; // Vị trí của nội dung trong bài viết
}
