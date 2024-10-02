package com.backend.KKUN_Booking.repository;

import com.backend.KKUN_Booking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    // Các phương thức truy vấn tùy chỉnh (nếu cần)
    List<Payment> findByBookingId(UUID bookingId);

    Payment findByTransactionReference(String transactionReference);
}

