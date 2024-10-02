package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public PaymentDto createPayment(@RequestBody PaymentDto paymentDto) {
        return paymentService.createPayment(paymentDto);
    }

    @PutMapping("/{id}")
    public PaymentDto updatePayment(@PathVariable UUID id, @RequestBody PaymentDto paymentDto) {
        return paymentService.updatePayment(id, paymentDto);
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable UUID id) {
        paymentService.deletePayment(id);
    }



}

