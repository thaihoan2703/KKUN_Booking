package com.backend.KKUN_Booking.service.provider;

import com.backend.KKUN_Booking.config.payment.VNPayConfig;
import com.backend.KKUN_Booking.model.enumModel.PaymentType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Factory to create appropriate PaymentProvider
@Component
public class PaymentProviderFactory {
    @Autowired
    private VNPayConfig vnPayConfig;
    public PaymentProvider getPaymentProvider(PaymentType paymentType, HttpServletRequest request) {
        switch (paymentType) {
            case VNPAY:
                return new VNPayProvider(request, vnPayConfig);
            case MOMO:
                return new MoMoProvider();
            case POC:
                return new POCProvider();

            // Add cases for other payment types
            default:
                throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }
    }
}
