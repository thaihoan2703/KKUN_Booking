package com.backend.KKUN_Booking.util;

import java.util.Random;

public class VoucherUtil {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    public static String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }
}
