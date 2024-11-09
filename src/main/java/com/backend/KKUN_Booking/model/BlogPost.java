package com.backend.KKUN_Booking.model;

import com.backend.KKUN_Booking.model.enumModel.BlogPostCategory;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;
    private int views;

    private int readTime;

    @Enumerated(EnumType.STRING)
    private BlogPostCategory blogPostCategory;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "blogPost",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogPostContent> contents;

    // getters và setters
}
