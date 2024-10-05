package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.ReviewDto;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.ReviewService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;
    private  final UserRepository userRepository;
    @Autowired
    public ReviewController(ReviewService reviewService, BookingService bookingService,UserRepository userRepository) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable UUID id) {
        ReviewDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewDto> createReviewForBooking(@PathVariable UUID bookingId, @RequestBody ReviewDto reviewDto, Principal principal) {
        String userEmail = principal.getName();
        Optional<User> user = userRepository.findByEmail(userEmail);
        user.ifPresent(value -> reviewDto.setUserId(value.getId()));

        BookingDto bookingDto = bookingService.getBookingById(bookingId);
        if (bookingDto == null) {
            return ResponseEntity.notFound().build();
        }

        if (!bookingDto.isReviewed()  && bookingDto.getStatus() == BookingStatus.CONFIRMED) {
            ReviewDto createdReview = reviewService.createReview(bookingId,reviewDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable UUID id, @RequestBody ReviewDto reviewDto) {
        ReviewDto updatedReview = reviewService.updateReview(id, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}