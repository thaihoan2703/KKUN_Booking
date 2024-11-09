package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BlogPostDto;
import com.backend.KKUN_Booking.dto.NavigationPostsDto;
import com.backend.KKUN_Booking.model.enumModel.BlogContentType;
import com.backend.KKUN_Booking.model.enumModel.BlogPostCategory;
import com.backend.KKUN_Booking.model.enumModel.PaymentPolicy;
import com.backend.KKUN_Booking.service.BlogPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogPostService blogPostService;

    @PostMapping("/create")
    public ResponseEntity<BlogPostDto> createBlogPost(@RequestBody BlogPostDto blogPostDTO, Principal principal) {
        String userEmail = principal.getName();
        BlogPostDto savedPost = blogPostService.saveBlogPost(blogPostDTO, userEmail);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostDto> getBlogPost(@PathVariable UUID id) {

        BlogPostDto blogPostDTO = blogPostService.getBlogPostById(id);
        return new ResponseEntity<>(blogPostDTO, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BlogPostDto>> getAllBlogPosts() {
        List<BlogPostDto> blogPosts = blogPostService.getAllBlogPosts();
        return new ResponseEntity<>(blogPosts, HttpStatus.OK);
    }

    @GetMapping("/blog-categories")
    public ResponseEntity<List<Map<String, String>>> getBlogCategories() {
        List<Map<String, String>> bedTypes = Arrays.stream(BlogPostCategory.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bedTypes);
    }
    @GetMapping("/blog-content-types")
    public ResponseEntity<List<Map<String, String>>> getBlogContentTypes() {
        List<Map<String, String>> bedTypes = Arrays.stream(BlogContentType.values())
                .map(type -> Map.of("value", type.name(), "label", type.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bedTypes);
    }
    @GetMapping("/{postId}/navigation")
    public ResponseEntity<NavigationPostsDto> getNavigationPosts(@PathVariable UUID postId) {
        NavigationPostsDto navigationPosts = blogPostService.getNavigationPosts(postId);
        return new ResponseEntity<>(navigationPosts, HttpStatus.OK);
    }
}
