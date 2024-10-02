package com.backend.KKUN_Booking.dto;

import com.backend.KKUN_Booking.model.enumModel.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;  // Add password field
    private String alias;
    private UserStatus status;
    private UUID roleId;   // ID của Role
    // Getters and Setters
}

