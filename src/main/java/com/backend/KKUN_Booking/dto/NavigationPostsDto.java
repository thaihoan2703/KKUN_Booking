package com.backend.KKUN_Booking.dto;

import lombok.Data;

@Data
public class NavigationPostsDto {
    private BlogPostDto prevPost;
    private BlogPostDto nextPost;
    // getters và setters
}
