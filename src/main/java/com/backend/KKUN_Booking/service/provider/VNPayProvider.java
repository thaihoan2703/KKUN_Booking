package com.backend.KKUN_Booking.service.provider;


import com.backend.KKUN_Booking.config.payment.VNPayConfig;
import com.backend.KKUN_Booking.dto.PaymentDto;
import com.backend.KKUN_Booking.exception.ResourceNotFoundException;
import com.backend.KKUN_Booking.model.enumModel.PaymentStatus;
import com.backend.KKUN_Booking.response.PaymentResponse;
import com.backend.KKUN_Booking.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// Concrete implementation for VNPay
public class VNPayProvider extends PaymentProvider {
    private final HttpServletRequest request;


    private final VNPayConfig vnPayConfig;
    public VNPayProvider(HttpServletRequest request, VNPayConfig vnPayConfig ) {
        this.request = request;
        this.vnPayConfig = vnPayConfig;
    }
    @Override
    public PaymentResponse.BaseResponse initiatePayment(PaymentDto paymentDto) {
        // Implement VNPay-specific payment initiation
        BigDecimal amount = paymentDto.getAmount().multiply(BigDecimal.valueOf(100L));
        long amountInLong = amount.longValue();
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amountInLong));

        String bankCode = request.getParameter("bankCode");
        // Only add bank code if it's provided in the PaymentDto
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        // Build query url without secure hash
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);

        // Calculate secure hash
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), queryUrl);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        String transactionReference = vnpParamsMap.get("vnp_TxnRef") + vnpParamsMap.get("vnp_Amount") + vnpParamsMap.get("vnp_OrderInfo");
        paymentDto.setTransactionReference(VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), transactionReference));
        return PaymentResponse.BaseResponse.builder()
                .code("200")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }


    @Override
    public PaymentResponse.BaseResponse processCallback(String provider, Map<String, String> callbackParams) {
        try {
            // Validate secure hash
            if (!validateSecureHash(callbackParams)) {
                return buildErrorResponse("Invalid signature", PaymentStatus.FAILED);
            }

            String vnpTxnRef = callbackParams.get("vnp_TxnRef");
            if (vnpTxnRef == null || vnpTxnRef.isEmpty()) {
                return buildErrorResponse("Transaction reference is missing", PaymentStatus.FAILED);
            }

            String responseCode = callbackParams.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                String transactionReference = callbackParams.get("vnp_TxnRef") + callbackParams.get("vnp_Amount") + callbackParams.get("vnp_OrderInfo");
                String transactionCode = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), transactionReference);
                return buildSuccessResponse(transactionCode);
            } else {
                return buildErrorResponse("Payment failed: " + responseCode, PaymentStatus.FAILED);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private boolean validateSecureHash(Map<String, String> callbackParams) {
        Map<String, String> paramsWithoutHash = new HashMap<>(callbackParams);
        paramsWithoutHash.remove("vnp_SecureHash");
        String hashData = VNPayUtil.getPaymentURL(paramsWithoutHash, false);
        String expectedSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        String receivedSecureHash = callbackParams.get("vnp_SecureHash");
        return receivedSecureHash.equals(expectedSecureHash);
    }

    private PaymentResponse.BaseResponse buildErrorResponse(String message, PaymentStatus status) {
        return PaymentResponse.BaseResponse.builder()
                .code("error")
                .message(message)
                .status(status)
                .build();
    }

    private PaymentResponse.BaseResponse buildSuccessResponse(String transactionCode) {
        return PaymentResponse.BaseResponse.builder()
                .code("200")
                .message("Payment successful")
                .status(PaymentStatus.COMPLETED)
                .transactionCode(transactionCode)  // Return paymentId
                .build();
    }

    private PaymentResponse.BaseResponse handleException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return buildErrorResponse("Invalid booking ID format", PaymentStatus.FAILED);
        } else if (e instanceof ResourceNotFoundException) {
            return buildErrorResponse("Booking not found", PaymentStatus.FAILED);
        } else {
            return buildErrorResponse("An unexpected error occurred", PaymentStatus.FAILED);
        }
    }


}
