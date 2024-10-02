package com.backend.KKUN_Booking.service.provider;

import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.response.PaymentResponse;

import java.util.Map;

// Concrete implementation for MoMo
public class MoMoProvider extends PaymentProvider {
    @Override
    public PaymentResponse.BaseResponse initiatePayment(PaymentDto paymentDto) {
        // Implement MoMo-specific payment initiation
        return null;
    }

    @Override
    public PaymentResponse.BaseResponse processCallback(String provider,Map<String, String> callbackParams) {
        // Implement MoMo-specific callback processing
        return null;

    }
}