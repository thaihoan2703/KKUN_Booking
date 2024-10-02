package com.backend.KKUN_Booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Sử dụng khóa bảo mật mạnh hơn (SecretKey) thay vì chuỗi
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @SuppressWarnings("deprecation")
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret )
                .compact();
    }

    // Validate the token
    public boolean validateToken(String token) {
        try {
            System.out.println("Validating token: " + token);
            Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }


    // Extract username (or user information) from JWT token
    public String getUserFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // Extract the subject (username)
    }

    // Extract the authentication object for the SecurityContextHolder
    public UsernamePasswordAuthenticationToken getAuthentication(String token, UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
