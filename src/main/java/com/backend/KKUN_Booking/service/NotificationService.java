package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender emailSender;

    @Value("${UI_WEBSITE_URL}") // Tiêm giá trị từ local.env
    private String baseUrl;
    @Autowired
    public NotificationService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendReviewReminder(User user, Booking booking) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart

        helper.setTo(user.getEmail());
        helper.setSubject("Reminder: Please review your recent stay");

        String link = baseUrl + "/rooms/" + booking.getRoom().getId() + "/bookings/" + booking.getId() + "/review"; // Đường dẫn đến trang đánh giá
        String emailContent = "<p>Dear " + user.getLastName() + ",</p>" +
                "<p>We hope you enjoyed your recent stay at our hotel. " +
                "We would greatly appreciate if you could take a moment to review your experience. " +
                "Your feedback is valuable to us and helps us improve our services.</p>" +
                "<p>To leave a review, please visit our website and go to your booking history or you can <a href=\"" + link + "\">click this link</a>.</p>" +
                "<p>Thank you for choosing our hotel.</p>" +
                "<p>Best regards,<br><strong>" +
                booking.getRoom().getHotel().getName() + "</strong></p>";

        helper.setText(emailContent, true); // true = HTML

        emailSender.send(message);
    }
}
