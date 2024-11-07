package com.backend.KKUN_Booking.service;

import com.backend.KKUN_Booking.dto.LoginDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.payload.JwtAuthenticationResponse;

public interface AuthService {
    JwtAuthenticationResponse login(LoginDto loginDto);
    JwtAuthenticationResponse authenticateGoogleToken(String tokenId);
    UserDto register(UserDto userDto);
    JwtAuthenticationResponse refreshAccessToken(String refreshToken);
}
