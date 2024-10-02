package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    Optional<Role> findById(UUID id);
}
