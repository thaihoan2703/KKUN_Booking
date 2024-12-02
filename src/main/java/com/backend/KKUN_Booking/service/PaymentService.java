package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    List<PaymentDto> getAllPayments();
    PaymentDto getPaymentById(UUID id);
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto updatePayment(UUID id, PaymentDto paymentDto);
    void deletePayment(UUID id);
    List<PaymentDto> getPaymentsByBookingId(UUID bookingId);
    void updatePaymentStatus(UUID paymentId, PaymentStatus status);
    void processCheckout(UUID paymentId);
    PaymentResponse.BaseResponse initiatePayment(PaymentDto paymentDto, HttpServletRequest request);
    PaymentResponse.BaseResponse processPaymentCallback(String paymentProvider, Map<String, String> callbackParams,HttpServletRequest request );
    List<PaymentDto> getPaymentsByHotel(UUID hotelId,String userEmail);
}

