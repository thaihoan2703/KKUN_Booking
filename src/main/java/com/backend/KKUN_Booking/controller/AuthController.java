package com.backend.KKUN_Booking.controller;

import com.backend.KKUN_Booking.dto.LoginDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.exception.UserAlreadyExistsException;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.payload.JwtAuthenticationResponse;
import com.backend.KKUN_Booking.repository.UserRepository;
import com.backend.KKUN_Booking.security.JwtTokenProvider;
import com.backend.KKUN_Booking.security.UserDetailsImpl;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userRepository = userRepository;
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

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleToken(@RequestBody Map<String, String> request) {
        String tokenId = request.get("accessToken");

        if (tokenId == null || tokenId.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing tokenId");
        }

        try {
            // Gọi Google API để xác thực tokenId và lấy thông tin người dùng
            String url = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + tokenId;
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = response.getBody();

                String email = (String) userInfo.get("email");
                String name = (String) userInfo.get("name");

                // Kiểm tra và lưu người dùng nếu chưa tồn tại
                User user = userService.findOrSaveOauthUser(email, name);
                // Chuyển đổi từ User sang UserDetailsImpl để đồng bộ với cơ chế xác thực
                UserDetailsImpl userDetails = UserDetailsImpl.build(user);
                // Tạo JWT token
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, user.getAuthorities());
                String jwtToken = tokenProvider.generateToken(authentication);

                // Trả về JWT token cho frontend
                return ResponseEntity.ok(new JwtAuthenticationResponse(jwtToken));

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error validating token");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser); // Trả về người dùng đã tạo với trạng thái 201 Created
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logoutUser() {
        // Hủy bỏ thông tin xác thực hiện tại
        SecurityContextHolder.clearContext();

        // Trả về phản hồi rằng người dùng đã đăng xuất thành công
        return ResponseEntity.ok("You have been logged out successfully.");
    }
}
