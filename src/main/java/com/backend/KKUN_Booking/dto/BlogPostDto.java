package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.BlogPostCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BlogPostDto {
    private UUID id;
    private String title;
    private int views;
    private LocalDateTime createdAt;
    private int readTime;
    private BlogPostCategory blogPostCategory;
    private String author;
    private List<BlogPostContentDto> contents;
    public String getBlogPostCategoryDisplayName() {
        return blogPostCategory != null ? blogPostCategory.getDisplayName() : null;
    }
}
