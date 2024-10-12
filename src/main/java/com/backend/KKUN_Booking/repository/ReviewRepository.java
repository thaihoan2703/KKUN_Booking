package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Review;
import com.backend.KKUN_Booking.model.reviewAbstract.RoomReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<RoomReview> findByRoomId(UUID roomId);

}
