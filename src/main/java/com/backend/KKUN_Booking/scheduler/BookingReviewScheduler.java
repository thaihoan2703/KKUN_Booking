package com.backend.KKUN_Booking.scheduler;

import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.NotificationService;
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

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void checkCompletedBookingsForReview() {
        List<Booking> completedBookings = bookingService.getCompletedBookingsWithoutReview();
        for (Booking booking : completedBookings) {
            notificationService.sendReviewReminder(booking.getUser(), booking);
        }
    }
}
