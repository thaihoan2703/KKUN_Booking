package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.ReviewDto;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    List<ReviewDto> getAllReviews();
    ReviewDto getReviewById(UUID id);
    List<ReviewDto>  getReviewsByRoomId(UUID roomId);
    ReviewDto createReview(UUID bookingId, ReviewDto reviewDto);
    ReviewDto updateReview(UUID id, ReviewDto reviewDto);
    void deleteReview(UUID id);

}
