package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.model.enumModel.BookingStatus;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.response.PaymentResponse;
import com.backend.KKUN_Booking.security.JwtTokenProvider;
import com.backend.KKUN_Booking.service.BookingService;
import com.backend.KKUN_Booking.service.PaymentService;
import com.backend.KKUN_Booking.util.PaymentUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public BookingController(BookingService bookingService, PaymentService paymentService, JwtTokenProvider jwtTokenProvider) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable UUID id) {
        BookingDto bookingDto = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingDto);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(
            @RequestBody BookingDto bookingDto,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userEmail = "anonymous@domain.com"; // Giá trị mặc định

        // Lấy token từ header Authorization
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            userEmail = jwtTokenProvider.getUserFromJWT(token); // Lấy email từ subject của token
        }

        try {
            // Gọi service để tạo booking
            BookingDto createdBooking = bookingService.createBooking(bookingDto, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( e.getMessage());
        }
    }

    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<PaymentResponse.BaseResponse> initiatePayment(
            @PathVariable UUID bookingId,
            HttpServletRequest request) {
        // Fetch booking details using bookingId
        BookingDto bookingDto = bookingService.getBookingById(bookingId);

        // Handle booking not found
        if (bookingDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PaymentResponse.BaseResponse.builder()
                            .code("error")
                            .message("Booking not found.")
                            .status(null)
                            .build());
        }

        // Check if the payment is already completed
        if (bookingDto.getPayment().getStatus().equals(PaymentStatus.COMPLETED)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(PaymentResponse.BaseResponse.builder()
                            .code("error")
                            .message("Payment has already been completed.")
                            .status(PaymentStatus.COMPLETED)
                            .build());
        }

        // Prepare PaymentDto with details
        PaymentDto paymentData = new PaymentDto();
        paymentData.setBookingId(bookingId);
        paymentData.setId(bookingDto.getPayment().getId());
        paymentData.setPaymentType(bookingDto.getPayment().getPaymentType());
        paymentData.setAmount(bookingDto.getPayment().getAmount());

        try {
            // Initiate payment
            PaymentResponse.BaseResponse response = paymentService.initiatePayment(paymentData, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle payment errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.BaseResponse.builder()
                            .code("error")
                            .message("An error occurred during payment initiation: " + e.getMessage())
                            .status(null)
                            .build());
        }
    }


    @GetMapping("/payment-callback")
    public ResponseEntity<PaymentResponse.BaseResponse> handlePaymentCallback(
            HttpServletRequest request,
            @RequestParam Map<String, String> callbackParams) {
        // Determine the payment provider based on the callback parameters
        String provider = PaymentUtil.determineProvider(callbackParams);

        // Process the callback using the appropriate payment provider
        PaymentResponse.BaseResponse response = paymentService.processPaymentCallback(provider, callbackParams, request);

        // Return the response
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable UUID id,
                                                    @RequestBody BookingDto bookingDto,
                                                    Principal principal) {
        // Lấy email hoặc username
        String userEmail = principal.getName();  // Lấy email hoặc username từ authentication
        BookingDto updatedBooking = bookingService.updateBooking(id, bookingDto, userEmail);
        return ResponseEntity.ok(updatedBooking);
    }
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID id, Principal principal) {
        // Lấy email hoặc username từ principal
        String userEmail = principal.getName();

        try {
            // Cập nhật trạng thái booking thành CANCELLED


            bookingService.cancelBooking(id,userEmail);
            // Nếu không có lỗi xảy ra, trả về thông báo thành công với mã HTTP 200
            return ResponseEntity.ok("Booking has been successfully cancelled.");
        } catch (IllegalStateException ex) {
            // Trường hợp không tìm thấy booking với id
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Booking not found with ID: " + id);
        } catch (Exception ex) {
            // Xử lý lỗi chung
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the cancellation.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }


}
