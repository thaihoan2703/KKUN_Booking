package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.ReviewDto;
import com.backend.KKUN_Booking.dto.UserDto;
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
    @GetMapping("/rooms/{id}")
    public ResponseEntity<List<ReviewDto> > getReviewByRoomId(@PathVariable UUID id) {
        List<ReviewDto> reviews = reviewService.getReviewsByRoomId(id);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<?> createReviewForBooking(
            @PathVariable UUID bookingId,
            @RequestBody ReviewDto reviewDto,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập để đánh giá đặt phòng.");
        }
        // Lấy email của người dùng từ Principal
        String userEmail = principal.getName();
        Optional<User> user = userRepository.findByEmail(userEmail);

        // Kiểm tra xem người dùng có tồn tại không
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng không tồn tại hoặc không được phép thực hiện hành động này.");
        }

        // Thiết lập thông tin user trong reviewDto
        UserDto userDto = new UserDto(){};
        userDto.setId(user.get().getId());
        reviewDto.setUser(userDto);

        // Lấy thông tin đặt phòng
        BookingDto bookingDto = bookingService.getBookingById(bookingId);
        if (bookingDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn đặt phòng không tồn tại.");
        }

        // Kiểm tra quyền truy cập: chỉ cho phép đánh giá nếu userId trong booking khớp với user hiện tại
        if (!bookingDto.getUserId().equals(userDto.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không được phép đánh giá đặt phòng này.");
        }

        // Kiểm tra trạng thái của booking để xác định có thể đánh giá hay không
        if (!bookingDto.isReviewed() && bookingDto.getStatus() == BookingStatus.CONFIRMED) {
            ReviewDto createdReview = reviewService.createReview(bookingId, reviewDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đơn này đã được đánh giá hoặc chưa xác nhận.");
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