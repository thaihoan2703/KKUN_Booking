package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.config.payment.VNPayConfig;
import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.Booking;
import com.backend.KKUN_Booking.model.Hotel;
import com.backend.KKUN_Booking.model.Payment;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import com.backend.KKUN_Booking.repository.BookingRepository;
import com.backend.KKUN_Booking.repository.HotelRepository;
import com.backend.KKUN_Booking.repository.PaymentRepository;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.response.PaymentResponse;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.NotificationService;
import com.backend.KKUN_Booking.service.PaymentService;
import com.backend.KKUN_Booking.service.provider.PaymentProvider;
import com.backend.KKUN_Booking.service.provider.PaymentProviderFactory;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final VNPayConfig vnPayConfig;

    private final NotificationService notificationService;

    @Autowired
    private PaymentProviderFactory paymentProviderFactory;
    @Autowired
    @Lazy
    private BookingService bookingService;
    @Autowired
    private PaymentService paymentService;
    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository, HotelRepository hotelRepository, UserRepository userRepository, VNPayConfig vnPayConfig, NotificationService notificationService ) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.vnPayConfig = vnPayConfig;
        this.notificationService =notificationService;
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Payment payment = convertToEntity(paymentDto);
        return convertToDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentDto updatePayment(UUID id, PaymentDto paymentDto) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setTransactionReference(paymentDto.getTransactionReference());
        return convertToDto(paymentRepository.save(payment));
    }

    @Override
    public void deletePayment(UUID id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public List<PaymentDto> getPaymentsByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(status);
        payment.getBooking().setStatus(BookingStatus.CONFIRMED);

        // Lưu lại trang thai thanh toán và booking
        paymentRepository.save(payment);

    }

    @Override
    public List<PaymentDto> getPaymentsByHotel(UUID hotelId, String userEmail) {
        // Find the user (for authorization check if needed)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Optional: Check if user is authorized to access the hotel (e.g., only hotel owners/admins can view payments)
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        // Check if the user is the owner or an admin (authorization)
        if (!hotel.getOwner().equals(user)) {
            throw new IllegalStateException("User is not authorized to view payments for this hotel");
        }

        // Fetch all payments associated with this hotel
        List<Payment> payments = paymentRepository.findByHotelId(hotelId);

        // Map the list of Payment entities to PaymentDto objects
        List<PaymentDto> paymentDtos = payments.stream()
                .map(this::convertToDto)  // Use the convertToDto method
                .collect(Collectors.toList());

        return paymentDtos;
    }


    private PaymentDto convertToDto(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(payment.getId());
        paymentDto.setAmount(payment.getAmount());
        paymentDto.setStatus(payment.getStatus());
        paymentDto.setPaymentDate(payment.getPaymentDate());
        paymentDto.setPaymentType(payment.getPaymentType());
        // Lấy bookingId từ booking nếu booking không null
        if (payment.getBooking() != null) {
            paymentDto.setBookingId(payment.getBooking().getId());
        }

        return paymentDto;
    }


    private Payment convertToEntity(PaymentDto paymentDto) {
        Payment payment = new Payment();
        payment.setId(paymentDto.getId());
        payment.setAmount(paymentDto.getAmount());
        payment.setStatus(paymentDto.getStatus());
        payment.setPaymentDate(paymentDto.getPaymentDate());
        payment.setTransactionReference(paymentDto.getTransactionReference());
        // Tạo thực thể Booking từ bookingId nếu cần (đảm bảo booking tồn tại)
        if (paymentDto.getBookingId() != null) {
            Booking booking = bookingRepository.findById(paymentDto.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            payment.setBooking(booking);
        }

        return payment;
    }

    @Override
    public PaymentResponse.BaseResponse initiatePayment(PaymentDto paymentDto, HttpServletRequest request) {
        // Fetch the existing payment record from the database using the payment ID
        PaymentDto existingPayment = paymentService.getPaymentById(paymentDto.getId());

        // Check if the payment has already been processed
        if (existingPayment != null && PaymentStatus.COMPLETED.equals(existingPayment.getStatus())) {
            // Return a response indicating that the payment has already been completed
            return PaymentResponse.BaseResponse.builder()
                    .code("already_paid")
                    .message("This payment has already been processed.")
                    .build();
        }
        // Fetch booking details using bookingId
        PaymentProvider provider = paymentProviderFactory.getPaymentProvider(paymentDto.getPaymentType(), request);
        PaymentResponse.BaseResponse response = provider.initiatePayment(paymentDto);

        // Update payment transaction
        paymentService.updatePayment(paymentDto.getId(),paymentDto);

        return response;
    }

    @Override
    public PaymentResponse.BaseResponse processPaymentCallback(String paymentProvider, Map<String, String> callbackParams, HttpServletRequest request) {
        PaymentProvider provider = paymentProviderFactory.getPaymentProvider(PaymentType.valueOf(paymentProvider), request);
        PaymentResponse.BaseResponse response = provider.processCallback(paymentProvider,callbackParams);

        // Update booking status based on payment result
        UUID paymentId = paymentRepository.findByTransactionReference(response.getTransactionCode()).getId();
        if (response.getStatus() == PaymentStatus.COMPLETED) {
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);
            processCheckout(paymentId);

        } else if (response.getStatus() == PaymentStatus.FAILED) {
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.FAILED);
        }

        return response;
    }

    public void processCheckout(UUID paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

            // Kiểm tra điều kiện gửi thông báo
            Booking booking = payment.getBooking();
            if (booking.getStatus().equals(BookingStatus.CONFIRMED) && !booking.isReviewed()) {
                User user = booking.getUser();
                String recipientEmail = (user != null) ? user.getEmail() : booking.getBookingEmail();

                // Gửi thông báo sử dụng recipientEmail
                notificationService.sendReviewReminder(recipientEmail, booking);
            }

        } catch (MessagingException e) {
            // Xử lý ngoại lệ, có thể log lại hoặc thông báo lỗi
            System.err.println("Error sending review reminder: " + e.getMessage());
        }
    }
}

