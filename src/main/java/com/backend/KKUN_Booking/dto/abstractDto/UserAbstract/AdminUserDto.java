package com.backend.KKUN_Booking.dto.abstractDto.UserAbstract;

import com.backend.KKUN_Booking.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("admin") // Specify the type for Jackson
public class AdminUserDto extends UserDto {
    // Các thuộc tính đặc biệt cho admin
    private List<String> managedSections;
    private int actionCount;

}