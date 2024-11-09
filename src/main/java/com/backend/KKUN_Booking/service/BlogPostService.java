package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.BlogPostDto;
import com.backend.KKUN_Booking.dto.NavigationPostsDto;

import java.util.List;
import java.util.UUID;

public interface BlogPostService {
    BlogPostDto saveBlogPost(BlogPostDto blogPostDTO, String userEmail);
    BlogPostDto getBlogPostById(UUID id);
    List<BlogPostDto> getAllBlogPosts();
    NavigationPostsDto getNavigationPosts(UUID postId);
}
