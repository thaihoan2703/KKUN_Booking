package com.backend.KKUN_Booking.service.implement;

import com.backend.KKUN_Booking.dto.LoginDto;
import com.backend.KKUN_Booking.dto.UserDto;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.payload.JwtAuthenticationResponse;
import com.backend.KKUN_Booking.security.JwtTokenProvider;
import com.backend.KKUN_Booking.security.UserDetailsImpl;
import com.backend.KKUN_Booking.service.AuthService;
import com.backend.KKUN_Booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    public JwtAuthenticationResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        return new JwtAuthenticationResponse(accessToken, refreshToken);
    }

    public JwtAuthenticationResponse authenticateGoogleToken(String tokenId) {
        String url = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + tokenId;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userInfo = response.getBody();
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");

            User user = userService.findOrSaveOauthUser(email, name);
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, user.getAuthorities());
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            return new JwtAuthenticationResponse(accessToken, refreshToken);
        } else {
            throw new BadCredentialsException("Invalid token");
        }
    }

    public UserDto register(UserDto userDto) {
        return userService.createUser(userDto);
    }

    @Override
    public JwtAuthenticationResponse refreshAccessToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = tokenProvider.getUserFromJWT(refreshToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) userService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        return new JwtAuthenticationResponse(newAccessToken, refreshToken);
    }
}
