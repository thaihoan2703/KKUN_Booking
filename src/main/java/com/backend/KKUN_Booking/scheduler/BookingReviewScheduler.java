package com.backend.KKUN_Booking.scheduler;

import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.NotificationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingReviewScheduler {

    private final BookingService bookingService;
    private final NotificationService notificationService;

    @Autowired
    public BookingReviewScheduler(BookingService bookingService, NotificationService notificationService) {
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    public void checkCompletedBookingsForReview() {
        List<Booking> completedBookings = bookingService.getCompletedBookingsWithoutReview();
        for (Booking booking : completedBookings) {
            try {
                User user = booking.getUser();
                String recipientEmail = (user != null) ? user.getEmail() : booking.getBookingEmail();

                // Gửi thông báo sử dụng recipientEmail
                notificationService.sendReviewReminder(recipientEmail, booking);
            } catch (MessagingException e) {
                // Xử lý ngoại lệ ở đây (log, thông báo, v.v.)
                System.err.println("Error sending review reminder: " + e.getMessage());
            }
        }
    }
}
