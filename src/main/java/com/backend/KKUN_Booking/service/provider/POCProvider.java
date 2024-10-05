package com.backend.KKUN_Booking.service.provider;

import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.response.PaymentResponse;

import java.util.Map;

import static java.util.stream.DoubleStream.builder;

public class POCProvider extends PaymentProvider{
    @Override
    public PaymentResponse.BaseResponse initiatePayment(PaymentDto paymentDto) {
        return PaymentResponse.BaseResponse.builder()
                .code("200")
                .message("success")
                .status(PaymentStatus.PAY_ON_CHECKOUT)
                .build();
    }

    @Override
    public PaymentResponse.BaseResponse processCallback(String provider,Map<String, String> callbackParams) {
        return PaymentResponse.BaseResponse.builder()
                .code("200")
                .message("Payment successful")
                .status(PaymentStatus.COMPLETED)
                .build();
    }
}
