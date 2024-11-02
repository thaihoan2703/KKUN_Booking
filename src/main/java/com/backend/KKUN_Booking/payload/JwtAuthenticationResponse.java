package com.backend.KKUN_Booking.payload;

public class JwtAuthenticationResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;

    // Constructor khởi tạo với accessToken và refreshToken, mặc định tokenType là "Bearer"
    public JwtAuthenticationResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }

    // Getter cho accessToken
    public String getAccessToken() {
        return accessToken;
    }

    // Getter cho refreshToken
    public String getRefreshToken() {
        return refreshToken;
    }

    // Getter cho tokenType
    public String getTokenType() {
        return tokenType;
    }
}
