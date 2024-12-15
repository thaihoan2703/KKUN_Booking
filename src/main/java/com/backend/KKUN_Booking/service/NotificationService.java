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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotificationService {

    private final JavaMailSender emailSender;

    @Value("${UI_WEBSITE_URL}") // Tiêm giá trị từ local.env
    private String baseUrl;
    @Autowired
    public NotificationService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendReviewReminder(String recipientEmail, Booking booking) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart

        // Đặt email người nhận
        helper.setTo(recipientEmail);

        // Tiêu đề email
        helper.setSubject("Reminder: Please review your recent stay");

        // Tên người nhận (nếu user không tồn tại, lấy tên từ booking)
        String recipientName = (booking.getUser() != null && booking.getUser().getLastName() != null)
                ? booking.getUser().getLastName()
                : booking.getBookingName();

        // Tạo link đến trang đánh giá
        String link = baseUrl + "/rooms/" + booking.getRoom().getId() + "/bookings/" + booking.getId() + "/review";

        // Nội dung email
        String emailContent = "<p>Dear " + recipientName + ",</p>" +
                "<p>We hope you enjoyed your recent stay at our hotel. " +
                "We would greatly appreciate if you could take a moment to review your experience. " +
                "Your feedback is valuable to us and helps us improve our services.</p>" +
                "<p>To leave a review, please visit our website and go to your booking history or you can <a href=\"" + link + "\">click this link</a>.</p>" +
                "<p>Thank you for choosing our hotel.</p>" +
                "<p>Best regards,<br><strong>" +
                booking.getRoom().getHotel().getName() + "</strong></p>";

        // Gửi email
        helper.setText(emailContent, true); // true = HTML
        emailSender.send(message);
    }
    public void sendBookingConfirmation(String recipientEmail, Booking booking) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart

        // Set recipient email
        helper.setTo(recipientEmail);

        // Email subject
        helper.setSubject("Booking Confirmation - " + booking.getRoom().getHotel().getName());

        // Determine recipient name
        String recipientName = (booking.getUser() != null && booking.getUser().getLastName() != null)
                ? booking.getUser().getLastName()
                : booking.getBookingName();

        // Format booking dates
        LocalDateTime checkInDateTime = booking.getCheckinDate();
        DateTimeFormatter checkInFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedCheckInDate = checkInDateTime.format(checkInFormatter);

        // Format Check-out Date
        LocalDateTime checkOutDateTime = booking.getCheckoutDate();
        DateTimeFormatter checkOutFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedCheckOutDate = checkOutDateTime.format(checkOutFormatter);

        // Format Total Price
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = currencyFormatter.format(booking.getTotalPrice());


        // Email content
        String emailContent = "<p>Dear " + recipientName + ",</p>" +
                "<p>Thank you for your booking with " + booking.getRoom().getHotel().getName() + ". Here are the details of your reservation:</p>" +
                "<table style='border-collapse: collapse; width: 100%;'>" +
                "<tr><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'><strong>Hotel:</strong></td><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + booking.getRoom().getHotel().getName() + "</td></tr>" +
                "<tr><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'><strong>Booking ID:</strong></td><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + booking.getId() + "</td></tr>" +
                "<tr><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'><strong>Room Type:</strong></td><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + booking.getRoom().getType() + "</td></tr>" +
                "<tr><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'><strong>Check-in Date:</strong></td><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + formattedCheckInDate  + "</td></tr>" +
                "<tr><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'><strong>Check-out Date:</strong></td><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + formattedCheckOutDate  + "</td></tr>" +
                "<tr><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'><strong>Total Price:</strong></td><td style='border: 1px solid #dddddd; text-align: left; padding: 8px;'>" + formattedPrice + "</td></tr>" +
                "</table>" +
                "<p>If you have any questions or need to make changes to your reservation, please contact our customer service.</p>" +
                "<p>We look forward to welcoming you!</p>" +
                "<p>Best regards,<br><strong>" + booking.getRoom().getHotel().getName() + "</strong></p>";

        // Send email
        helper.setText(emailContent, true); // true = HTML
        emailSender.send(message);
    }
}
