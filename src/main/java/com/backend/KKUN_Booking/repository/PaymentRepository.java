package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<Payment> findByBookingId(UUID bookingId);

    @Query("SELECT p FROM Payment p " +
            "JOIN p.booking b " +          // Join with the Booking entity
            "JOIN b.room r " +             // Join with the Room entity
            "JOIN r.hotel h " +            // Join with the Hotel entity
            "WHERE h.id = :hotelId")       // Filter by hotelId
    List<Payment> findByHotelId(UUID hotelId);
    Payment findByTransactionReference(String transactionReference);
}

