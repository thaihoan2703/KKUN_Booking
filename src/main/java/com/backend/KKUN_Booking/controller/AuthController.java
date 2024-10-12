package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.LoginDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.payload.JwtAuthenticationResponse;
import com.backend.KKUN_Booking.security.JwtTokenProvider;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> authenticateUser(@RequestBody LoginDto loginDto) {
        // Xác thực người dùng
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            // If successful, set the authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tạo JWT token
            String jwt = tokenProvider.generateToken(authentication);

            // Trả về JWT token với trạng thái 200 OK
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));

        } catch (BadCredentialsException e) {
            // Handle wrong password case
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (AuthenticationException e) {
            // Handle other authentication errors
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser); // Trả về người dùng đã tạo với trạng thái 201 Created
    }
}
