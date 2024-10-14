package com.backend.KKUN_Booking.config;

import com.backend.KKUN_Booking.exception.UserAlreadyExistsException;
import com.backend.KKUN_Booking.model.enumModel.RoleUser;
import com.backend.KKUN_Booking.security.JwtAuthenticationFilter;
import com.backend.KKUN_Booking.service.OAuthService;
import com.backend.KKUN_Booking.service.UserService;
import com.backend.KKUN_Booking.service.implement.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
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

    private final OAuthService oAuthService;
    private final UserService userService;
    private final UserServiceImpl userServiceImpl;

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
                        .requestMatchers("/", "/api/**", "/api/auth/**","/api/auth/login").permitAll()
                        .requestMatchers("/admin/**",  "/api/roles/**").hasAnyAuthority(RoleUser.ADMIN.name())
                        .requestMatchers("/api/hotels**", "/api/rooms/**","/api/payments/**","/api/amenities/**").hasAuthority(RoleUser.HOTELOWNER.name())
                        .requestMatchers("/api/wishlist/**","/api/bookings/**").hasAuthority(RoleUser.CUSTOMER.name())
                        .requestMatchers("/@**/settings").authenticated()
                        .anyRequest().authenticated())
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
                        .loginPage("/login")
                        .failureHandler((request, response, exception) -> {
                            request.getSession().setAttribute("error", "Đăng nhập thất bại: " + exception.getMessage());
                            response.sendRedirect("/login");
                        })
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuthService))
                        .successHandler((request, response, authentication) -> {
                            var oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                            try {
                                userService.saveOauthUser(oidcUser.getEmail());
                                response.sendRedirect("/");
                            } catch (UserAlreadyExistsException e) {
                                request.getSession().setAttribute("error", e.getMessage());
                                response.sendRedirect("/login");
                            }
                        })
                        .permitAll())
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
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true); // If you need to allow credentials like cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // Apply to all endpoints

        return new CorsFilter(source);
    }
}
