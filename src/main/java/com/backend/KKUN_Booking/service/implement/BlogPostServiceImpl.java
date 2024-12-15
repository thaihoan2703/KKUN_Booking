package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.BlogPostContentDto;
import com.backend.KKUN_Booking.dto.BlogPostDto;
import com.backend.KKUN_Booking.dto.NavigationPostsDto;
import com.backend.KKUN_Booking.model.BlogPost;
import com.backend.KKUN_Booking.model.BlogPostContent;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.repository.BlogPostContentRepository;
import com.backend.KKUN_Booking.repository.BlogPostRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.BlogPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlogPostServiceImpl implements BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogPostContentRepository blogPostContentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public BlogPostDto saveBlogPost(BlogPostDto blogPostDto, String userEmail) {
        // Tìm User (Author) từ userEmail
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + userEmail));

        BlogPost blogPost = convertToEntity(blogPostDto);
        blogPost.setAuthor(author); // Thiết lập tác giả cho bài viết
        BlogPost savedPost = blogPostRepository.save(blogPost);

        // Lưu các phần nội dung của bài viết và liên kết với bài viết
        for (BlogPostContent content : savedPost.getContents()) {
            content.setBlogPost(savedPost);
            blogPostContentRepository.save(content);
        }

        return convertToDto(savedPost);
    }

    @Override
    public BlogPostDto getBlogPostById(UUID id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found"));
        return convertToDto(blogPost);
    }

    @Override
    public List<BlogPostDto> getAllBlogPosts() {
        List<BlogPost> blogPosts = blogPostRepository.findAll();
        return blogPosts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public NavigationPostsDto getNavigationPosts(UUID postId) {
        // Lấy bài viết hiện tại
        BlogPost currentPost = blogPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Tìm bài viết trước (bài viết có createdAt nhỏ hơn)
        BlogPost prevPost = blogPostRepository.findTopByCreatedAtLessThanOrderByCreatedAtDesc(currentPost.getCreatedAt())
                .orElse(null);

        // Tìm bài viết sau (bài viết có createdAt lớn hơn)
        BlogPost nextPost = blogPostRepository.findTopByCreatedAtGreaterThanOrderByCreatedAtAsc(currentPost.getCreatedAt())
                .orElse(null);

        // Chuyển đổi các bài viết này thành DTO
        BlogPostDto prevPostDto = prevPost != null ? convertToDto(prevPost) : null;
        BlogPostDto nextPostDto = nextPost != null ? convertToDto(nextPost) : null;

        // Tạo và trả về NavigationPostsDto
        NavigationPostsDto navigationPosts = new NavigationPostsDto();
        navigationPosts.setPrevPost(prevPostDto);
        navigationPosts.setNextPost(nextPostDto);
        return navigationPosts;
    }

    private BlogPost convertToEntity(BlogPostDto blogPostDTO) {
        BlogPost blogPost = new BlogPost();
        blogPost.setTitle(blogPostDTO.getTitle());
        blogPost.setViews(blogPostDTO.getViews());
        blogPost.setCreatedAt(LocalDateTime.now());
        blogPost.setReadTime(blogPostDTO.getReadTime());
        blogPost.setBlogPostCategory(blogPostDTO.getBlogPostCategory());

        // Chuyển đổi BlogPostContentDTO sang BlogPostContent
        List<BlogPostContent> contents = blogPostDTO.getContents().stream()
                .map(contentDTO -> {
                    BlogPostContent content = new BlogPostContent();
                    content.setType(contentDTO.getType());
                    content.setContent(contentDTO.getContent());
                    content.setImageUrl(contentDTO.getImageUrl());
                    content.setAuthorQuote(contentDTO.getAuthorQuote());
                    content.setPosition(contentDTO.getPosition());
                    content.setBlogPost(blogPost);  // Thiết lập liên kết với BlogPost
                    return content;
                }).collect(Collectors.toList());

        blogPost.setContents(contents);

        return blogPost;
    }

    // Chuyển đổi từ BlogPost (Entity) sang BlogPostDTO
    private BlogPostDto convertToDto(BlogPost blogPost) {
        BlogPostDto blogPostDTO = new BlogPostDto();
        blogPostDTO.setId(blogPost.getId());
        blogPostDTO.setTitle(blogPost.getTitle());
        blogPostDTO.setViews(blogPost.getViews());
        blogPostDTO.setCreatedAt(blogPost.getCreatedAt());
        blogPostDTO.setReadTime(blogPost.getReadTime());
        blogPostDTO.setBlogPostCategory(blogPost.getBlogPostCategory());

        // Chỉ lưu tên của tác giả (ví dụ: lastName)
        if (blogPost.getAuthor() != null) {
            blogPostDTO.setAuthor(blogPost.getAuthor().getFirstName() + " " +blogPost.getAuthor().getLastName());
        }

        // Chuyển đổi BlogPostContent sang BlogPostContentDTO
        List<BlogPostContentDto> contentDTOs = blogPost.getContents().stream()
                .map(content -> {
                    BlogPostContentDto contentDTO = new BlogPostContentDto();
                    contentDTO.setId(content.getId());
                    contentDTO.setType(content.getType());
                    contentDTO.setContent(content.getContent());
                    contentDTO.setImageUrl(content.getImageUrl());
                    contentDTO.setAuthorQuote(content.getAuthorQuote());
                    contentDTO.setPosition(content.getPosition());
                    return contentDTO;
                })
                .collect(Collectors.toList());

        blogPostDTO.setContents(contentDTOs);
        return blogPostDTO;
    }

}
