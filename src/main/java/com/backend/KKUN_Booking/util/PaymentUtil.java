package com.backend.KKUN_Booking.util;

import com.backend.KKUN_Booking.model.enumModel.PaymentType;

import java.util.Map;

public class PaymentUtil {
    public static String determineProvider(Map<String, String> callbackParams) {
        if (callbackParams.containsKey("vnp_BankCode")) {
            return PaymentType.VNPAY.name(); // or other identifiers for VNPay
        } else if (callbackParams.containsKey("momo_transaction_id")) {
            return PaymentType.MOMO.name(); // replace with the actual parameter for MoMo
        } else if (callbackParams.containsKey("paypal_transaction_id")) {
            return PaymentType.PAYPAL.name(); // replace with the actual parameter for PayPal
        }
        return "Unknown"; // Fallback if no provider is matched
    }


}
