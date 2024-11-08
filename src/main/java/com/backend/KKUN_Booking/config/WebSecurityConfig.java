package com.backend.KKUN_Booking.config;

import com.backend.KKUN_Booking.exception.UserAlreadyExistsException;
import com.backend.KKUN_Booking.model.User;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.security.JwtAuthenticationFilter;
import com.backend.KKUN_Booking.security.JwtTokenProvider;
import com.backend.KKUN_Booking.service.OAuthService;
import com.backend.KKUN_Booking.service.UserService;
import com.backend.KKUN_Booking.service.implement.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserService userService;
    private final UserServiceImpl userServiceImpl;
    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuthService; // Service xử lý người dùng OAuth2

    @Autowired
    private JwtTokenProvider tokenProvider; // JWT Token provider
    @Bean
    public UserDetailsService userDetailsService() {
        return userServiceImpl;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Lazy JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors().and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        // Những route cho phép truy cập chung
                        .requestMatchers(HttpMethod.GET, "/api/blogs/**","/api/search/**", "/api/bookings/{id}", "/api/hotels/**", "/api/amenities/**", "/api/rooms/**", "/api/reviews/**","/api/reviews/rooms/**", "/api/recommendations/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**", "/api/search/**", "/api/recommendations/**","/api/chat/**").permitAll()
                        .requestMatchers("/", "/api/auth/**", "/api/auth/google", "/api/auth/login", "/api/auth/register", "/oauth2/**").permitAll()

                        // Route `/api/users/me` cho phép tất cả vai trò cập nhật thông tin cá nhân
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").hasAnyAuthority(RoleUser.ADMIN.name(), RoleUser.HOTELOWNER.name(), RoleUser.CUSTOMER.name())
                        .requestMatchers(HttpMethod.POST, "/api/blogs/**").hasAnyAuthority(RoleUser.ADMIN.name(), RoleUser.HOTELOWNER.name(), RoleUser.CUSTOMER.name())

                        // Những route yêu cầu quyền quản trị - ADMIN
                        .requestMatchers("/admin/**", "/api/roles/**", "/api/hotels").hasAuthority(RoleUser.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/roles/**").hasAuthority(RoleUser.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority(RoleUser.ADMIN.name()) // Cho phép ADMIN quản lý mọi người dùng

                        // Những route khác cho RoleUser.HOTELOWNER
                        .requestMatchers(HttpMethod.POST, "/api/hotels/**", "/api/rooms/**", "/api/payments/**").hasAuthority(RoleUser.HOTELOWNER.name())
                        .requestMatchers(HttpMethod.PUT, "/api/hotels/**", "/api/rooms/**", "/api/payments/**").hasAuthority(RoleUser.HOTELOWNER.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/hotels/**", "/api/rooms/**", "/api/payments/**").hasAuthority(RoleUser.HOTELOWNER.name())

                        // Route đặc quyền khách hàng - CUSTOMER
                        .requestMatchers(HttpMethod.GET, "/api/users/booking-hotel/history").hasAuthority(RoleUser.CUSTOMER.name())
                        .requestMatchers("/api/wishlist/**", "/api/bookings/**").hasAuthority(RoleUser.CUSTOMER.name())

                        // Đảm bảo các quyền còn lại là xác thực
                        .anyRequest().authenticated()
                )


                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        }))
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .oauth2Login(oauth2Login -> oauth2Login
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuthService))
                        .successHandler((request, response, authentication) -> {
                            // Lấy thông tin từ OIDC user
                            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                            String email = oidcUser.getEmail();

                            // Lưu người dùng nếu chưa tồn tại
                            User user = userService.findOrSaveOauthUser(email, oidcUser.getFullName());

                            // Tạo JWT token
                            Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            String jwtToken = tokenProvider.generateAccessToken(auth);

                            // Trả về JWT token trong response body
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            String json = String.format("{\"accessToken\": \"%s\"}", jwtToken);
                            response.getWriter().write(json);
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/403"))
                .sessionManagement(sessionManagement -> sessionManagement
                        .maximumSessions(1)
                        .expiredUrl("/login"))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService());
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5005"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true); // If you need to allow credentials like cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // Apply to all endpoints

        return new CorsFilter(source);
    }
}
