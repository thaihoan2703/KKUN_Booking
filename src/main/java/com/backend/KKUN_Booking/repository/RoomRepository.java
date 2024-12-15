package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Room;
import com.backend.KKUN_Booking.model.enumModel.BedType;
import com.backend.KKUN_Booking.model.enumModel.RoomType;
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

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.available = true " +
            "AND NOT EXISTS (" +
            "    SELECT b FROM Booking b WHERE b.room.id = r.id " +
            "    AND (b.checkinDate < :checkoutDate AND b.checkoutDate > :checkinDate)" +
            ")")
    List<Room> findAvailableRoomsByHotelAndDateRange(
            @Param("hotelId") UUID hotelId,
            @Param("checkinDate") LocalDateTime checkinDate,
            @Param("checkoutDate") LocalDateTime checkoutDate
    );

    @Query(value = "SELECT r.* FROM room r " +
            "JOIN hotels h ON r.hotel_id = h.id " +
            "WHERE r.available = TRUE " +
            "AND to_tsvector('simple', h.location) @@ plainto_tsquery('simple', :location) " +
            "AND ((:roomType IS NULL AND :bedType IS NULL) " +
            "     OR (:roomType IS NULL AND r.bed_type = :bedType) " +
            "     OR (:bedType IS NULL AND r.type = :roomType) " +
            "     OR (r.type = :roomType AND r.bed_type = :bedType)) " +
            "AND r.capacity >= :guests " +
            "AND r.id NOT IN (" +
            "    SELECT b.room_id FROM booking b " +  // Sửa từ b.room.id thành b.room_id
            "    WHERE b.status != 'CANCELLED' " +
            "    AND b.checkin_date <= :checkOutDate " +
            "    AND b.checkout_date >= :checkInDate" +
            ")",
            nativeQuery = true)
    List<Room> searchRoomsByAttributes(@Param("location") String location,
                                       @Param("roomType") String roomType,
                                       @Param("bedType") String bedType,
                                       @Param("guests") int guests,
                                       @Param("checkInDate") LocalDateTime checkInDate,
                                       @Param("checkOutDate") LocalDateTime checkOutDate);






}

