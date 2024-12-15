package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.LoginDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.payload.JwtAuthenticationResponse;
import com.backend.KKUN_Booking.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDto loginDto) {
        try {
            JwtAuthenticationResponse jwtResponse = authService.login(loginDto);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleToken(@RequestBody Map<String, String> request) {
        String tokenId = request.get("accessToken");
        if (tokenId == null || tokenId.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing tokenId");
        }
        try {
            JwtAuthenticationResponse jwtResponse = authService.authenticateGoogleToken(tokenId);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = authService.register(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("You have been logged out successfully.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            JwtAuthenticationResponse jwtResponse = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}
