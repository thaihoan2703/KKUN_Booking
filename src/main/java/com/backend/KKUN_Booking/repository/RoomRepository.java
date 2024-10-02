package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<Room> findByHotelId(UUID hotelId);
    List<Room> findByAvailable(boolean available);
}

