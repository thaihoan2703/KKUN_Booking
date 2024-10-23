package com.backend.KKUN_Booking.dto;
import lombok.*;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class ChangePasswordRequest implements Serializable {
    @JsonProperty("oldPassword")
    private String oldPassword;

    @JsonProperty("newPassword")
    private String newPassword;

    @JsonProperty("confirmNewPassword")
    private String confirmNewPassword;

    // Getters và Setters
}