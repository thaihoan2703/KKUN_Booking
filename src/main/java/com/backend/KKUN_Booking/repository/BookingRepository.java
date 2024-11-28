package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.Room;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<Booking> findByUserId(UUID userId);
    List<Booking> findByRoom_Hotel_Id(UUID hotelId);
    List<Booking> findByStatus(String status);

    @Query("SELECT b FROM Booking b WHERE b.room = :room AND " +
            "((b.checkinDate <= :checkoutDate AND b.checkoutDate >= :checkinDate) OR " +
            "(b.checkinDate >= :checkinDate AND b.checkinDate < :checkoutDate))")
    List<Booking> findByRoomAndDateRange(@Param("room") Room room,
                                         @Param("checkinDate") LocalDateTime checkinDate,
                                         @Param("checkoutDate") LocalDateTime checkoutDate);

    List<Booking> findByCheckoutDateBeforeAndReviewedFalse(LocalDateTime date);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId")
    List<Booking> findByRoomId(@Param("roomId") UUID roomId);

  //  @Transactional
//    public void deleteRoom(UUID roomId) {
//        if (bookingRepository.existsByRoomId(roomId)) {
//            throw new IllegalStateException("Cannot delete room. It is still referenced by bookings.");
//        }
//        roomRepository.deleteById(roomId);
//    }

}

