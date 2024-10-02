package com.backend.KKUN_Booking.service.provider;

import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.response.PaymentResponse;

import java.util.Map;

// Abstract base class for all payment providers
public abstract class PaymentProvider {
    public abstract PaymentResponse.BaseResponse initiatePayment(PaymentDto paymentDto);
    public abstract PaymentResponse.BaseResponse processCallback(String paymentProvider,Map<String, String> callbackParams);
}
