package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<Room> findByHotelId(UUID hotelId);
    List<Room> findByAvailable(boolean available);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId " +
            "AND r.id NOT IN (" +
            "    SELECT b.room.id FROM Booking b " +
            "    WHERE b.room.hotel.id = :hotelId " +
            "    AND ((b.checkinDate <= :checkoutDate AND b.checkoutDate >= :checkinDate) " +
            "         OR (b.checkinDate >= :checkinDate AND b.checkinDate < :checkoutDate))" +
            ")")
    List<Room> findAvailableRoomsByHotelAndDateRange(
            @Param("hotelId") UUID hotelId,
            @Param("checkinDate") LocalDateTime checkinDate,
            @Param("checkoutDate") LocalDateTime checkoutDate
    );
}

