package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần
    Optional<BlogPost> findTopByCreatedAtLessThanOrderByCreatedAtDesc(LocalDateTime createdAt);
    Optional<BlogPost> findTopByCreatedAtGreaterThanOrderByCreatedAtAsc(LocalDateTime createdAt);
}
