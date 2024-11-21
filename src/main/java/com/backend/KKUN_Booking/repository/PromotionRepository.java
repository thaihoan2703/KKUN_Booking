package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    // Có thể thêm các phương thức tìm kiếm tuỳ chỉnh nếu cần
    boolean existsByCode(String code);
}