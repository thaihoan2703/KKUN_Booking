package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Có thể thêm các phương thức truy vấn tùy chỉnh tại đây
    Optional<User> findByEmail(String email);
    // Phương thức kiểm tra email đã tồn tại hay chưa
    boolean existsByEmail(String email);

}

