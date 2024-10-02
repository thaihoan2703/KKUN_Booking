package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender emailSender;

    @Autowired
    public NotificationService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendReviewReminder(User user, Booking booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());

        message.setSubject("Reminder: Please review your recent stay");
        message.setText("Dear " + user.getLastName() + ",\n\n" +
                "We hope you enjoyed your recent stay at our hotel. " +
                "We would greatly appreciate if you could take a moment to review your experience. " +
                "Your feedback is valuable to us and helps us improve our services.\n\n" +
                "To leave a review, please visit our website and go to your booking history.\n\n" +
                "Thank you for choosing our hotel.\n\n" +
                "Best regards,\n" +
                "The Hotel Team");
        emailSender.send(message);
    }
}
