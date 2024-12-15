package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.BlogPostContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlogPostContentRepository extends JpaRepository<BlogPostContent, UUID> {
    List<BlogPostContent> findByBlogPostIdOrderByPosition(UUID blogPostId);
}
