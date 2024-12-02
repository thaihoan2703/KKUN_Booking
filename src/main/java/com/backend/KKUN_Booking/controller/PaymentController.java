package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping
    public List<PaymentDto> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public PaymentDto getPaymentById(@PathVariable UUID id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByHotel(@PathVariable UUID hotelId, Principal principal) {
        String userEmail = principal.getName();

        // Call the service to get the payments for the hotel
        List<PaymentDto> paymentDtos = paymentService.getPaymentsByHotel(hotelId, userEmail);

        // Return ResponseEntity with HTTP status OK (200) and the list of PaymentDto
        return ResponseEntity.ok(paymentDtos);
    }

    @PostMapping
    public PaymentDto createPayment(@RequestBody PaymentDto paymentDto) {
        return paymentService.createPayment(paymentDto);
    }

    @PutMapping("/{id}")
    public PaymentDto updatePayment(@PathVariable UUID id, @RequestBody PaymentDto paymentDto) {
        return paymentService.updatePayment(id, paymentDto);
    }

    @PutMapping("/{id}/updatePaymentSuccessStatus")
    public ResponseEntity<String> updatePaymentSuccessStatus(@PathVariable UUID id) {
        // Cập nhật trạng thái thanh toán thành "COMPLETED"
        paymentService.updatePaymentStatus(id, PaymentStatus.COMPLETED);

        // Trả về phản hồi thành công
        return ResponseEntity.ok("Payment status updated successfully.");
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable UUID id) {
        paymentService.deletePayment(id);
    }



}

