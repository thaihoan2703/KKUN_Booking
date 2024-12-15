package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.model.enumModel.BlogContentType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class BlogPostContent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "blog_post_id", nullable = false)
    private BlogPost blogPost;

    private BlogContentType type; // "paragraph", "image", "quote"

    @Column(columnDefinition = "TEXT")
    private String content; // Dùng cho đoạn văn và trích dẫn
    private String imageUrl; // Dùng cho hình ảnh
    private String authorQuote; // Tác giả của trích dẫn (nếu có)
    private int position; // Vị trí của nội dung trong bài viết

    // getters và setters
}
