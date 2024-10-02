package com.backend.KKUN_Booking.response;

import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import lombok.*;

public class PaymentResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseResponse {
        private String code;
        private String message;
        private PaymentStatus status; // Add this line for status
        private String paymentUrl;
        private String transactionCode;


    }

    // If you have other specific response classes, ensure they're defined here
    @Getter
    @Setter
    public static class VNPayResponse extends BaseResponse {
        // Add any VNPay-specific fields if necessary
    }
}
