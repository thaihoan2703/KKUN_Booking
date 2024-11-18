package com.backend.KKUN_Booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${app.refreshTokenExpirationMs}")
    private int refreshTokenExpirationMs;

    // Tạo Access Token
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtExpirationMs);
    }

    // Tạo Refresh Token
    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenExpirationMs);
    }

    // Phương thức dùng chung để tạo token
    private String generateToken(Authentication authentication, int expirationMs) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            UUID userId = userDetails.getId();
            String firstName = userDetails.getFirstName();
            String lastName = userDetails.getLastName();
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expirationMs);

            return Jwts.builder()
                    .setSubject(userDetails.getUsername())
                    .claim("userId", userId.toString())
                    .claim("firstName", firstName)
                    .claim("lastName", lastName)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(jwtSecret)
                    .compact();
        } else {
            throw new IllegalArgumentException("Principal is not an instance of UserDetailsImpl");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("Token hết hạn: " + e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) {
            System.out.println("Chữ ký không hợp lệ: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("Token sai định dạng: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi khác: " + e.getMessage());
        }
        return false;
    }

    public String getUserFromJWT(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret).build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("Token hết hạn khi lấy thông tin user: " + e.getMessage());
        } catch (io.jsonwebtoken.SignatureException e) {
            System.out.println("Chữ ký không hợp lệ: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("Token sai định dạng khi lấy thông tin user: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi khác khi lấy thông tin user: " + e.getMessage());
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token, UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
