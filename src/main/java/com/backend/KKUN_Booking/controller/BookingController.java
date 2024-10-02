package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.BookingDto;
import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.response.PaymentResponse;
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

    @Autowired
    public BookingController(BookingService bookingService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
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
    public ResponseEntity<BookingDto> createBooking( @RequestBody BookingDto bookingDto,
                                                     Principal principal) {
        // Lấy email hoặc username
        String userEmail = principal.getName();  // Lấy email hoặc username từ authentication
        BookingDto createdBooking = bookingService.createBooking(bookingDto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<PaymentResponse.BaseResponse> initiatePayment(
            @PathVariable UUID bookingId,
            HttpServletRequest request) {
        // Fetch booking details using bookingId
        BookingDto bookingDto = bookingService.getBookingById(bookingId);

        if (bookingDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Handle booking not found
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
        paymentData.setId(bookingService.getBookingById(bookingId).getPayment().getId());
        paymentData.setPaymentType(bookingDto.getPayment().getPaymentType());
        paymentData.setAmount(bookingDto.getPayment().getAmount());

        PaymentResponse.BaseResponse response = paymentService.initiatePayment(paymentData, request);
        return ResponseEntity.ok(response);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
