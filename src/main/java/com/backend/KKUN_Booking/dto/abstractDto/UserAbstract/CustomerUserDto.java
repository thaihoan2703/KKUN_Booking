package com.backend.KKUN_Booking.dto.abstractDto.UserAbstract;

import com.backend.KKUN_Booking.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("customer") // Specify the type for Jackson
public class CustomerUserDto extends UserDto {
    // Các thuộc tính đặc biệt cho khách hàng (nếu có)
    private List<UUID> bookingIds;
}
